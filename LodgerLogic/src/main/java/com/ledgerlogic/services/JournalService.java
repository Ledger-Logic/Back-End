package com.ledgerlogic.services;

import com.ledgerlogic.dtos.JournalDTO;
import com.ledgerlogic.models.*;
import com.ledgerlogic.repositories.JournalEntryRepository;
import com.ledgerlogic.repositories.JournalRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JournalService {
    private JournalRepository   journalRepository;
    private AccountService      accountService;
    private EventLogService     eventLogService;

    private EmailService        emailService;

    private JournalEntryRepository journalEntryRepository;

    public JournalService(JournalRepository journalRepository,
                          EventLogService eventLogService,
                          AccountService accountService,
                          EmailService emailService,
                          JournalEntryRepository journalEntryRepository) {
        this.journalRepository = journalRepository;
        this.accountService = accountService;
        this.eventLogService = eventLogService;
        this.emailService = emailService;
        this.journalEntryRepository = journalEntryRepository;
    }

    public Journal addJournal(Journal journal, MultipartFile attachedFile, String attachedFileContentType, Long userId) {
        List<JournalEntry> journalEntries = journal.getJournalEntries();
        if (journalEntries != null) {
            for (JournalEntry entry : journalEntries) {
                entry.setJournal(journal);
                entry.setDescription(entry.getDescription());
                entry.setTransactionDate(journal.getTransactionDate());
            }

            journal.setJournalEntries(journalEntries);
        }

        EventLog userEventLog = new EventLog("Added new Journal", journal.getJournalId(), userId, LocalDateTime.now(), journal.toString(), null);
        this.eventLogService.saveEventLog(userEventLog);

        this.emailService.send("bw@gmail.com", "autoprocess@ledgerlogic.com", "New Journal Created", "New Journal is created by user with ID: " + userId);
        if (attachedFile != null && !attachedFile.isEmpty()) {
            try {
                journal.setAttachedFile(attachedFile.getBytes());
                journal.setAttachedFileContentType(attachedFileContentType);
                journal.setAttachedFileMultipart(attachedFile);
            } catch (IOException e) {
                throw new RuntimeException("Failed to store attached file", e);
            }
        }

        journal.setCreatedDate(new Date());
        Journal savedJournal = this.journalRepository.save(journal);
        this.journalEntryRepository.saveAll(journalEntries);
        return savedJournal;
    }


    public Journal approveJournal(Long id, Journal.Status newStatus) {
        Optional<Journal> optionalJournal = this.journalRepository.findById(id);

        if (optionalJournal.isPresent()) {
            Journal journal = optionalJournal.get();
            Journal previousJournalState = new Journal(journal.getRejectionReason(), journal.getAttachedFile(),
                    journal.getCreatedDate(), journal.getCreatedBy(), journal.getJournalEntries());
            previousJournalState.setStatus(journal.getStatus());

            String previousJournalEntriesDescription = journal.getJournalEntries().stream()
                    .map(entry -> "Account: " + entry.getAccount().getAccountName() +
                            ", Debit: " + entry.getDebit() +
                            ", Credit: " + entry.getCredit())
                    .collect(Collectors.joining("; "));

            if (newStatus.equals(Journal.Status.APPROVED)) {
                journal.setStatus(Journal.Status.APPROVED);

                List<JournalEntry> journalEntries = journal.getJournalEntries();
                Map<Long, Account> accountsToUpdate = new HashMap<>();

                if (journalEntries != null) {
                    for (JournalEntry journalEntry : journalEntries) {
                        journalEntry.setStatus("approved");

                        Account accountToUpdate = journalEntry.getAccount();
                        accountsToUpdate.put(accountToUpdate.getAccountId(), accountToUpdate);

                        BigDecimal debit = journalEntry.getDebit();
                        BigDecimal credit = journalEntry.getCredit();

                        updateAccountBalance(accountToUpdate, debit, credit);
                    }
                }

                Map<Long, Account> updatedAccounts = new HashMap<>();
                for (Account accountToUpdate : accountsToUpdate.values()) {
                    Account previousAccountState = new Account(accountToUpdate.getAccountNumber(),
                            accountToUpdate.getAccountName(), accountToUpdate.getDescription(),
                            accountToUpdate.getNormalSide(), accountToUpdate.getCategory());
                    previousAccountState.setDebit(accountToUpdate.getDebit().subtract(journalEntries.stream()
                            .filter(entry -> entry.getAccount().equals(accountToUpdate))
                            .map(JournalEntry::getDebit)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)));
                    previousAccountState.setCredit(accountToUpdate.getCredit().subtract(journalEntries.stream()
                            .filter(entry -> entry.getAccount().equals(accountToUpdate))
                            .map(JournalEntry::getCredit)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)));
                    previousAccountState.setBalance(previousAccountState.getDebit().subtract(previousAccountState.getCredit()));
                    previousAccountState.setInitialBalance(accountToUpdate.getInitialBalance());

                    Account updatedAccount = this.accountService.update(accountToUpdate.getAccountId(), accountToUpdate);
                    updatedAccounts.put(accountToUpdate.getAccountId(), updatedAccount);
                }

                String journalEntriesDescription = journalEntries.stream()
                        .map(entry -> "Account: " + updatedAccounts.get(entry.getAccount().getAccountId()).getAccountName() +
                                ", Debit: " + entry.getDebit() +
                                ", Credit: " + entry.getCredit())
                        .collect(Collectors.joining("; "));

                EventLog journalEventLog = new EventLog("Approved New Journal", journal.getJournalId(),
                        journal.getCreatedBy().getUserId(), LocalDateTime.now(),
                        journal.toString() + ", Entries: [" + journalEntriesDescription + "]",
                        previousJournalState.toString() + ", Entries: [" + previousJournalEntriesDescription + "]");
                this.eventLogService.saveEventLog(journalEventLog);

                return this.journalRepository.save(journal);
            }
        }

        return null;
    }

    private void updateAccountBalance(Account account, BigDecimal debit, BigDecimal credit) {
        String category = account.getCategory().toLowerCase().trim();
        String subcategory = account.getSubCategory().toLowerCase().trim();

        BigDecimal currentBalance = account.getBalance();
        BigDecimal newBalance = BigDecimal.ZERO;

        if (category.contains("asset")) {
            newBalance = currentBalance.add(debit).subtract(credit);
        } else if (category.contains("liability") || category.contains("equity")) {
            newBalance = currentBalance.subtract(debit).add(credit);
        } else if (category.contains("revenue")) {
            newBalance = currentBalance.add(credit).subtract(debit);
        } else if (category.contains("expense")) {
            newBalance = currentBalance.subtract(credit).add(debit);
        }

        if (category.contains("asset") || category.contains("expense")) {
            account.setDebit(account.getDebit().add(debit));
            account.setCredit(account.getCredit().add(credit));
        } else if (category.contains("liability") || category.contains("equity") || category.contains("revenue")) {
            account.setDebit(account.getDebit().subtract(debit));
            account.setCredit(account.getCredit().add(credit));
        }

        account.setBalance(newBalance);
    }


    public Journal rejectJournal(JournalDTO journalDTO) {
        if (journalDTO != null && journalDTO.getJournalId() != null) {
            Optional<Journal> optionalJournal = this.journalRepository.findById(journalDTO.getJournalId());
            if (optionalJournal.isPresent()) {
                Journal existingJournal = optionalJournal.get();
                Journal previousState = new Journal(
                        existingJournal.getRejectionReason(),
                        existingJournal.getAttachedFile(),
                        existingJournal.getCreatedDate(),
                        existingJournal.getCreatedBy(),
                        new ArrayList<>(existingJournal.getJournalEntries())
                );
                previousState.setStatus(existingJournal.getStatus());

                existingJournal.setStatus(Journal.Status.REJECTED);
                existingJournal.setRejectionReason(journalDTO.getRejectionReason());

                List<JournalEntry> journalEntries = existingJournal.getJournalEntries();
                if (journalEntries != null) {
                    for (JournalEntry journalEntry : journalEntries) {
                        journalEntry.setStatus("rejected");
                        journalEntry.setRejectionReason(journalDTO.getRejectionReason());
                    }
                }

                EventLog userEventLog = new EventLog(
                        "Rejected Journal",
                        existingJournal.getJournalId(),
                        existingJournal.getCreatedBy().getUserId(),
                        LocalDateTime.now(),
                        existingJournal.toString(),
                        previousState.toString()
                );
                this.eventLogService.saveEventLog(userEventLog);

                return this.journalRepository.save(existingJournal);
            }
        }
        return null;
    }

    public List<Journal> getAllJournals(){
        return this.journalRepository.findAll();
    }

    public List<Journal> getByStatus(Journal.Status status){
        return this.journalRepository.findByStatus(status);
    }

    public List<Journal> getByDate(Date date){
        return this.journalRepository.findByCreatedDate(date);
    }

}
