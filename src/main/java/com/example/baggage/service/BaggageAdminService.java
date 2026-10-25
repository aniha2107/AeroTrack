package com.example.baggage.service;

import com.example.baggage.model.BaggageRecord;
import com.example.baggage.model.RecordOrigin;
import com.example.baggage.model.VerifiableAttribute;
import com.example.baggage.model.VerificationStatus;
import com.example.baggage.repo.BaggageRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * Administrator operations: manual entry, and verifying/adding attributes the source did not provide.
 * A verified attribute must actually have a value - an administrator cannot "verify" Not specified.
 */
@Service
public class BaggageAdminService {

    private final BaggageRepository repo;
    private final OfficialUrlValidator urls;

    public BaggageAdminService(BaggageRepository repo, OfficialUrlValidator urls) {
        this.repo = repo;
        this.urls = urls;
    }

    @Transactional(readOnly = true)
    public List<BaggageRecord> all() {
        return repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    @Transactional(readOnly = true)
    public Optional<BaggageRecord> find(Long id) {
        return repo.findById(id);
    }

    /** Returns human-readable problems; empty means the form can be saved. */
    @Transactional(readOnly = true)
    public List<String> validate(AdminForm f) {
        List<String> errors = new ArrayList<>();
        BaggageRecord probe = new BaggageRecord();
        collect(errors, () -> probe.setTagId(FieldRules.id(f.getTagId(), "Tag ID")));
        collect(errors, () -> probe.setCaseReference(FieldRules.id(f.getCaseReference(), "Case reference")));
        collect(errors, () -> probe.setFlightNumber(FieldRules.flight(f.getFlightNumber(), "Flight number")));
        collect(errors, () -> probe.setOriginAirport(FieldRules.airport(f.getOriginAirport(), "Origin")));
        collect(errors, () -> probe.setDestinationAirport(FieldRules.airport(f.getDestinationAirport(), "Destination")));
        collect(errors, () -> probe.setLastRecordedAirport(FieldRules.airport(f.getLastRecordedAirport(), "Last recorded airport")));
        collect(errors, () -> probe.setLastCheckpoint(FieldRules.text(f.getLastCheckpoint(), 200, "Last checkpoint")));
        collect(errors, () -> probe.setDescription(FieldRules.text(f.getDescription(), 1000, "Description")));
        collect(errors, () -> probe.setDistinctiveMarks(FieldRules.text(f.getDistinctiveMarks(), 1000, "Distinctive marks")));
        collect(errors, () -> probe.setSource(FieldRules.text(f.getSource(), 500, "Source")));

        if (errors.isEmpty() && probe.getTagId() == null && probe.getCaseReference() == null) {
            errors.add("Enter a baggage tag ID or a missing-baggage case reference.");
        }
        if (probe.getSource() == null && errors.stream().noneMatch(s -> s.startsWith("Source"))) {
            errors.add("Source is required: say which airline/airport information or permitted export this came from.");
        }
        String url = FieldRules.clean(f.getExternalRecordUrl());
        if (url != null && !urls.isValid(url)) {
            errors.add("The official tracking URL must be a full https:// link (and on an allowed host, if an allow-list is configured).");
        }
        if (f.getDateLastChecked() != null && f.getDateLastChecked().isAfter(LocalDate.now())) {
            errors.add("Date last checked cannot be in the future.");
        }
        if (f.getRecordOrigin() == null || f.getRecordOrigin() == RecordOrigin.AUTHORIZED_API && f.getId() == null) {
            errors.add("Choose how this record was obtained (manual entry or authorized export).");
        }
        if (errors.isEmpty()) {
            checkDuplicate(errors, "tag ID", probe.getTagId(), f.getId(), repo.findFirstByTagId(nullToEmpty(probe.getTagId())));
            checkDuplicate(errors, "case reference", probe.getCaseReference(), f.getId(), repo.findFirstByCaseReference(nullToEmpty(probe.getCaseReference())));
            for (VerifiableAttribute a : f.getVerifiedAttributes()) {
                if (!hasValue(a, f, probe)) {
                    errors.add("\"" + a.getLabel() + "\" cannot be marked verified while it is Not specified.");
                }
            }
        }
        return errors;
    }

    @Transactional
    public BaggageRecord save(AdminForm f, String adminName) {
        List<String> problems = validate(f);
        if (!problems.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", problems));
        }
        BaggageRecord r = f.getId() == null ? new BaggageRecord()
                : repo.findById(f.getId()).orElseThrow(() -> new IllegalArgumentException("Record not found."));
        r.setTagId(FieldRules.id(f.getTagId(), "Tag ID"));
        r.setCaseReference(FieldRules.id(f.getCaseReference(), "Case reference"));
        r.setStatus(f.getStatus());
        r.setFlightNumber(FieldRules.flight(f.getFlightNumber(), "Flight number"));
        r.setOriginAirport(FieldRules.airport(f.getOriginAirport(), "Origin"));
        r.setDestinationAirport(FieldRules.airport(f.getDestinationAirport(), "Destination"));
        r.setLastRecordedAirport(FieldRules.airport(f.getLastRecordedAirport(), "Last recorded airport"));
        r.setLastCheckpoint(FieldRules.text(f.getLastCheckpoint(), 200, "Last checkpoint"));
        r.setDescription(FieldRules.text(f.getDescription(), 1000, "Description"));
        r.setDistinctiveMarks(FieldRules.text(f.getDistinctiveMarks(), 1000, "Distinctive marks"));
        r.setRecoveryStatus(f.getRecoveryStatus());
        String url = FieldRules.clean(f.getExternalRecordUrl());
        r.setExternalRecordUrl(url);
        r.setSource(FieldRules.text(f.getSource(), 500, "Source"));
        if (r.getId() == null || r.getRecordOrigin() != RecordOrigin.DEMO) {
            r.setRecordOrigin(f.getRecordOrigin());
        }
        r.setDateLastChecked(f.getDateLastChecked() == null ? LocalDate.now() : f.getDateLastChecked());

        r.setVerifiedAttributes(new HashSet<>(f.getVerifiedAttributes()));
        if (f.getVerifiedAttributes().isEmpty()) {
            r.setVerificationStatus(VerificationStatus.UNVERIFIED);
            r.setVerifiedBy(null);
            r.setVerifiedAt(null);
        } else {
            r.setVerificationStatus(VerificationStatus.ADMIN_VERIFIED);
            r.setVerifiedBy(adminName);
            r.setVerifiedAt(Instant.now());
        }
        return repo.save(r);
    }

    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }

    @Transactional
    public int deleteDemoRecords() {
        return repo.deleteDemoRecords();
    }

    private static boolean hasValue(VerifiableAttribute a, AdminForm f, BaggageRecord p) {
        return switch (a) {
            case STATUS -> f.getStatus() != null;
            case LOCATION -> p.getLastRecordedAirport() != null || p.getLastCheckpoint() != null;
            case FLIGHT -> p.getFlightNumber() != null || p.getOriginAirport() != null || p.getDestinationAirport() != null;
            case DESCRIPTION -> p.getDescription() != null || p.getDistinctiveMarks() != null;
            case RECOVERY -> f.getRecoveryStatus() != null;
        };
    }

    private static void checkDuplicate(List<String> errors, String what, String value, Long selfId,
                                       Optional<BaggageRecord> existing) {
        if (value != null && existing.isPresent() && !existing.get().getId().equals(selfId)) {
            errors.add("Another record already uses this " + what + ".");
        }
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static void collect(List<String> errors, Runnable r) {
        try {
            r.run();
        } catch (IllegalArgumentException ex) {
            errors.add(ex.getMessage());
        }
    }
}
