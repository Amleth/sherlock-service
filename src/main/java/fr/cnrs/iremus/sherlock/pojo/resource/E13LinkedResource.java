package fr.cnrs.iremus.sherlock.pojo.resource;

import fr.cnrs.iremus.sherlock.pojo.e13.E13AsLink;
import io.micronaut.core.annotation.Introspected;

import javax.validation.constraints.NotNull;
import java.util.List;

@Introspected
public class E13LinkedResource extends NewResource {

    public List<E13AsLink> getE13_linked_resources() {
        return e13_linked_resources;
    }

    public void setE13_linked_resources(List<E13AsLink> linked_resources) {
        this.e13_linked_resources = linked_resources;
    }

    @NotNull
    @E13AsLinkValidator
    private List<E13AsLink> e13_linked_resources;
}
