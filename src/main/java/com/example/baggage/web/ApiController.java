package com.example.baggage.web;

import com.example.baggage.search.BaggageSearchCriteria;
import com.example.baggage.service.BaggageSearchService;
import com.example.baggage.service.BaggageView;
import com.example.baggage.service.Texts;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** JSON version of the search. Same filters, same database query, same disclaimers. */
@RestController
public class ApiController {

    public record ApiResponse(String notice, String liveSynchronization, String dataMode, int count,
                              List<BaggageView> results) {
    }

    private final BaggageSearchService search;

    public ApiController(BaggageSearchService search) {
        this.search = search;
    }

    @GetMapping("/api/baggage")
    public ApiResponse search(@ModelAttribute BaggageSearchCriteria criteria) {
        BaggageSearchService.SearchResult r = search.search(criteria);
        String notice = Texts.DISCLAIMER + " " + Texts.NO_LIVE_SYNC
                + (r.results().stream().anyMatch(BaggageView::demo) ? " " + Texts.DEMO_BANNER : "");
        return new ApiResponse(notice, "not implemented", r.dataMode().name(), r.results().size(), r.results());
    }
}
