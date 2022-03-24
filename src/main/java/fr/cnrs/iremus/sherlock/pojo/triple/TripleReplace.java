package fr.cnrs.iremus.sherlock.pojo.triple;

import io.micronaut.core.annotation.Introspected;

import javax.validation.constraints.NotBlank;

@Introspected
public class TripleReplace {
    @NotBlank
    private String old_s;
    @NotBlank
    private String old_p;
    @NotBlank
    private String old_o;

    @NotBlank
    private String new_s;
    @NotBlank
    private String new_p;
    @NotBlank
    private String new_o;

    public String getOld_s() {
        return old_s;
    }

    public void setOld_s(String old_s) {
        this.old_s = old_s;
    }

    public String getOld_p() {
        return old_p;
    }

    public void setOld_p(String old_p) {
        this.old_p = old_p;
    }

    public String getOld_o() {
        return old_o;
    }

    public void setOld_o(String old_o) {
        this.old_o = old_o;
    }

    public String getNew_s() {
        return new_s;
    }

    public void setNew_s(String new_s) {
        this.new_s = new_s;
    }

    public String getNew_p() {
        return new_p;
    }

    public void setNew_p(String new_p) {
        this.new_p = new_p;
    }

    public String getNew_o() {
        return new_o;
    }

    public void setNew_o(String new_o) {
        this.new_o = new_o;
    }
}