package com.microservice.quarkus.infrastructure.events.eventhubs.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class EventHubsConfig {

    @ConfigProperty(name = "azure.eventhubs.connection-string")
    String eventHubsConnectionString;

    @ConfigProperty(name = "azure.eventhubs.name")
    String eventHubsName;

    @ConfigProperty(name = "azure.eventhubs.consumer-group")
    String consumerGroup;

    @ConfigProperty(name = "azure.storage.connection-string")
    String storageConnectionString;

    @ConfigProperty(name = "azure.storage.container-name")
    String containerName;

    @Produces
    @ApplicationScoped
    public EventHubProducerClient eventHubProducerClient() {
        return new EventHubClientBuilder()
                .connectionString(eventHubsConnectionString, eventHubsName)
                .buildProducerClient();
    }

    @Produces
    @ApplicationScoped
    public EventHubConsumerClient eventHubConsumerClient() {
        return new EventHubClientBuilder()
                .connectionString(eventHubsConnectionString, eventHubsName)
                .consumerGroup(consumerGroup)
                .buildConsumerClient();
    }

    @Produces
    @ApplicationScoped
    public BlobContainerAsyncClient blobContainerAsyncClient() {
        return new BlobContainerClientBuilder()
                .connectionString(storageConnectionString)
                .containerName(containerName)
                .buildAsyncClient();
    }

    @Produces
    @ApplicationScoped
    public EventProcessorClientBuilder eventProcessorClient(BlobContainerAsyncClient blobContainerAsyncClient) {
        return new EventProcessorClientBuilder()
                .connectionString(eventHubsConnectionString, eventHubsName)
                .consumerGroup(consumerGroup)
                .checkpointStore(new BlobCheckpointStore(blobContainerAsyncClient));

    }
}
