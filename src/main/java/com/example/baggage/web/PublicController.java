package com.example.baggage.web;

import com.example.baggage.model.BaggageStatus;
import com.example.baggage.model.RecoveryStatus;
import com.example.baggage.model.VerificationStatus;
import com.example.baggage.search.AirportRole;
import com.example.baggage.search.BaggageSearchCriteria;
import com.example.baggage.service.BaggageSearchService;
import com.example.baggage.service.Texts;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class PublicController {

    private final BaggageSearchService search;

    public PublicController(BaggageSearchService search) {
        this.search = search;
    }

    @GetMapping("/")
    public String index(@ModelAttribute("criteria") BaggageSearchCriteria criteria, Model model) {
        BaggageSearchService.SearchResult result = search.search(criteria);
        model.addAttribute("results", result.results());
        model.addAttribute("dataMode", result.dataMode().name());
        model.addAttribute("airports", search.knownAirports());
        model.addAttribute("statuses", BaggageStatus.values());
        model.addAttribute("recoveries", RecoveryStatus.values());
        model.addAttribute("verifications", VerificationStatus.values());
        model.addAttribute("airportRoles", AirportRole.values());
        model.addAttribute("demoBanner", Texts.DEMO_BANNER);
        model.addAttribute("demoLabel", Texts.DEMO_RECORD_LABEL);
        return "index";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("dataMode", search.dataMode().name());
        return "about";
    }
}
