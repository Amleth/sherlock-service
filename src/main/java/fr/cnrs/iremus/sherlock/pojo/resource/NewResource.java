package fr.cnrs.iremus.sherlock.pojo.resource;

import io.micronaut.core.annotation.Introspected;

import javax.validation.constraints.NotBlank;

@Introspected
public class NewResource {
    @NotBlank
    private String type;
    @NotBlank
    private String p1;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getP1() {
        return p1;
    }

    public void setP1(String p1) {
        this.p1 = p1;
    }
}
