# AeroTrack - Airport Lost-Baggage Tracking System

A Java Spring Boot student project that lets people search baggage records using filters, while using **only data the project is authorized to use**. No airline or airport website is scraped, and no passenger names or contact details are stored.

> **Status of this build:** the Spring Boot code, templates and tests were written but could not be compiled or run in the environment where they were generated (Maven Central was blocked there). The dependency-free logic (input normalisation, CSV parsing) was run and passes; every source file passes a syntax check. Please run `mvn test` first and fix anything the compiler reports.

## 1. Is there an authorized data source? (Requirement 1)

Findings from the research done for this project (nothing is assumed):

| Service | What it is | Can a student project use it? |
|---|---|---|
| **SITA Bag Journey API** | REST API that exposes baggage-tracking events and flight info to airline/airport systems | SITA's developer page says the API is only available to customers with an existing association with an airline. No public or self-service access found. |
| **SITA WorldTracer** | Global system airlines and ground handlers use to trace and match delayed or mishandled bags (500+ airlines, ~2,800 airports) | Used by airline and airport staff. Not offered as a public data feed. |
| **IATA Resolution 753** | Requires airlines to track bags at four points (acceptance, loading, transfer, arrival) and share tracking data with interline partners | A compliance standard for airlines, not a public API. |
| **Passenger-authorized location sharing (e.g. Google Find Hub in WorldTracer)** | A passenger shares a tracker location with the airline | Goes to airline staff only, at the passenger's choice. |

**Conclusion:** no public or student-accessible API was found, so none is used and **live synchronization is not implemented**. The app therefore uses the fallback in the brief: manually entered records from information you have permission to use, an authorized export, and clearly labelled demonstration records.

To get real access you would need a written agreement or partnership with an airline or airport (for example through your institution), or a permitted data export from them. If you obtain one, implement `AuthorizedBaggageSource` (see below).

Sources: [SITA Bag Journey API overview](https://www.developer.aero/api-catalog/bag-journey-overview), [SITA Bag Journey](https://sita.aero/solutions/sita-at-airports/sita-baggage-management/sita-bag-journey), [IATA baggage tracking / Resolution 753](https://www.iata.org/en/programs/ops-infra/baggage/rfid/), [SITA and Google Find Hub in WorldTracer](https://ecommercenews.asia/story/sita-links-google-find-hub-with-worldtracer-baggage-data).

## 2. Run it

Requirements: Java 17+ and Maven.

```bash
export BAGGAGE_ADMIN_PASSWORD='choose-a-password'   # if unset, a random one is printed in the log
mvn test
mvn spring-boot:run
```

- Public search: http://localhost:8080/
- About and data sources: http://localhost:8080/about
- Administrator (sign in as `admin`): http://localhost:8080/admin
- JSON search: `http://localhost:8080/api/baggage?status=FOUND&airport=DXB&airportRole=ARRIVAL`

On an empty database the app seeds 7 **demonstration** records (tags start with `DEMO`, flights use the fictional `DM` prefix, no links). Once you hold authorized data, use *Remove demonstration records* in the admin dashboard or set `baggage.demo-data.enabled=false`.

## 3. How the requirements are met

| # | Requirement | Where |
|---|---|---|
| 1 | Investigate, don't assume an API | Section 1 above; no API client is shipped |
| 2 | Spring Boot adapter if access exists | `AuthorizedBaggageSource` interface. Any bean implementing it appears on the admin dashboard with an "Import now" button (a manual pull, not a live feed) |
| 3 | Manual records or authorized export; official link when available | Admin form and CSV import (`/admin/new`, `/admin/import`) |
| 4 | No scraping or republishing | No HTTP client or crawler in the project; import asks for an authorization confirmation; unknown CSV columns (e.g. passenger names) are ignored |
| 5 | Store URL, source, date last checked, verification status | `BaggageRecord`: `externalRecordUrl`, `source`, `dateLastChecked`, `verificationStatus` (+ `recordOrigin`) |
| 6 | Status may change; verify with airline/airport | Banner on every page and footer, plus the JSON `notice` |
| 7 | Filters | Airport (departure / arrival / last recorded / any), tag ID or case reference, status, flight number, origin, destination, last checkpoint, description and marks, recovery status, verification status |
| 8 | Never infer; use "Not specified" | Attributes are nullable; blank or unknown inputs become null; unrecognised values are rejected at import instead of guessed |
| 9 | Unknown never matches a filter | `BaggageSpecifications`: only equality/LIKE comparisons, which are never true for NULL |
| 10 | Admin can verify/add attributes | `/admin/{id}/edit`: set missing values and tick verified attributes. An attribute with no value cannot be ticked |

**Search behaviour:** the controller binds the selected filters, `BaggageSearchService` builds one JPA Specification query, and every matching record is returned. Each result shows tag ID / case reference, details, flight, last recorded location, verified attributes, last-checked date, source, and a labelled "Official tracking page" link only when the stored URL is a valid https link (otherwise "Official tracking link: Not specified").

## 4. Importing an authorized export

Admin → *Import authorized CSV export*. Header columns (all optional except one of `tag_id` / `case_reference`):

`tag_id, case_reference, status, flight_number, origin, destination, last_airport, last_checkpoint, description, distinctive_marks, recovery_status, external_url`

`sample-import-FICTIONAL.csv` is an invented file for testing the importer. Do not treat it as real data.

## 5. Plugging in a real authorized source later

```java
@Component
class ExampleAirlineSource implements AuthorizedBaggageSource {
    public String sourceName() { return "Example Airline - partner API (agreement ref. 123)"; }
    public List<ExternalBaggageRecord> fetch() {
        // call the API you are contractually allowed to use, copy values through unchanged,
        // leave anything the API does not state as null
    }
}
```

Imported records start **Unverified**. An administrator can then verify or add attributes the source did not provide.

## 6. Security and privacy notes

- `/admin/**` requires login; there is no default password.
- No passenger names or contact details are stored or imported.
- Links are shown only if they are absolute https URLs; optionally restrict hosts with `baggage.official-url.allowed-hosts`.
- Data is stored in a local H2 file database (`./data`). Do not publish a deployment that holds real baggage data without proper access controls and an agreement covering that data.
