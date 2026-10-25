package com.example.baggage.model;

public enum RecoveryStatus implements Labeled {
    NOT_RECOVERED("Not yet recovered"),
    LOCATED("Located - awaiting collection"),
    RETURNED("Returned to passenger");

    private final String label;

    RecoveryStatus(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
