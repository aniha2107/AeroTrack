package com.example.baggage.config;

import com.example.baggage.model.BaggageRecord;
import com.example.baggage.model.BaggageStatus;
import com.example.baggage.model.RecordOrigin;
import com.example.baggage.model.RecoveryStatus;
import com.example.baggage.model.VerifiableAttribute;
import com.example.baggage.model.VerificationStatus;
import com.example.baggage.repo.BaggageRepository;
import com.example.baggage.service.BaggageImportService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Seeds clearly labelled DEMONSTRATION records so the search UI can be shown before any authorized
 * data exists. Everything here is invented: tag IDs start with "DEMO", flight numbers use the
 * fictitious "DM" prefix, and no record has an official URL. Only runs on an empty database.
 */
@Component
public class DemoDataLoader implements ApplicationRunner {

    private static final String SOURCE = "Demonstration data written for this project - not a live airport or airline record";

    private static final String CSV_RESOURCE = "/data/aerotrack_100_luggage.csv";
    private static final String CSV_SOURCE = "AeroTrack synthetic demonstration dataset (100 made-up records) - not real baggage";

    private final BaggageRepository repo;
    private final BaggageImportService importer;
    private final boolean enabled;

    public DemoDataLoader(BaggageRepository repo, BaggageImportService importer,
                          @Value("${baggage.demo-data.enabled:true}") boolean enabled) {
        this.repo = repo;
        this.importer = importer;
        this.enabled = enabled;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled || repo.count() > 0) {
            return;
        }
        // Preferred: load the bundled synthetic dataset (100 records) every time the database is empty.
        try (InputStream in = DemoDataLoader.class.getResourceAsStream(CSV_RESOURCE)) {
            if (in != null) {
                String csv = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                importer.importCsv(csv, CSV_SOURCE, LocalDate.now(), RecordOrigin.DEMO);
                if (repo.count() > 0) {
                    return;
                }
            }
        } catch (java.io.IOException ex) {
            // fall through to the small built-in sample below
        }
        // Fallback sample. tag, case, status, flight, from, to, lastAirport, checkpoint, description, marks, recovery
        save("DEMO-0001", "DEMO-CASE-1001", BaggageStatus.REPORTED_MISSING, "DM101", "BOM", "LHR", "BOM",
                "Transfer belt", "Large black hard-shell suitcase", "Blue ribbon on handle", RecoveryStatus.NOT_RECOVERED, false);
        save("DEMO-0002", "DEMO-CASE-1002", BaggageStatus.FOUND, "DM202", "DEL", "DXB", "DXB",
                "Lost-property counter", "Red soft-sided duffel bag", "Yellow luggage strap", RecoveryStatus.LOCATED, false);
        save("DEMO-0003", null, BaggageStatus.IN_TRANSIT, "DM303", "BLR", "SIN", "BLR",
                "Loaded onto aircraft", null, null, null, false);
        save("DEMO-0004", "DEMO-CASE-1004", BaggageStatus.DELIVERED, null, "LHR", "BOM", "BOM",
                "Baggage claim belt 3", "Green backpack", "Round white sticker", RecoveryStatus.RETURNED, true);
        save("DEMO-0005", null, BaggageStatus.REGISTERED, "DM404", "DEL", "BOM", null,
                null, "Small grey cabin trolley", null, null, false);
        // Deliberately incomplete: no status, no flight. Shows that unknown attributes never match filters.
        save("DEMO-0006", null, null, null, null, null, "BOM",
                null, "Silver cabin bag with laptop sleeve", null, null, false);
        save(null, "DEMO-CASE-1007", BaggageStatus.REPORTED_MISSING, "DM505", "BOM", "DEL", null,
                null, null, "Orange name-tag holder", RecoveryStatus.NOT_RECOVERED, false);
    }

    private void save(String tag, String caseRef, BaggageStatus status, String flight, String from, String to,
                      String lastAirport, String checkpoint, String description, String marks,
                      RecoveryStatus recovery, boolean adminVerifiedExample) {
        BaggageRecord r = new BaggageRecord();
        r.setTagId(tag);
        r.setCaseReference(caseRef);
        r.setStatus(status);
        r.setFlightNumber(flight);
        r.setOriginAirport(from);
        r.setDestinationAirport(to);
        r.setLastRecordedAirport(lastAirport);
        r.setLastCheckpoint(checkpoint);
        r.setDescription(description);
        r.setDistinctiveMarks(marks);
        r.setRecoveryStatus(recovery);
        r.setSource(SOURCE);
        r.setRecordOrigin(RecordOrigin.DEMO);
        r.setDateLastChecked(LocalDate.now());
        if (adminVerifiedExample) {
            Set<VerifiableAttribute> v = new HashSet<>(Set.of(VerifiableAttribute.STATUS, VerifiableAttribute.LOCATION));
            r.setVerifiedAttributes(v);
            r.setVerificationStatus(VerificationStatus.ADMIN_VERIFIED);
            r.setVerifiedBy("demo (illustrative only)");
            r.setVerifiedAt(Instant.now());
        }
        repo.save(r);
    }
}
