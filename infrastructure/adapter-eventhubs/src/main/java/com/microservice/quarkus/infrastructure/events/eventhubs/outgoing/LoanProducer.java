package com.microservice.quarkus.infrastructure.events.eventhubs.outgoing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.microservice.quarkus.domain.events.LoanStateChanged;
import com.microservice.quarkus.domain.ports.spi.EventBus;
import com.microservice.quarkus.domain.shared.DomainEvent;
import com.microservice.quarkus.infrastructure.events.eventhubs.LoanStateChangedAvro;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;

@ApplicationScoped
@Slf4j
public class LoanProducer implements EventBus {

    private final EventHubProducerClient producerClient;

    @Inject
    public LoanProducer(EventHubProducerClient producerClient) {
        this.producerClient = producerClient;
    }

    @Override
    public void publish(Collection<DomainEvent> events) {
        log.debug("publish({})", events);
        
        try {
            EventDataBatch batch = producerClient.createBatch();
            
            for (DomainEvent event : events) {
                LoanStateChanged loanEvent = (LoanStateChanged) event;
                
                LoanStateChangedAvro avroEvent = LoanStateChangedAvro.newBuilder()
                        .setEventId(loanEvent.getEventId().toString())
                        .setFromState(loanEvent.getFromState().toString())
                        .setOccurredOn(loanEvent.occurredOn().toString())
                        .setState(loanEvent.getState().toString())
                        .setLoanId(loanEvent.getLoanId().getId())
                        .build();
                
                byte[] eventData = serializeAvroEvent(avroEvent);
                
                EventData eventHubData = new EventData(eventData);
                eventHubData.getProperties().put("eventType", "LoanStateChanged");
                
                if (!batch.tryAdd(eventHubData)) {
                    // Batch is full, send it and create a new one
                    producerClient.send(batch);
                    batch = producerClient.createBatch();
                    
                    // Try to add the event to the new batch
                    if (!batch.tryAdd(eventHubData)) {
                        throw new RuntimeException("Event is too large for an empty batch");
                    }
                }
            }
            
            // Send the final batch if it's not empty
            if (batch.getCount() > 0) {
                producerClient.send(batch);
            }
            
        } catch (Exception e) {
            log.error("Error publishing events to Event Hubs", e);
            throw new RuntimeException("Failed to publish events to Event Hubs", e);
        }
    }
    
    private byte[] serializeAvroEvent(LoanStateChangedAvro event) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);
        DatumWriter<LoanStateChangedAvro> writer = new SpecificDatumWriter<>(LoanStateChangedAvro.class);
        
        writer.write(event, encoder);
        encoder.flush();
        return outputStream.toByteArray();
    }
}
