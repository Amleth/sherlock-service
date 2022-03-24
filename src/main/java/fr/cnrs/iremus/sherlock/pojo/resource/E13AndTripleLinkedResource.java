package fr.cnrs.iremus.sherlock.pojo.resource;

import fr.cnrs.iremus.sherlock.common.Triple;

import javax.validation.constraints.NotNull;
import java.util.List;

public class E13AndTripleLinkedResource extends E13LinkedResource {

    @NotNull
    @LinkedResourcesValidator
    private List<Triple> linked_triple;

    public List<Triple> getLinked_triple() {
        return linked_triple;
    }

    public void setLinked_triple(List<Triple> linked_triple) {
        this.linked_triple = linked_triple;
    }
}
