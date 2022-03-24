package fr.cnrs.iremus.sherlock.common;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Datatype {
    STRING("string"),
    INTEGER("integer"),
    DATE("date");

    @JsonValue
    private final String label;

    Datatype(String label) {
        this.label = label;
    }
}
