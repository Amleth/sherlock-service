package fr.cnrs.iremus.sherlock.pojo.selection;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class SelectionCreate {
    @NotBlank
    private String sherlockns__has_document_context;

    @NotEmpty
    private List<String> children;

    public String getSherlockns__has_document_context() {
        return sherlockns__has_document_context;
    }

    public void setSherlockns__has_document_context(String sherlockns__has_document_context) {
        this.sherlockns__has_document_context = sherlockns__has_document_context;
    }

    public List<String> getChildren() {
        return children;
    }
    public void setChildren(List<String> children) {
        this.children = children;
    }
}
