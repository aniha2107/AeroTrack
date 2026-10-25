package com.example.baggage.service;

import com.example.baggage.model.BaggageRecord;
import com.example.baggage.model.BaggageStatus;
import com.example.baggage.model.RecordOrigin;
import com.example.baggage.model.RecoveryStatus;
import com.example.baggage.model.VerifiableAttribute;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/** Form-backing object for creating/editing a record (administrators only). */
public class AdminForm {

    private Long id;
    private String tagId;
    private String caseReference;
    private BaggageStatus status;
    private String flightNumber;
    private String originAirport;
    private String destinationAirport;
    private String lastRecordedAirport;
    private String lastCheckpoint;
    private String description;
    private String distinctiveMarks;
    private RecoveryStatus recoveryStatus;
    private String externalRecordUrl;
    private String source;
    private RecordOrigin recordOrigin = RecordOrigin.MANUAL_ENTRY;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateLastChecked;
    private Set<VerifiableAttribute> verifiedAttributes = new HashSet<>();

    public static AdminForm from(BaggageRecord r) {
        AdminForm f = new AdminForm();
        f.id = r.getId();
        f.tagId = r.getTagId();
        f.caseReference = r.getCaseReference();
        f.status = r.getStatus();
        f.flightNumber = r.getFlightNumber();
        f.originAirport = r.getOriginAirport();
        f.destinationAirport = r.getDestinationAirport();
        f.lastRecordedAirport = r.getLastRecordedAirport();
        f.lastCheckpoint = r.getLastCheckpoint();
        f.description = r.getDescription();
        f.distinctiveMarks = r.getDistinctiveMarks();
        f.recoveryStatus = r.getRecoveryStatus();
        f.externalRecordUrl = r.getExternalRecordUrl();
        f.source = r.getSource();
        f.recordOrigin = r.getRecordOrigin();
        f.dateLastChecked = r.getDateLastChecked();
        f.verifiedAttributes = new HashSet<>(r.getVerifiedAttributes());
        return f;
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
    public Set<VerifiableAttribute> getVerifiedAttributes() { return verifiedAttributes; }
    public void setVerifiedAttributes(Set<VerifiableAttribute> verifiedAttributes) { this.verifiedAttributes = verifiedAttributes; }
}
