package fr.cnrs.iremus.sherlock.pojo.triple;

import fr.cnrs.iremus.sherlock.common.Datatype;
import fr.cnrs.iremus.sherlock.common.ResourceType;
import io.micronaut.core.annotation.Introspected;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Introspected
@TripleValidator
public class TripleCreate {
    @NotBlank
    private String s;
    @NotBlank
    private String p;
    @NotBlank
    private String o;
    @NotNull
    private ResourceType object_type;
    private Datatype o_datatype;
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

    public ResourceType getObject_type() {
        return object_type;
    }

    public void setObject_type(ResourceType object_type) {
        this.object_type = object_type;
    }

    public Datatype getO_datatype() {
        return o_datatype;
    }

    public void setO_datatype(Datatype o_datatype) {
        this.o_datatype = o_datatype;
    }

    public String getO_lg() {
        return o_lg;
    }

    public void setO_lg(String o_lg) {
        this.o_lg = o_lg;
    }
}