package fr.cnrs.iremus.sherlock.service;

import fr.cnrs.iremus.sherlock.common.CIDOCCRM;
import fr.cnrs.iremus.sherlock.common.ResourceType;
import fr.cnrs.iremus.sherlock.common.Sherlock;
import fr.cnrs.iremus.sherlock.common.Triple;
import fr.cnrs.iremus.sherlock.pojo.e13.E13AsLink;
import fr.cnrs.iremus.sherlock.pojo.resource.NewResource;
import io.micronaut.context.annotation.Property;
import io.micronaut.security.authentication.Authentication;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;

import java.util.List;

@Singleton
public class ResourceService {


    @Property(name = "jena")
    protected String jena;
    @Inject
    DateService dateService;
    @Inject
    E13Service e13Service;

    @Inject
    Sherlock sherlock;

    public void insertNewResource(NewResource body, Model m, Resource newResource, Resource e13p1, Authentication authentication) {
        String authenticatedUserUuid = (String) authentication.getAttributes().get("uuid");
        String now = dateService.getNow();
        // new resource
        String newResourceTypeIri = sherlock.resolvePrefix(body.getType());

        // new resource
        Resource authenticatedUser = m.createResource(sherlock.makeIri(authenticatedUserUuid));
        m.add(newResource, RDF.type, m.createResource(newResourceTypeIri));
        m.add(newResource, DCTerms.creator, authenticatedUser);
        m.add(newResource, DCTerms.created, now);
        // e13 p1
        m.add(e13p1, RDF.type, CIDOCCRM.E13_Attribute_Assignment);
        m.add(e13p1, CIDOCCRM.P14_carried_out_by, authenticatedUser);
        m.add(e13p1, CIDOCCRM.P140_assigned_attribute_to, newResource);
        m.add(e13p1, CIDOCCRM.P141_assigned, m.createLiteral(body.getP1()));
        m.add(e13p1, CIDOCCRM.P177_assigned_property_of_type, CIDOCCRM.P1_is_identified_by);
        m.add(e13p1, DCTerms.created, now);
    }

    public void insertTripleLinked(List<Triple> linked_triple, Model m, Resource newResource) {
        linked_triple.forEach(triple -> {
            org.apache.jena.rdf.model.Property p = m.createProperty(triple.getP());
            if (triple.getS() != null) {
                m.add(m.createResource(triple.getS()), p, newResource);
            } else {
                m.add(newResource, p, m.createResource(triple.getO()));
            }
        });
    }

    public void insertE13LinkedResources(List<E13AsLink> e13_linked_resources, Model m, Resource newResource, Authentication authentication) {
        e13_linked_resources.forEach(e13AsLink -> {
            String e13Iri = sherlock.makeIri();
            Resource e13 = m.createResource(e13Iri);
            if (e13AsLink.getP141() == null) {
                Resource p140 = m.createResource(e13AsLink.getP140());
                e13Service.insertNewE13(e13, p140, newResource, m.createResource(e13AsLink.getP177()), m, authentication);
            } else {
                RDFNode p141 = null;
                if (e13AsLink.getP141_type().equals(ResourceType.URI)) {
                    p141 = m.createResource(sherlock.resolvePrefix(e13AsLink.getP141()));
                } else if (e13AsLink.getP141_type().equals(ResourceType.LITERAL)) {
                    p141 = m.createLiteral(e13AsLink.getP141());
                }
                e13Service.insertNewE13(e13, newResource, p141, m.createResource(e13AsLink.getP177()), m, authentication);
            }
        });

    }
}
