package fr.cnrs.iremus.sherlock.pojo.selection;

import java.util.List;

import javax.validation.constraints.NotEmpty;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class SelectionCreate {

    @NotEmpty
    private List<String> children;

    public List<String> getChildren() {
        return children;
    }
    public void setChildren(List<String> children) {
        this.children = children;
    }
}
