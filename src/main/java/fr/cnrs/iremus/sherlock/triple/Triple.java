package fr.cnrs.iremus.sherlock.triple;

import io.micronaut.core.annotation.Introspected;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@Introspected
public class Triple {
    @NotBlank
    private String s;
    @NotBlank
    private String p;
    @NotBlank
    private String o;
    @NotNull
    private Boolean o_is_uri;
    private String o_type;
    private String o_lg;

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    public String getP() {
        return p;
    }

    public void setP(String p) {
        this.p = p;
    }

    public String getO() {
        return o;
    }

    public void setO(String o) {
        this.o = o;
    }

    public Boolean getO_is_uri() {
        return o_is_uri;
    }

    public void setO_is_uri(Boolean o_is_uri) {
        this.o_is_uri = o_is_uri;
    }

    public String getO_type() {
        return o_type;
    }

    public void setO_type(String o_type) {
        this.o_type = o_type;
    }

    public String getO_lg() {
        return o_lg;
    }

    public void setO_lg(String o_lg) {
        this.o_lg = o_lg;
    }
}