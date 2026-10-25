package com.example.baggage.search;

import com.example.baggage.model.Labeled;

public enum AirportRole implements Labeled {
    ANY("Any of the three"),
    DEPARTURE("Departure airport"),
    ARRIVAL("Arrival airport"),
    LAST_RECORDED("Last recorded airport");

    private final String label;

    AirportRole(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
