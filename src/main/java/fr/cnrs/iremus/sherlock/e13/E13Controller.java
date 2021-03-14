package fr.cnrs.iremus.sherlock.e13;

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

@Controller("/sherlock/api/e13")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class E13Controller {
    @Property(name = "jena")
    protected String jena;

    @Inject
    DateService dateService;

    @Inject
    Sherlock sherlock;

    @Post
    @Produces(MediaType.APPLICATION_JSON)
    public String create(@Valid @Body NewE13 body, Authentication authentication) throws ParseException {
        // context
        String authenticatedUserUuid = (String) authentication.getAttributes().get("uuid");
        String now = dateService.getNow();
        // new e13
        String e13Iri = sherlock.makeIri();
        String p140 = sherlock.resolvePrefix(body.getP140_assigned_attribute_to());
        String p177 = sherlock.resolvePrefix(body.getP177_assigned_property_type());
        String p141 = sherlock.resolvePrefix(body.getP141_assigned());
        String p141Type = sherlock.resolvePrefix(body.getP141_type());

        // UPDATE QUERY
        Model m = ModelFactory.createDefaultModel();
        Resource e13 = m.createResource(e13Iri);
        Resource authenticatedUser = m.createResource(sherlock.makeIri(authenticatedUserUuid));
        m.add(e13, RDF.type, CIDOCCRM.E13_Attribute_Assignment);
        m.add(e13, CIDOCCRM.P14_carried_out_by, authenticatedUser);
        m.add(e13, CIDOCCRM.P140_assigned_attribute_to, m.createResource(p140));
        m.add(e13, CIDOCCRM.P177_assigned_property_type, m.createResource(p177));
        if (p141Type.equals("uri")) {
            p141 = sherlock.resolvePrefix(p141);
            m.add(e13, CIDOCCRM.P141_assigned, m.createResource(p141));
        } else if (p141Type.equals("literal")) {
            m.add(e13, CIDOCCRM.P141_assigned, m.createLiteral(p141));
        }
        m.add(e13, DCTerms.created, now);

        String updateWithModel = sherlock.makeUpdateQuery(m);

        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

            // WRITE
            conn.update(updateWithModel);

            // AND READ IT BACK AS JSON-LD
            ConstructBuilder cb = new ConstructBuilder()
                    .addConstruct(e13, "?e13_p", "?e13_o")
                    .addGraph(sherlock.getGraph(), e13, "?e13_p", "?e13_o");
            Query q = cb.build();
            QueryExecution qe = conn.query(q);
            Model res = qe.execConstruct();

            return sherlock.modelToJson(res);
        }
    }
}
