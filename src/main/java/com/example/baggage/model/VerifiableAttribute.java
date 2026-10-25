package com.example.baggage.model;

/** Attributes an administrator can separately verify (and add) for a record. */
public enum VerifiableAttribute implements Labeled {
    STATUS("Baggage status"),
    LOCATION("Last recorded location"),
    FLIGHT("Flight details"),
    DESCRIPTION("Description / distinctive marks"),
    RECOVERY("Recovery status");

    private final String label;

    VerifiableAttribute(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
