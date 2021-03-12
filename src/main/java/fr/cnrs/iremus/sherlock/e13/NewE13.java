package fr.cnrs.iremus.sherlock.e13;

import io.micronaut.core.annotation.Introspected;

import javax.validation.constraints.NotBlank;

@Introspected
public class NewE13 {
    @NotBlank
    private String p140_assigned_attribute_to;
    @NotBlank
    private String p177_assigned_property_type;
    @NotBlank
    private String p141_assigned;
    @NotBlank
    private String p141_type;

    public String getP140_assigned_attribute_to() {
        return p140_assigned_attribute_to;
    }

    public void setP140_assigned_attribute_to(String p140_assigned_attribute_to) {
        this.p140_assigned_attribute_to = p140_assigned_attribute_to;
    }

    public String getP177_assigned_property_type() {
        return p177_assigned_property_type;
    }

    public void setP177_assigned_property_type(String p177_assigned_property_type) {
        this.p177_assigned_property_type = p177_assigned_property_type;
    }

    public String getP141_assigned() {
        return p141_assigned;
    }

    public void setP141_assigned(String p141_assigned) {
        this.p141_assigned = p141_assigned;
    }

    public String getP141_type() {
        return p141_type;
    }

    public void setP141_type(String p141_type) {
        this.p141_type = p141_type;
    }
}
