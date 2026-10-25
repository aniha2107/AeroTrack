package com.example.baggage.service;

import java.util.List;

/**
 * Display model for one result. Every text attribute is either confirmed data or "Not specified".
 * {@code officialTrackingUrl} is non-null only when the stored URL passed validation.
 */
public record BaggageView(
        Long id,
        String tagId,
        String caseReference,
        String status,
        String flightNumber,
        String origin,
        String destination,
        String lastRecordedAirport,
        String lastCheckpoint,
        String description,
        String distinctiveMarks,
        String recoveryStatus,
        String verificationStatus,
        List<String> verifiedAttributes,
        String dateLastChecked,
        String source,
        String recordType,
        boolean demo,
        String officialTrackingUrl,
        String officialTrackingHost) {
}
