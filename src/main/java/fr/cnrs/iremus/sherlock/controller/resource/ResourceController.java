package fr.cnrs.iremus.sherlock.controller.resource;

import fr.cnrs.iremus.sherlock.common.CIDOCCRM;
import fr.cnrs.iremus.sherlock.common.Sherlock;
import fr.cnrs.iremus.sherlock.pojo.resource.NewResource;
import fr.cnrs.iremus.sherlock.service.DateService;
import fr.cnrs.iremus.sherlock.service.ResourceService;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.sparql.lang.sparql_11.ParseException;

import javax.validation.Valid;

@Controller("/api/resource")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class ResourceController {

    @Property(name = "jena")
    protected String jena;

    @Inject
    ResourceService resourceService;

    @Inject
    DateService dateService;

    @Inject
    Sherlock sherlock;

    @Post
    @Produces(MediaType.APPLICATION_JSON)
    public String create(@Valid @Body NewResource body, Authentication authentication) throws ParseException {
        Model m = ModelFactory.createDefaultModel();
        // new resource
        String newResourceIri = sherlock.makeIri();
        Resource newResource = m.createResource(newResourceIri);
        // e13 p1
        String e13p1Iri = sherlock.makeIri();
        Resource e13p1 = m.createResource(e13p1Iri);

        resourceService.insertNewResource(body, m, newResource, e13p1, authentication);
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
