package com.example.baggage.service;

/** Wording that must appear consistently across the UI and the JSON API. */
public final class Texts {

    public static final String NOT_SPECIFIED = "Not specified";

    public static final String DISCLAIMER =
            "Baggage status and location information can change at any moment and may be out of date here. "
                    + "Always verify current details with the relevant airline or airport, and contact their "
                    + "baggage service provider (baggage services or lost-and-found desk) for the latest information.";

    public static final String NO_LIVE_SYNC =
            "This system is not connected to any live airport or airline feed. The \"last checked\" date shows "
                    + "when a record was last compared with its source - not that it is current.";

    public static final String DEMO_BANNER =
            "No authorized baggage data has been loaded yet. The records below are DEMONSTRATION records created "
                    + "for this project. They are not live airport or airline records and do not describe real baggage.";

    public static final String DEMO_RECORD_LABEL =
            "DEMONSTRATION RECORD - not a live airport or airline record";

    private Texts() {
    }
}
