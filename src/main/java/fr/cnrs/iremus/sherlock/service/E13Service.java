package fr.cnrs.iremus.sherlock.service;

import fr.cnrs.iremus.sherlock.common.CIDOCCRM;
import fr.cnrs.iremus.sherlock.common.Sherlock;
import io.micronaut.security.authentication.Authentication;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;

@Singleton
public class E13Service {

    @Inject
    Sherlock sherlock;

    @Inject
    DateService dateService;

    public void insertNewE13(Resource e13, Resource p140, RDFNode p141, Resource p177, Model m, Authentication authentication) {
        String authenticatedUserUuid = (String) authentication.getAttributes().get("uuid");
        Resource authenticatedUser = m.createResource(sherlock.makeIri(authenticatedUserUuid));
        String now = dateService.getNow();

        m.add(e13, RDF.type, CIDOCCRM.E13_Attribute_Assignment);
        m.add(e13, CIDOCCRM.P14_carried_out_by, authenticatedUser);
        m.add(e13, CIDOCCRM.P140_assigned_attribute_to, p140);
        m.add(e13, CIDOCCRM.P141_assigned, p141);
        m.add(e13, CIDOCCRM.P177_assigned_property_of_type, p177);
        m.add(e13, DCTerms.created, now);

    }
}
