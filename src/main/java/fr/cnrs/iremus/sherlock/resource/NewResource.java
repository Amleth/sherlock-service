package fr.cnrs.iremus.sherlock.resource;

import io.micronaut.core.annotation.Introspected;

import javax.validation.constraints.NotBlank;

@Introspected
public class NewResource {
    @NotBlank
    private String type;
    @NotBlank
    private String p1_is_identified_by;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getP1_is_identified_by() {
        return p1_is_identified_by;
    }

    public void setP1_is_identified_by(String p1_is_identified_by) {
        this.p1_is_identified_by = p1_is_identified_by;
    }
}
