package com.example.baggage.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Decides whether a stored URL may be shown as an "official tracking link".
 * Only absolute https URLs with a host, no embedded credentials, are accepted - so values such as
 * "javascript:..." can never become a link. An optional host allow-list can restrict links further.
 */
@Component
public class OfficialUrlValidator {

    private final List<String> allowedHosts;

    public OfficialUrlValidator(@Value("${baggage.official-url.allowed-hosts:}") String allowed) {
        this.allowedHosts = Arrays.stream(allowed.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.toLowerCase(Locale.ROOT))
                .toList();
    }

    public boolean isValid(String url) {
        return host(url) != null;
    }

    /** Returns the host for a valid URL, or null if the URL is not acceptable. */
    public String host(String url) {
        if (url == null || url.isBlank() || url.length() > 2000) {
            return null;
        }
        try {
            URI uri = new URI(url.trim());
            if (!uri.isAbsolute() || !"https".equalsIgnoreCase(uri.getScheme())) {
                return null;
            }
            String host = uri.getHost();
            if (host == null || host.isBlank() || uri.getUserInfo() != null || !host.contains(".")) {
                return null;
            }
            host = host.toLowerCase(Locale.ROOT);
            if (!allowedHosts.isEmpty()) {
                boolean ok = false;
                for (String a : allowedHosts) {
                    if (host.equals(a) || host.endsWith("." + a)) {
                        ok = true;
                        break;
                    }
                }
                if (!ok) {
                    return null;
                }
            }
            return host;
        } catch (Exception e) {
            return null;
        }
    }
}
