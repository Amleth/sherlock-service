package fr.cnrs.iremus.sherlock.pojo.resource;

import fr.cnrs.iremus.sherlock.common.Triple;
import io.micronaut.core.annotation.Introspected;

import javax.validation.constraints.NotNull;
import java.util.List;

@Introspected
public class LinkedResource extends NewResource {
    public List<Triple> getLinked_resources() {
        return linked_resources;
    }

    public void setLinked_resources(List<Triple> linked_resources) {
        this.linked_resources = linked_resources;
    }

    @NotNull
    @LinkedResourcesValidator
    private List<Triple> linked_resources;
}
