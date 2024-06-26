package com.ledgerlogic.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Journal {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "journal_seq")
    @SequenceGenerator(name = "journal_seq", sequenceName = "journal_seq", allocationSize = 1)
    private Long             journalId;
    private Status           status = Status.PENDING;
    private String           rejectionReason;
    private BigDecimal       balance;
    @Lob
    private byte[]           attachedFile;
    @Transient
    private MultipartFile attachedFileMultipart;
    @Setter
    private String           attachedFileContentType;
    private Date             createdDate;
    private Date             transactionDate;

    @ManyToOne
    private User             createdBy;

    @OneToMany(mappedBy = "journal")
    private List<JournalEntry> journalEntries;

    public Journal(String rejectionReason, byte[] attachedFile, Date createdDate, User createdBy, List<JournalEntry> journalEntries){
        this.rejectionReason = rejectionReason;
        this.attachedFile     = attachedFile;
        this.createdDate     = createdDate;
        this.createdBy       = createdBy;
        this.journalEntries  = journalEntries;
    }

    public Journal(String rejectionReason, byte[] attachedFile, Date createdDate, User createdBy) {
        this.rejectionReason = rejectionReason;
        this.attachedFile = attachedFile;
        this.createdDate = createdDate;
        this.createdBy = createdBy;
    }

    public enum Status {
        PENDING, APPROVED, REJECTED
    }

    @Override
    public String toString() {
        return "Journal{" +
                "journalId=" + journalId +
                ", status=" + status +
                ", rejectionReason='" + rejectionReason + '\'' +
                ", balance=" + balance +
                ", createdDate=" + createdDate +
                '}';
    }
}
