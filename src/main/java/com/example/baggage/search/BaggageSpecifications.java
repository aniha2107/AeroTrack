package com.example.baggage.search;

import com.example.baggage.model.BaggageRecord;
import com.example.baggage.service.FieldRules;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Turns the selected filters into one database query.
 *
 * Every filter is an equality or LIKE comparison against a column. In SQL, comparing a NULL column
 * ("Not specified") to anything is never true, so a record whose attribute is unknown can never match
 * a filter on that attribute. Nothing here infers or defaults a missing value.
 */
public final class BaggageSpecifications {

    private static final int MAX_KEYWORDS = 6;

    private BaggageSpecifications() {
    }

    public static Specification<BaggageRecord> from(BaggageSearchCriteria c) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();

            String id = upper(FieldRules.clean(c.getIdOrCase()));
            if (id != null) {
                id = id.replaceAll("\\s+", "");
                p.add(cb.or(
                        cb.equal(root.<String>get("tagId"), id),
                        cb.equal(root.<String>get("caseReference"), id)));
            }

            String airport = upper(FieldRules.clean(c.getAirport()));
            if (airport != null) {
                AirportRole role = c.getAirportRole() == null ? AirportRole.ANY : c.getAirportRole();
                Predicate dep = cb.equal(root.<String>get("originAirport"), airport);
                Predicate arr = cb.equal(root.<String>get("destinationAirport"), airport);
                Predicate last = cb.equal(root.<String>get("lastRecordedAirport"), airport);
                switch (role) {
                    case DEPARTURE -> p.add(dep);
                    case ARRIVAL -> p.add(arr);
                    case LAST_RECORDED -> p.add(last);
                    default -> p.add(cb.or(dep, arr, last));
                }
            }

            if (c.getStatus() != null) {
                p.add(cb.equal(root.get("status"), c.getStatus()));
            }

            String flight = upper(FieldRules.clean(c.getFlightNumber()));
            if (flight != null) {
                p.add(cb.equal(root.<String>get("flightNumber"), flight.replaceAll("\\s+", "")));
            }

            String origin = upper(FieldRules.clean(c.getOrigin()));
            if (origin != null) {
                p.add(cb.equal(root.<String>get("originAirport"), origin));
            }

            String destination = upper(FieldRules.clean(c.getDestination()));
            if (destination != null) {
                p.add(cb.equal(root.<String>get("destinationAirport"), destination));
            }

            String checkpoint = FieldRules.clean(c.getCheckpoint());
            if (checkpoint != null) {
                p.add(cb.like(cb.lower(root.<String>get("lastCheckpoint")), like(checkpoint), '\\'));
            }

            String keywords = FieldRules.clean(c.getKeywords());
            if (keywords != null) {
                String[] terms = keywords.split("\\s+");
                for (int i = 0; i < Math.min(terms.length, MAX_KEYWORDS); i++) {
                    String pattern = like(terms[i]);
                    p.add(cb.or(
                            cb.like(cb.lower(root.<String>get("description")), pattern, '\\'),
                            cb.like(cb.lower(root.<String>get("distinctiveMarks")), pattern, '\\')));
                }
            }

            if (c.getRecovery() != null) {
                p.add(cb.equal(root.get("recoveryStatus"), c.getRecovery()));
            }

            if (c.getVerification() != null) {
                p.add(cb.equal(root.get("verificationStatus"), c.getVerification()));
            }

            return cb.and(p.toArray(new Predicate[0]));
        };
    }

    private static String upper(String s) {
        return s == null ? null : s.toUpperCase(Locale.ROOT);
    }

    private static String like(String term) {
        String escaped = term.toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
