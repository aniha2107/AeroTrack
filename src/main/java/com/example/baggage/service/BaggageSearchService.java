package com.example.baggage.service;

import com.example.baggage.model.BaggageRecord;
import com.example.baggage.model.Labeled;
import com.example.baggage.model.RecordOrigin;
import com.example.baggage.model.VerifiableAttribute;
import com.example.baggage.repo.BaggageRepository;
import com.example.baggage.search.BaggageSearchCriteria;
import com.example.baggage.search.BaggageSpecifications;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

@Service
public class BaggageSearchService {

    public record SearchResult(List<BaggageView> results, DataMode dataMode) {
    }

    private final BaggageRepository repo;
    private final OfficialUrlValidator urls;

    public BaggageSearchService(BaggageRepository repo, OfficialUrlValidator urls) {
        this.repo = repo;
        this.urls = urls;
    }

    /** Runs the selected filters against the database and returns every matching record. */
    @Transactional(readOnly = true)
    public SearchResult search(BaggageSearchCriteria criteria) {
        Sort sort = Sort.by(Sort.Direction.DESC, "dateLastChecked").and(Sort.by(Sort.Direction.ASC, "id"));
        List<BaggageView> views = repo.findAll(BaggageSpecifications.from(criteria), sort)
                .stream().map(this::toView).toList();
        return new SearchResult(views, dataMode());
    }

    @Transactional(readOnly = true)
    public DataMode dataMode() {
        long total = repo.count();
        if (total == 0) {
            return DataMode.NO_DATA;
        }
        long demo = repo.countByRecordOrigin(RecordOrigin.DEMO);
        if (demo == total) {
            return DataMode.DEMO_ONLY;
        }
        return demo == 0 ? DataMode.AUTHORIZED_ONLY : DataMode.MIXED;
    }

    /** Airport codes that actually occur in the data, for the filter drop-downs. */
    @Transactional(readOnly = true)
    public List<String> knownAirports() {
        TreeSet<String> set = new TreeSet<>();
        set.addAll(repo.distinctOrigins());
        set.addAll(repo.distinctDestinations());
        set.addAll(repo.distinctLastAirports());
        return List.copyOf(set);
    }

    public BaggageView toView(BaggageRecord r) {
        String host = r.isDemo() ? null : urls.host(r.getExternalRecordUrl());
        String url = host == null ? null : r.getExternalRecordUrl().trim();
        List<String> verified = r.getVerifiedAttributes().stream()
                .sorted(Comparator.comparingInt(VerifiableAttribute::ordinal))
                .map(VerifiableAttribute::getLabel)
                .toList();
        return new BaggageView(
                r.getId(),
                nz(r.getTagId()),
                nz(r.getCaseReference()),
                label(r.getStatus()),
                nz(r.getFlightNumber()),
                nz(r.getOriginAirport()),
                nz(r.getDestinationAirport()),
                nz(r.getLastRecordedAirport()),
                nz(r.getLastCheckpoint()),
                nz(r.getDescription()),
                nz(r.getDistinctiveMarks()),
                label(r.getRecoveryStatus()),
                r.getVerificationStatus().getLabel(),
                verified,
                r.getDateLastChecked() == null ? Texts.NOT_SPECIFIED : r.getDateLastChecked().toString(),
                nz(r.getSource()),
                r.getRecordOrigin().getLabel(),
                r.isDemo(),
                url,
                host);
    }

    private static String nz(String s) {
        return (s == null || s.isBlank()) ? Texts.NOT_SPECIFIED : s;
    }

    private static String label(Labeled l) {
        return l == null ? Texts.NOT_SPECIFIED : l.getLabel();
    }
}
