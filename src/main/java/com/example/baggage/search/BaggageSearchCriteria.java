package com.example.baggage.search;

import com.example.baggage.model.BaggageStatus;
import com.example.baggage.model.RecoveryStatus;
import com.example.baggage.model.VerificationStatus;

/** The filters a user can select. A null/blank filter means "not applied". */
public class BaggageSearchCriteria {

    private String idOrCase;
    private String airport;
    private AirportRole airportRole = AirportRole.ANY;
    private BaggageStatus status;
    private String flightNumber;
    private String origin;
    private String destination;
    private String checkpoint;
    private String keywords;
    private RecoveryStatus recovery;
    private VerificationStatus verification;

    public String getIdOrCase() { return idOrCase; }
    public void setIdOrCase(String idOrCase) { this.idOrCase = idOrCase; }
    public String getAirport() { return airport; }
    public void setAirport(String airport) { this.airport = airport; }
    public AirportRole getAirportRole() { return airportRole; }
    public void setAirportRole(AirportRole airportRole) { this.airportRole = airportRole; }
    public BaggageStatus getStatus() { return status; }
    public void setStatus(BaggageStatus status) { this.status = status; }
    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public String getCheckpoint() { return checkpoint; }
    public void setCheckpoint(String checkpoint) { this.checkpoint = checkpoint; }
    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }
    public RecoveryStatus getRecovery() { return recovery; }
    public void setRecovery(RecoveryStatus recovery) { this.recovery = recovery; }
    public VerificationStatus getVerification() { return verification; }
    public void setVerification(VerificationStatus verification) { this.verification = verification; }
}
