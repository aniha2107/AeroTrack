package com.example.baggage.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * One baggage record. Every descriptive attribute is nullable on purpose: a null means
 * "Not specified" - the source did not explicitly confirm it - and a null attribute never
 * matches a search filter. No passenger names or contact details are stored.
 */
@Entity
@Table(name = "baggage_record", indexes = {
        @Index(name = "idx_bag_tag", columnList = "tagId"),
        @Index(name = "idx_bag_case", columnList = "caseReference"),
        @Index(name = "idx_bag_status", columnList = "status")
})
public class BaggageRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 40)
    private String tagId;

    @Column(length = 40)
    private String caseReference;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private BaggageStatus status;

    @Column(length = 8)
    private String flightNumber;

    @Column(length = 3)
    private String originAirport;

    @Column(length = 3)
    private String destinationAirport;

    @Column(length = 3)
    private String lastRecordedAirport;

    @Column(length = 200)
    private String lastCheckpoint;

    @Column(length = 1000)
    private String description;

    @Column(length = 1000)
    private String distinctiveMarks;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private RecoveryStatus recoveryStatus;

    /** Link to the official tracking page, stored exactly as supplied/approved. */
    @Column(length = 2000)
    private String externalRecordUrl;

    /** Where the information came from (airline/airport name and the permission or export it was taken from). */
    @Column(nullable = false, length = 500)
    private String source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RecordOrigin recordOrigin;

    private LocalDate dateLastChecked;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VerificationStatus verificationStatus = VerificationStatus.UNVERIFIED;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "baggage_verified_attribute", joinColumns = @JoinColumn(name = "baggage_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "attribute", length = 30)
    private Set<VerifiableAttribute> verifiedAttributes = new HashSet<>();

    @Column(length = 100)
    private String verifiedBy;

    private Instant verifiedAt;

    public boolean isDemo() {
        return recordOrigin == RecordOrigin.DEMO;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTagId() { return tagId; }
    public void setTagId(String tagId) { this.tagId = tagId; }
    public String getCaseReference() { return caseReference; }
    public void setCaseReference(String caseReference) { this.caseReference = caseReference; }
    public BaggageStatus getStatus() { return status; }
    public void setStatus(BaggageStatus status) { this.status = status; }
    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
    public String getOriginAirport() { return originAirport; }
    public void setOriginAirport(String originAirport) { this.originAirport = originAirport; }
    public String getDestinationAirport() { return destinationAirport; }
    public void setDestinationAirport(String destinationAirport) { this.destinationAirport = destinationAirport; }
    public String getLastRecordedAirport() { return lastRecordedAirport; }
    public void setLastRecordedAirport(String lastRecordedAirport) { this.lastRecordedAirport = lastRecordedAirport; }
    public String getLastCheckpoint() { return lastCheckpoint; }
    public void setLastCheckpoint(String lastCheckpoint) { this.lastCheckpoint = lastCheckpoint; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDistinctiveMarks() { return distinctiveMarks; }
    public void setDistinctiveMarks(String distinctiveMarks) { this.distinctiveMarks = distinctiveMarks; }
    public RecoveryStatus getRecoveryStatus() { return recoveryStatus; }
    public void setRecoveryStatus(RecoveryStatus recoveryStatus) { this.recoveryStatus = recoveryStatus; }
    public String getExternalRecordUrl() { return externalRecordUrl; }
    public void setExternalRecordUrl(String externalRecordUrl) { this.externalRecordUrl = externalRecordUrl; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public RecordOrigin getRecordOrigin() { return recordOrigin; }
    public void setRecordOrigin(RecordOrigin recordOrigin) { this.recordOrigin = recordOrigin; }
    public LocalDate getDateLastChecked() { return dateLastChecked; }
    public void setDateLastChecked(LocalDate dateLastChecked) { this.dateLastChecked = dateLastChecked; }
    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }
    public Set<VerifiableAttribute> getVerifiedAttributes() { return verifiedAttributes; }
    public void setVerifiedAttributes(Set<VerifiableAttribute> verifiedAttributes) { this.verifiedAttributes = verifiedAttributes; }
    public String getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }
    public Instant getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(Instant verifiedAt) { this.verifiedAt = verifiedAt; }
}
