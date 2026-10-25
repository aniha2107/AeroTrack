package com.example.baggage.model;

public enum BaggageStatus implements Labeled {
    REGISTERED("Registered"),
    IN_TRANSIT("In transit"),
    REPORTED_MISSING("Reported missing"),
    FOUND("Found"),
    DELIVERED("Delivered");

    private final String label;

    BaggageStatus(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
