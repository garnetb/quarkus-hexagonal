package com.microservice.quarkus.infrastructure.db.cosmosdb.dbo;

import java.time.Instant;

import com.azure.cosmos.models.PartitionKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanEntity {
    public static final String CONTAINER_NAME = "loans";
    public static final String PARTITION_KEY_PATH = "/id";
    
    private String id;
    
    private Integer numberOfYears;
    
    private Long loanAmount;
    
    private Double annualInterestRate;
    
    private Instant loanDate;
    
    private String userId;
    
    private String state;
    
    public PartitionKey getPartitionKey() {
        return new PartitionKey(id);
    }
}
