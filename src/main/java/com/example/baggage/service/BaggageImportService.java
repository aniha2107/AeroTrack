package com.example.baggage.service;

import com.example.baggage.model.BaggageRecord;
import com.example.baggage.model.BaggageStatus;
import com.example.baggage.model.RecordOrigin;
import com.example.baggage.model.RecoveryStatus;
import com.example.baggage.model.VerificationStatus;
import com.example.baggage.repo.BaggageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Imports records from an authorized export (CSV) or an {@link AuthorizedBaggageSource}.
 * Values are copied as supplied: missing values stay null ("Not specified"), unrecognised values
 * are rejected rather than guessed, and imported records start as UNVERIFIED.
 */
@Service
public class BaggageImportService {

    public static final List<String> CSV_COLUMNS = List.of(
            "tag_id", "case_reference", "status", "flight_number", "origin", "destination",
            "last_airport", "last_checkpoint", "description", "distinctive_marks",
            "recovery_status", "external_url");

    public record ImportResult(int imported, int skippedDuplicates, List<String> errors, List<String> ignoredColumns) {
    }

    private final BaggageRepository repo;
    private final OfficialUrlValidator urls;

    public BaggageImportService(BaggageRepository repo, OfficialUrlValidator urls) {
        this.repo = repo;
        this.urls = urls;
    }

    @Transactional
    public ImportResult importCsv(String csvText, String source, LocalDate checkedOn) {
        List<List<String>> rows = CsvParser.parse(csvText);
        if (rows.isEmpty()) {
            return new ImportResult(0, 0, List.of("The file is empty."), List.of());
        }
        Map<String, Integer> col = new HashMap<>();
        List<String> ignored = new ArrayList<>();
        List<String> header = rows.get(0);
        for (int i = 0; i < header.size(); i++) {
            String name = header.get(i).trim().toLowerCase(Locale.ROOT).replace(' ', '_');
            if (CSV_COLUMNS.contains(name)) {
                col.put(name, i);
            } else if (!name.isEmpty()) {
                ignored.add(header.get(i).trim());
            }
        }
        if (!col.containsKey("tag_id") && !col.containsKey("case_reference")) {
            return new ImportResult(0, 0,
                    List.of("The header row needs a tag_id or case_reference column. Expected columns: "
                            + String.join(", ", CSV_COLUMNS)), ignored);
        }
        List<ExternalBaggageRecord> records = new ArrayList<>();
        for (int r = 1; r < rows.size(); r++) {
            List<String> row = rows.get(r);
            records.add(new ExternalBaggageRecord(
                    cell(row, col, "tag_id"), cell(row, col, "case_reference"), cell(row, col, "status"),
                    cell(row, col, "flight_number"), cell(row, col, "origin"), cell(row, col, "destination"),
                    cell(row, col, "last_airport"), cell(row, col, "last_checkpoint"),
                    cell(row, col, "description"), cell(row, col, "distinctive_marks"),
                    cell(row, col, "recovery_status"), cell(row, col, "external_url")));
        }
        return importRecords(records, RecordOrigin.AUTHORIZED_EXPORT, source, checkedOn, ignored);
    }

    @Transactional
    public ImportResult importFromSource(AuthorizedBaggageSource src) {
        return importRecords(src.fetch(), RecordOrigin.AUTHORIZED_API, src.sourceName(), LocalDate.now(), List.of());
    }

    @Transactional
    public ImportResult importRecords(List<ExternalBaggageRecord> records, RecordOrigin origin, String source,
                                      LocalDate checkedOn, List<String> ignoredColumns) {
        String src = FieldRules.clean(source);
        if (src == null) {
            return new ImportResult(0, 0, List.of("A source description is required."), ignoredColumns);
        }
        int imported = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < records.size(); i++) {
            String where = "Record " + (i + 1);
            try {
                BaggageRecord entity = map(records.get(i), origin, src, checkedOn);
                if (isDuplicate(entity)) {
                    skipped++;
                    continue;
                }
                repo.save(entity);
                imported++;
            } catch (IllegalArgumentException ex) {
                errors.add(where + ": " + ex.getMessage());
            }
        }
        return new ImportResult(imported, skipped, errors, ignoredColumns);
    }

    private BaggageRecord map(ExternalBaggageRecord e, RecordOrigin origin, String source, LocalDate checkedOn) {
        BaggageRecord r = new BaggageRecord();
        r.setTagId(FieldRules.id(e.tagId(), "tag_id"));
        r.setCaseReference(FieldRules.id(e.caseReference(), "case_reference"));
        if (r.getTagId() == null && r.getCaseReference() == null) {
            throw new IllegalArgumentException("needs a tag_id or case_reference");
        }
        r.setStatus(FieldRules.enumValue(e.status(), BaggageStatus.values(), "status"));
        r.setFlightNumber(FieldRules.flight(e.flightNumber(), "flight_number"));
        r.setOriginAirport(FieldRules.airport(e.origin(), "origin"));
        r.setDestinationAirport(FieldRules.airport(e.destination(), "destination"));
        r.setLastRecordedAirport(FieldRules.airport(e.lastAirport(), "last_airport"));
        r.setLastCheckpoint(FieldRules.text(e.lastCheckpoint(), 200, "last_checkpoint"));
        r.setDescription(FieldRules.text(e.description(), 1000, "description"));
        r.setDistinctiveMarks(FieldRules.text(e.distinctiveMarks(), 1000, "distinctive_marks"));
        r.setRecoveryStatus(FieldRules.enumValue(e.recoveryStatus(), RecoveryStatus.values(), "recovery_status"));
        String url = FieldRules.clean(e.externalUrl());
        if (url != null && !FieldRules.isUnknown(url)) {
            if (!urls.isValid(url)) {
                throw new IllegalArgumentException("external_url is not an acceptable https link");
            }
            r.setExternalRecordUrl(url);
        }
        r.setSource(source);
        r.setRecordOrigin(origin);
        r.setDateLastChecked(checkedOn == null ? LocalDate.now() : checkedOn);
        r.setVerificationStatus(VerificationStatus.UNVERIFIED);
        return r;
    }

    private boolean isDuplicate(BaggageRecord r) {
        return (r.getTagId() != null && repo.findFirstByTagId(r.getTagId()).isPresent())
                || (r.getCaseReference() != null && repo.findFirstByCaseReference(r.getCaseReference()).isPresent());
    }

    private static String cell(List<String> row, Map<String, Integer> col, String name) {
        Integer idx = col.get(name);
        return (idx == null || idx >= row.size()) ? null : row.get(idx);
    }
}
