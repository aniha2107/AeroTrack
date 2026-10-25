package com.example.baggage.model;

public enum RecordOrigin implements Labeled {
    DEMO("Demonstration record (not a live airport or airline record)"),
    MANUAL_ENTRY("Entered manually from permitted information"),
    AUTHORIZED_EXPORT("Imported from an authorized data export"),
    AUTHORIZED_API("Retrieved from an authorized API");

    private final String label;

    RecordOrigin(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
