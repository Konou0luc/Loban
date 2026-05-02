package com.loban.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public final class JsonListStrings {

    private static final ObjectMapper M = new ObjectMapper();
    private static final TypeReference<List<String>> TYPE = new TypeReference<>() {};

    private JsonListStrings() {}

    public static List<String> parse(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return M.readValue(json, TYPE);
        } catch (Exception e) {
            return List.of();
        }
    }

    public static String toJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        try {
            return M.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }
}
