package com.example.baggage.service;

import java.util.List;

/**
 * Extension point for a data source you have been explicitly authorized to use
 * (an airline/airport API, a partner integration, a scheduled permitted export, ...).
 *
 * NO implementation ships with this project: the research behind it found no public API, and the
 * industry baggage-data services require an existing airline or airport relationship (see README).
 * If your institution obtains access, register an implementation as a Spring bean. The admin
 * dashboard will then list it and let an administrator trigger an import. It is a manual pull,
 * not a live synchronization.
 */
public interface AuthorizedBaggageSource {

    /** Human-readable name used as the stored "source" (e.g. "Example Airline - partner API"). */
    String sourceName();

    /** Fetch only what the agreement permits; map values through unchanged. */
    List<ExternalBaggageRecord> fetch();
}
