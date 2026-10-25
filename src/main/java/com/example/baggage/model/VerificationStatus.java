package com.example.baggage.model;

public enum VerificationStatus implements Labeled {
    UNVERIFIED("Unverified - exactly as supplied by the source or entered manually"),
    ADMIN_VERIFIED("Verified by administrator (see verified attributes)");

    private final String label;

    VerificationStatus(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

    /** Short form for the filter drop-down. */
    public String getShortLabel() {
        return this == UNVERIFIED ? "Unverified" : "Verified by administrator";
    }
}
