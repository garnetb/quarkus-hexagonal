package com.microservice.quarkus.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.microservice.quarkus.application.ports.commands.LoanStatusService;
import com.microservice.quarkus.domain.model.loan.Loan;
import com.microservice.quarkus.domain.model.loan.LoanId;
import com.microservice.quarkus.domain.ports.spi.EventBus;
import com.microservice.quarkus.domain.ports.spi.LoanRepository;
import io.quarkus.arc.profile.IfBuildProfile;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@IfBuildProfile("prod")
@Slf4j
public class LoanStatusServiceImpl implements LoanStatusService {

    private final LoanRepository loanRepository;
    private final EventBus eventBus;

    @Inject
    public LoanStatusServiceImpl(LoanRepository loanRepository, EventBus eventBus) {
        this.loanRepository = loanRepository;
        this.eventBus = eventBus;
    }

    @Override
    public void approveLoan(LoanId loanId) {
        log.info("Approving loan with ID: {}", loanId.getId());

        Loan loan = loanRepository.findById(loanId.getId());
        if (loan == null) {
            throw new IllegalArgumentException("Loan not found with ID: " + loanId.getId());
        }

        loan.approveLoan();
        loanRepository.save(loan);
        eventBus.publish(loan.domainEvents());
    }

    @Override
    public void rejectLoan(LoanId loanId) {
        log.info("Rejecting loan with ID: {}", loanId.getId());

        Loan loan = loanRepository.findById(loanId.getId());
        if (loan == null) {
            throw new IllegalArgumentException("Loan not found with ID: " + loanId.getId());
        }

        loan.rejectLoan();
        loanRepository.save(loan);
        eventBus.publish(loan.domainEvents());
    }
}
