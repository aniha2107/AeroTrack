package com.example.baggage.web;

import com.example.baggage.service.Texts;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Makes the mandatory disclaimers available to every page. */
@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute("disclaimer")
    public String disclaimer() {
        return Texts.DISCLAIMER;
    }

    @ModelAttribute("noLiveSync")
    public String noLiveSync() {
        return Texts.NO_LIVE_SYNC;
    }

    @ModelAttribute("notSpecified")
    public String notSpecified() {
        return Texts.NOT_SPECIFIED;
    }
}
