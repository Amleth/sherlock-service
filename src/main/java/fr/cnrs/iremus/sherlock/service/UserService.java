package fr.cnrs.iremus.sherlock.service;

import fr.cnrs.iremus.sherlock.common.CIDOCCRM;
import fr.cnrs.iremus.sherlock.common.Sherlock;
import io.micronaut.context.annotation.Property;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.sparql.resultset.ResultSetMem;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

@Singleton
public class UserService {
    @Property(name = "jena")
    protected String jena;

    @Inject
    DateService dateService;

    @Inject
    Sherlock sherlock;


    /**
     * Create user if not exists in database
     *
     * @param orcid ORCID id from person who used OAuth2 protocol
     * @return E21_Person URI
     */
    public String createUserIfNotExists(String orcid) {
        String userUuid = getUuidByOrcid(orcid);
        if (userUuid != null) return userUuid;

        String e21Iri = sherlock.makeIri();
        String e42Iri = sherlock.makeIri();
        String now = dateService.getNow();


        // BUILD MODEL
        Model m = ModelFactory.createDefaultModel();
        Resource e21_user = m.createResource(e21Iri);
        Resource e42_identifier = m.createResource(e42Iri);
        m.add(e21_user, CIDOCCRM.P1_is_identified_by, e42_identifier);
        m.add(e21_user, RDF.type, CIDOCCRM.E21_Person);
        m.add(e21_user, DCTerms.created, now);
        m.add(e42_identifier, RDF.type, CIDOCCRM.E42_Identifier);
        m.add(e42_identifier, CIDOCCRM.P2_has_type, "orcid");
        m.add(e42_identifier, RDFS.label, orcid);

        String updateWithModel = sherlock.makeUpdateQuery(m);
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            // WRITE
            conn.update(updateWithModel);
            return e21_user.getURI();
        }
    }

    private String getUuidByOrcid(String orcid) {
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

            // WRITE
            SelectBuilder cb = new SelectBuilder()
                    .addVar( "*" )
                    .addGraph(sherlock.getGraph(),"?E42_Identifier", RDFS.label, orcid)
                    .addGraph(sherlock.getGraph(),"?E42_Identifier", RDF.type, CIDOCCRM.E42_Identifier)
                    .addGraph(sherlock.getGraph(),"?E21_Person", CIDOCCRM.P1_is_identified_by, "?E42_Identifier")
                    .addGraph(sherlock.getGraph(),"?E21_Person", RDF.type, CIDOCCRM.E21_Person);
            Query q = cb.build();
            QueryExecution qe = conn.query(q);
            ResultSetMem results = (ResultSetMem) ResultSetFactory.copyResults(qe.execSelect());
            return results.size() == 0 ? null : results.peek().get("?E21_Person").toString();
        }
    }
}
