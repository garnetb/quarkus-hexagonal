package com.microservice.quarkus.infrastructure.events.eventhubs.incoming;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.microservice.quarkus.application.ports.commands.LoanStatusService;
import com.microservice.quarkus.domain.model.loan.LoanId;
import com.microservice.quarkus.infrastructure.events.eventhubs.LoanStateChangedAvro;
import io.quarkus.arc.profile.IfBuildProfile;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;

@ApplicationScoped
@IfBuildProfile("prod")
@Slf4j
public class LoanConsumer {

    private final EventProcessorClientBuilder eventProcessorClientBuilder;
    private final LoanStatusService loanStatusService;

    @Inject
    public LoanConsumer(EventProcessorClientBuilder eventProcessorClientBuilder,
                        LoanStatusService loanStatusService) {
        this.eventProcessorClientBuilder = eventProcessorClientBuilder;
        this.loanStatusService = loanStatusService;
    }

    @PostConstruct
    void initialize() {
        try {
            log.info("Initializing Event Hubs consumer");

            EventProcessorClient eventProcessorClient = eventProcessorClientBuilder
                .processEvent(this::processEvent)
                .processError(this::processError)
                .buildEventProcessorClient();
            // Start the event processor client
            eventProcessorClient.start();

            log.info("Event processor client started");
        } catch (Exception e) {
            log.error("Error initializing Event Hubs consumer", e);
        }
    }

    /**
     * Process errors from the Event Hubs processor.
     */
    private void processError(ErrorContext errorContext) {
        log.error("Error occurred in EventProcessor: {}", errorContext.getThrowable().getMessage());
    }

    /**
     * Process an Event Hubs event.
     * This method would be called by the EventProcessorClient when an event is received.
     */
    private void processEvent(EventContext eventContext) {
        try {
            EventData eventData = eventContext.getEventData();

            // Check if this is a LoanStateChanged event
            String eventType = (String) eventData.getProperties().get("eventType");
            if ("LoanStateChanged".equals(eventType)) {
                LoanStateChangedAvro event = deserializeAvroEvent(eventData.getBody());
                log.info("Got a loan event: {}", event);

                LoanId loanId = LoanId.builder().id(event.getLoanId()).build();

                try {
                    loanStatusService.approveLoan(loanId);

                    // Checkpoint the event to mark it as processed
                    eventContext.updateCheckpoint();
                } catch (RuntimeException e) {
                    log.error("Error processing loan event", e);
                    // TODO error logic
                }
            }
        } catch (Exception e) {
            log.error("Error processing event", e);
        }
    }

    private LoanStateChangedAvro deserializeAvroEvent(byte[] data) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(inputStream, null);
        DatumReader<LoanStateChangedAvro> reader = new SpecificDatumReader<>(LoanStateChangedAvro.class);

        return reader.read(null, decoder);
    }
}
