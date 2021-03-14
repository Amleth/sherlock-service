package fr.cnrs.iremus.sherlock.resource;

import fr.cnrs.iremus.sherlock.CIDOCCRM;
import fr.cnrs.iremus.sherlock.DateService;
import fr.cnrs.iremus.sherlock.Sherlock;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;

import javax.inject.Inject;
import javax.validation.Valid;

@Controller("/sherlock/api/resource")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class ResourceController {

    @Property(name = "jena")
    protected String jena;

    @Inject
    DateService dateService;

    @Inject
    Sherlock sherlock;

    @Post
    @Produces(MediaType.APPLICATION_JSON)
    public String create(@Valid @Body NewResource body, Authentication authentication) throws ParseException {
        // context
        String authenticatedUserUuid = (String) authentication.getAttributes().get("uuid");
        String now = dateService.getNow();
        // new resource
        String newResourceIri = sherlock.makeIri();
        String newResourceTypeIri = sherlock.resolvePrefix(body.getType());
        // e13 p1
        String identifier = sherlock.resolvePrefix(body.getP1_is_identified_by());
        String e13p1Iri = sherlock.makeIri();

        // UPDATE QUERY
        Model m = ModelFactory.createDefaultModel();
        // new resource
        Resource newResource = m.createResource(newResourceIri);
        Resource authenticatedUser = m.createResource(sherlock.makeIri(authenticatedUserUuid));
        m.add(newResource, RDF.type, m.createResource(newResourceTypeIri));
        m.add(newResource, DCTerms.creator, authenticatedUser);
        m.add(newResource, DCTerms.created, now);
        // e13 p1
        Resource e13p1 = m.createResource(e13p1Iri);
        m.add(e13p1, RDF.type, CIDOCCRM.E13_Attribute_Assignment);
        m.add(e13p1, CIDOCCRM.P14_carried_out_by, authenticatedUser);
        m.add(e13p1, CIDOCCRM.P140_assigned_attribute_to, newResource);
        m.add(e13p1, CIDOCCRM.P141_assigned, m.createLiteral(identifier));
        m.add(e13p1, CIDOCCRM.P177_assigned_property_type, CIDOCCRM.P1_is_identified_by);
        m.add(e13p1, DCTerms.created, now);

        String updateWithModel = sherlock.makeUpdateQuery(m);

        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

            // WRITE
            conn.update(updateWithModel);

            // AND READ IT BACK AS JSON-LD
            ConstructBuilder cb = new ConstructBuilder()
                    .addConstruct(newResource, "?r_p", "?r_o")
                    .addConstruct(e13p1, "?e13p1_p", "?e13p1_o")
                    .addGraph(sherlock.getGraph(), newResource, "?r_p", "?r_o")
                    .addGraph(sherlock.getGraph(), e13p1, "?e13p1_p", "?e13p1_o")
                    .addGraph(sherlock.getGraph(), e13p1, CIDOCCRM.P140_assigned_attribute_to, newResource);
            Query q = cb.build();
            QueryExecution qe = conn.query(q);
            Model res = qe.execConstruct();

            return sherlock.modelToJson(res);
        }
    }
}
