package com.example.baggage;

import com.example.baggage.model.BaggageRecord;
import com.example.baggage.model.BaggageStatus;
import com.example.baggage.model.RecordOrigin;
import com.example.baggage.repo.BaggageRepository;
import com.example.baggage.search.AirportRole;
import com.example.baggage.search.BaggageSearchCriteria;
import com.example.baggage.service.AdminForm;
import com.example.baggage.service.BaggageAdminService;
import com.example.baggage.service.BaggageImportService;
import com.example.baggage.service.BaggageSearchService;
import com.example.baggage.service.BaggageView;
import com.example.baggage.service.OfficialUrlValidator;
import com.example.baggage.model.VerifiableAttribute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:baggage-test;DB_CLOSE_DELAY=-1",
        "baggage.demo-data.enabled=false",
        "baggage.admin.password=test-password"
})
@AutoConfigureMockMvc
class BaggageSystemTest {

    @Autowired BaggageRepository repo;
    @Autowired BaggageSearchService search;
    @Autowired BaggageImportService importer;
    @Autowired BaggageAdminService admin;
    @Autowired OfficialUrlValidator urls;
    @Autowired MockMvc mvc;

    @BeforeEach
    void clean() {
        repo.deleteAll();
    }

    private BaggageRecord save(String tag, BaggageStatus status, String flight, String from, String to, String last,
                               String desc) {
        BaggageRecord r = new BaggageRecord();
        r.setTagId(tag);
        r.setStatus(status);
        r.setFlightNumber(flight);
        r.setOriginAirport(from);
        r.setDestinationAirport(to);
        r.setLastRecordedAirport(last);
        r.setDescription(desc);
        r.setSource("test");
        r.setRecordOrigin(RecordOrigin.MANUAL_ENTRY);
        r.setDateLastChecked(LocalDate.now());
        return repo.save(r);
    }

    private List<String> tags(BaggageSearchCriteria c) {
        return search.search(c).results().stream().map(BaggageView::tagId).toList();
    }

    @Test
    void unknownAttributesNeverMatchFilters() {
        save("AAA111", BaggageStatus.FOUND, "AB123", "BOM", "DEL", "DEL", "Red bag");
        save("BBB222", null, null, null, null, null, null);   // everything unknown

        BaggageSearchCriteria c = new BaggageSearchCriteria();
        c.setStatus(BaggageStatus.FOUND);
        assertThat(tags(c)).containsExactly("AAA111");

        c = new BaggageSearchCriteria();
        c.setFlightNumber("ab 123");
        assertThat(tags(c)).containsExactly("AAA111");

        c = new BaggageSearchCriteria();
        c.setKeywords("red");
        assertThat(tags(c)).containsExactly("AAA111");

        c = new BaggageSearchCriteria();
        c.setAirport("DEL");
        assertThat(tags(c)).containsExactly("AAA111");

        // no filters: every record, including the incomplete one, which shows "Not specified"
        List<BaggageView> all = search.search(new BaggageSearchCriteria()).results();
        assertThat(all).hasSize(2);
        BaggageView incomplete = all.stream().filter(v -> v.tagId().equals("BBB222")).findFirst().orElseThrow();
        assertThat(incomplete.status()).isEqualTo("Not specified");
        assertThat(incomplete.flightNumber()).isEqualTo("Not specified");
        assertThat(incomplete.officialTrackingUrl()).isNull();
    }

    @Test
    void airportRolesAreRespected() {
        save("T1", BaggageStatus.IN_TRANSIT, "AB1", "BOM", "DEL", "BLR", null);

        BaggageSearchCriteria c = new BaggageSearchCriteria();
        c.setAirport("BOM");
        c.setAirportRole(AirportRole.DEPARTURE);
        assertThat(tags(c)).containsExactly("T1");
        c.setAirportRole(AirportRole.ARRIVAL);
        assertThat(tags(c)).isEmpty();
        c.setAirportRole(AirportRole.LAST_RECORDED);
        assertThat(tags(c)).isEmpty();
        c.setAirport("BLR");
        assertThat(tags(c)).containsExactly("T1");
        c.setAirportRole(AirportRole.ANY);
        c.setAirport("DEL");
        assertThat(tags(c)).containsExactly("T1");
    }

    @Test
    void likeWildcardsAreTreatedAsLiterals() {
        save("W1", null, null, null, null, null, "Blue 100% cotton bag");
        save("W2", null, null, null, null, null, "Blue nylon bag");
        BaggageSearchCriteria c = new BaggageSearchCriteria();
        c.setKeywords("%");
        assertThat(tags(c)).containsExactly("W1");
    }

    @Test
    void officialLinkOnlyWhenUrlIsValid() {
        assertThat(urls.isValid("https://www.example.com/track?id=1")).isTrue();
        assertThat(urls.isValid("http://www.example.com/track")).isFalse();
        assertThat(urls.isValid("javascript:alert(1)")).isFalse();
        assertThat(urls.isValid("https://user:pw@example.com/")).isFalse();
        assertThat(urls.isValid("not a url")).isFalse();
        assertThat(urls.isValid(null)).isFalse();

        BaggageRecord r = save("L1", null, null, null, null, null, null);
        r.setExternalRecordUrl("javascript:alert(1)");
        repo.save(r);
        assertThat(search.search(new BaggageSearchCriteria()).results().get(0).officialTrackingUrl()).isNull();

        r.setExternalRecordUrl("https://www.example.com/track?id=1");
        repo.save(r);
        BaggageView v = search.search(new BaggageSearchCriteria()).results().get(0);
        assertThat(v.officialTrackingUrl()).isEqualTo("https://www.example.com/track?id=1");
        assertThat(v.officialTrackingHost()).isEqualTo("www.example.com");
    }

    @Test
    void csvImportCopiesOnlySuppliedValuesAndRejectsGuesses() {
        String csv = "tag_id,case_reference,status,flight_number,origin,description,passenger_name,external_url\n"
                + "CSV001,,Found,ab 12,bom,\"Black bag, ribbon\",Jane Doe,https://www.example.com/t/1\n"
                + "CSV002,,,,,,,\n"
                + "CSV003,,teleported,,,,,\n"
                + "CSV004,,,,,,,http://insecure.example.com/\n"
                + "CSV001,,,,,,,\n";
        BaggageImportService.ImportResult res = importer.importCsv(csv, "Test permitted export", LocalDate.now());

        assertThat(res.imported()).isEqualTo(2);
        assertThat(res.skippedDuplicates()).isEqualTo(1);
        assertThat(res.errors()).hasSize(2);
        assertThat(res.ignoredColumns()).containsExactly("passenger_name");

        BaggageRecord one = repo.findFirstByTagId("CSV001").orElseThrow();
        assertThat(one.getStatus()).isEqualTo(BaggageStatus.FOUND);
        assertThat(one.getFlightNumber()).isEqualTo("AB12");
        assertThat(one.getOriginAirport()).isEqualTo("BOM");
        assertThat(one.getDescription()).isEqualTo("Black bag, ribbon");
        assertThat(one.getRecordOrigin()).isEqualTo(RecordOrigin.AUTHORIZED_EXPORT);
        assertThat(one.getSource()).isEqualTo("Test permitted export");
        assertThat(one.getDateLastChecked()).isEqualTo(LocalDate.now());

        BaggageRecord two = repo.findFirstByTagId("CSV002").orElseThrow();
        assertThat(two.getStatus()).isNull();
        assertThat(two.getFlightNumber()).isNull();
        assertThat(two.getLastRecordedAirport()).isNull();
    }

    @Test
    void adminCannotVerifyAttributeThatIsNotSpecified() {
        AdminForm f = new AdminForm();
        f.setTagId("adm1");
        f.setSource("Manual entry from airline's written reply");
        f.setVerifiedAttributes(Set.of(VerifiableAttribute.STATUS));
        assertThat(admin.validate(f)).anyMatch(s -> s.contains("Not specified"));

        f.setStatus(BaggageStatus.REPORTED_MISSING);
        assertThat(admin.validate(f)).isEmpty();
        BaggageRecord saved = admin.save(f, "alice");
        assertThat(saved.getTagId()).isEqualTo("ADM1");
        assertThat(saved.getVerifiedBy()).isEqualTo("alice");
        assertThat(saved.getVerificationStatus().name()).isEqualTo("ADMIN_VERIFIED");
        assertThat(saved.getRecoveryStatus()).isNull();
    }

    @Test
    void adminRequiresIdentifierAndSource() {
        AdminForm f = new AdminForm();
        assertThat(admin.validate(f)).isNotEmpty();
        f.setTagId("ok123");
        assertThat(admin.validate(f)).anyMatch(s -> s.startsWith("Source"));
    }

    @Test
    void publicPagesCarryDisclaimerAndAdminIsProtected() throws Exception {
        save("PUB1", BaggageStatus.FOUND, null, null, null, null, null);
        mvc.perform(get("/")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("verify current details")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("PUB1")));
        mvc.perform(get("/api/baggage").param("status", "FOUND")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"liveSynchronization\":\"not implemented\"")));
        mvc.perform(get("/admin")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void demoRecordsAreLabelled() throws Exception {
        BaggageRecord r = save("DEMO-X", BaggageStatus.REGISTERED, null, null, null, null, null);
        r.setRecordOrigin(RecordOrigin.DEMO);
        repo.save(r);
        mvc.perform(get("/")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("DEMONSTRATION RECORD")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("not live airport or airline records")));
    }
}
