package fr.cnrs.iremus.sherlock.common;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ResourceType {
    URI("uri"),
    LITERAL("literal");

    @JsonValue
    private final String label;

    ResourceType(String label) {
        this.label = label;
    }
}
