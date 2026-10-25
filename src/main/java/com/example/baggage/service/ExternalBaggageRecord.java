package com.example.baggage.service;

/**
 * Raw values exactly as supplied by an authorized source (CSV export row, API response, ...).
 * All fields are strings; blank means the source did not supply the value.
 * There is deliberately no field for passenger names or contact details.
 */
public record ExternalBaggageRecord(
        String tagId,
        String caseReference,
        String status,
        String flightNumber,
        String origin,
        String destination,
        String lastAirport,
        String lastCheckpoint,
        String description,
        String distinctiveMarks,
        String recoveryStatus,
        String externalUrl) {
}
