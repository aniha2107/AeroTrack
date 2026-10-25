package com.example.baggage.service;

import java.util.ArrayList;
import java.util.List;

/** Small RFC-4180-style CSV parser (quoted fields, doubled quotes, CRLF). */
final class CsvParser {

    private CsvParser() {
    }

    static List<List<String>> parse(String text) {
        if (text.startsWith("﻿")) {
            text = text.substring(1);
        }
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        int len = text.length();
        for (int i = 0; i < len; i++) {
            char ch = text.charAt(i);
            if (inQuotes) {
                if (ch == '"') {
                    if (i + 1 < len && text.charAt(i + 1) == '"') {
                        cur.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    cur.append(ch);
                }
            } else if (ch == '"') {
                inQuotes = true;
            } else if (ch == ',') {
                row.add(cur.toString());
                cur.setLength(0);
            } else if (ch == '\n') {
                row.add(cur.toString());
                cur.setLength(0);
                rows.add(row);
                row = new ArrayList<>();
            } else if (ch != '\r') {
                cur.append(ch);
            }
        }
        if (cur.length() > 0 || !row.isEmpty()) {
            row.add(cur.toString());
            rows.add(row);
        }
        rows.removeIf(r -> r.stream().allMatch(String::isBlank));
        return rows;
    }
}
