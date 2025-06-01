package com.microservice.quarkus.infrastructure.rest;

import jakarta.enterprise.context.ApplicationScoped;

import com.microservice.quarkus.application.ports.commands.LoanStatusService;
import com.microservice.quarkus.domain.model.loan.LoanId;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class MockLoanStatusService implements LoanStatusService {

    @Override
    public void approveLoan(LoanId loanId) {
        log.info("Mock approving loan with ID: {}", loanId.getId());
        // This is a mock implementation that doesn't do anything
    }

    @Override
    public void rejectLoan(LoanId loanId) {
        log.info("Mock rejecting loan with ID: {}", loanId.getId());
        // This is a mock implementation that doesn't do anything
    }
}
