package fr.cnrs.iremus.sherlock;

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
import java.util.Map;

@Controller("/resource")
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
    public String create(@Body Map<String, String> body, Authentication authentication) throws ParseException {
        // context
        String authenticatedUserUuid = (String) authentication.getAttributes().get("uuid");
        String now = dateService.getNow();
        // new resource
        String newResourceUri = sherlock.makeIri();
        String newResourceTypeUri = sherlock.resolvePrefix(body.get("rdf:type"));
        // e13 p1
        String identifier = sherlock.resolvePrefix(body.get("crm:P1_is_identified_by"));
        String e13p1Uri = sherlock.makeIri();

        // UPDATE QUERY
        Model m = ModelFactory.createDefaultModel();
        // new resource
        Resource newResourceResource = m.createResource(newResourceUri);
        Resource authenticatedUserResource = m.createResource(sherlock.makeIri(authenticatedUserUuid));
        m.add(newResourceResource, RDF.type, m.createResource(newResourceTypeUri));
        m.add(newResourceResource, DCTerms.creator, authenticatedUserResource);
        m.add(newResourceResource, DCTerms.created, now);
        // e13 p1
        Resource e13p1Resource = m.createResource(e13p1Uri);
        m.add(e13p1Resource, RDF.type, CIDOCCRM.E13_Attribute_Assignment);
        m.add(e13p1Resource, CIDOCCRM.P14_carried_out_by, authenticatedUserResource);
        m.add(e13p1Resource, CIDOCCRM.P140_assigned_attribute_to, newResourceResource);
        m.add(e13p1Resource, CIDOCCRM.P141_assigned, m.createLiteral(identifier));
        m.add(e13p1Resource, CIDOCCRM.P177_assigned_property_type, CIDOCCRM.P1_is_identified_by);
        m.add(e13p1Resource, DCTerms.created, now);
        String updateWithModel = sherlock.makeUpdateQuery(m);

        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

            // WRITE
            conn.update(updateWithModel);

            // AND READ IT BACK AS JSON-LD
            ConstructBuilder cb = new ConstructBuilder()
                    .addConstruct(newResourceResource, "?r_p", "?r_o")
                    .addConstruct(e13p1Resource, "?e13p1_p", "?e13p1_o")
                    .addGraph(sherlock.getGraph(), newResourceResource, "?r_p", "?r_o")
                    .addGraph(sherlock.getGraph(), e13p1Resource, "?e13p1_p", "?e13p1_o")
                    .addGraph(sherlock.getGraph(), e13p1Resource, CIDOCCRM.P140_assigned_attribute_to, newResourceResource);
            Query q = cb.build();
            QueryExecution qe = conn.query(q);
            Model res = qe.execConstruct();
            return sherlock.modelToJson(res);
        }
    }
}
