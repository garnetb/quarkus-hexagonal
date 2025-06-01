package com.microservice.quarkus.infrastructure.db.cosmosdb.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.microservice.quarkus.infrastructure.db.cosmosdb.dbo.LoanEntity;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class CosmosDBConfig {

    @ConfigProperty(name = "azure.cosmos.endpoint")
    String endpoint;

    @ConfigProperty(name = "azure.cosmos.key")
    String key;

    @ConfigProperty(name = "azure.cosmos.database")
    String databaseName;

    @Produces
    @ApplicationScoped
    public CosmosClient cosmosClient() {
        return new CosmosClientBuilder()
                .endpoint(endpoint)
                .key(key)
                .buildClient();
    }

    @Produces
    @ApplicationScoped
    public CosmosDatabase cosmosDatabase(CosmosClient client) {
        return client.getDatabase(databaseName);
    }

    @Produces
    @ApplicationScoped
    public CosmosContainer loansContainer(CosmosDatabase database) {
        return database.getContainer(LoanEntity.CONTAINER_NAME);
    }
}
