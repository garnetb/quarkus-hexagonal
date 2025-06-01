package com.microservice.quarkus.infrastructure.db.cosmosdb.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.microservice.quarkus.domain.model.loan.Loan;
import com.microservice.quarkus.domain.ports.spi.LoanRepository;
import com.microservice.quarkus.infrastructure.db.cosmosdb.dbo.LoanEntity;
import com.microservice.quarkus.infrastructure.db.cosmosdb.exceptions.DboException;
import com.microservice.quarkus.infrastructure.db.cosmosdb.mapper.LoanEntityMapper;
import lombok.AllArgsConstructor;

@ApplicationScoped
@AllArgsConstructor
public class LoanDboRepository implements LoanRepository {

    private final CosmosContainer loansContainer;
    private final LoanEntityMapper loanMapper;

    @Override
    public Loan findById(String id) {
        try {
            CosmosItemResponse<LoanEntity> response = loansContainer.readItem(
                    id, 
                    new PartitionKey(id), 
                    LoanEntity.class);

            return loanMapper.toDomain(response.getItem());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<Loan> getAll() {
        CosmosPagedIterable<LoanEntity> response = loansContainer.queryItems(
                "SELECT * FROM c", new CosmosQueryRequestOptions(), LoanEntity.class);

        List<Loan> loans = new ArrayList<>();
        response.forEach(entity -> loans.add(loanMapper.toDomain(entity)));

        return loans;
    }

    @Override
    public void save(Loan loan) {
        LoanEntity entity = loanMapper.toDbo(loan);

        CosmosItemResponse<LoanEntity> response = loansContainer.createItem(entity);

        if (response.getStatusCode() >= 400) {
            throw new DboException("Failed to save loan: %s", response.getStatusCode());
        }
    }

    @Override
    public void update(Loan loan) {
        String id = loan.getId().getId();

        try {
            // Read the existing item
            CosmosItemResponse<LoanEntity> readResponse = loansContainer.readItem(
                    id, 
                    new PartitionKey(id), 
                    LoanEntity.class);

            LoanEntity existingEntity = readResponse.getItem();

            // Update the entity
            loanMapper.updateEntityFromDomain(loan, existingEntity);

            // Replace the item
            CosmosItemResponse<LoanEntity> updateResponse = loansContainer.replaceItem(
                    existingEntity, 
                    id, 
                    new PartitionKey(id), 
                    new CosmosItemRequestOptions());

            if (updateResponse.getStatusCode() >= 400) {
                throw new DboException("Failed to update loan: %s", updateResponse.getStatusCode());
            }
        } catch (Exception e) {
            throw new DboException("No Loan found for Id [%s]", loan.getId());
        }
    }

    @Override
    public void delete(String id) {
        try {
            CosmosItemResponse<Object> response = loansContainer.deleteItem(
                    id, 
                    new PartitionKey(id), 
                    new CosmosItemRequestOptions());

            if (response.getStatusCode() >= 400) {
                throw new DboException("Failed to delete loan: %s", response.getStatusCode());
            }
        } catch (Exception e) {
            throw new DboException("Failed to delete loan with id [%s]: %s", id, e.getMessage());
        }
    }

    @Override
    public String nextLoanId() {
        return UUID.randomUUID().toString();
    }
}
