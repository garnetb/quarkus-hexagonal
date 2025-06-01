package com.microservice.quarkus.infrastructure.db.cosmosdb.config;

import java.time.Instant;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.ThroughputProperties;
import com.microservice.quarkus.domain.model.loan.LoanState;
import com.microservice.quarkus.infrastructure.db.cosmosdb.dbo.LoanEntity;
import io.quarkus.arc.profile.UnlessBuildProfile;
import io.quarkus.runtime.StartupEvent;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@UnlessBuildProfile("prod")
@Slf4j
public class DevBootstrap {
    
    @ConfigProperty(name = "azure.cosmos.database")
    String databaseName;
    
    @Inject
    CosmosClient cosmosClient;
    
    void onStart(@Observes StartupEvent ev) {
        log.info("Bootstrapping Cosmos DB database");
        
        // Create database if it doesn't exist
        CosmosDatabaseResponse databaseResponse = cosmosClient.createDatabaseIfNotExists(databaseName);
        CosmosDatabase database = cosmosClient.getDatabase(databaseName);
        log.info("Database {} created or exists", databaseName);
        
        // Create container if it doesn't exist
        CosmosContainerProperties containerProperties = new CosmosContainerProperties(
                LoanEntity.CONTAINER_NAME, 
                LoanEntity.PARTITION_KEY_PATH);
        
        CosmosContainerResponse containerResponse = database.createContainerIfNotExists(
                containerProperties, 
                ThroughputProperties.createManualThroughput(400));
        
        CosmosContainer container = database.getContainer(LoanEntity.CONTAINER_NAME);
        log.info("Container {} created or exists", LoanEntity.CONTAINER_NAME);
        
        // Populate container with sample data
        populateContainer(container);
    }
    
    private void populateContainer(CosmosContainer container) {
        log.info("Populating Cosmos DB container {}", container.getId());
        
        LoanEntity loan1 = LoanEntity.builder()
                .id(UUID.randomUUID().toString())
                .annualInterestRate(2.15)
                .loanAmount(130000L)
                .numberOfYears(30)
                .loanDate(Instant.now())
                .userId(UUID.randomUUID().toString())
                .state(LoanState.PENDING.value())
                .build();
        
        LoanEntity loan2 = LoanEntity.builder()
                .id(UUID.randomUUID().toString())
                .annualInterestRate(2.15)
                .loanAmount(45000L)
                .numberOfYears(15)
                .loanDate(Instant.now())
                .userId(UUID.randomUUID().toString())
                .state(LoanState.PENDING.value())
                .build();
        
        CosmosItemResponse<LoanEntity> response1 = container.createItem(loan1);
        log.info("Created loan item with id {}, request charge: {}", 
                loan1.getId(), response1.getRequestCharge());
        
        CosmosItemResponse<LoanEntity> response2 = container.createItem(loan2);
        log.info("Created loan item with id {}, request charge: {}", 
                loan2.getId(), response2.getRequestCharge());
    }
}
