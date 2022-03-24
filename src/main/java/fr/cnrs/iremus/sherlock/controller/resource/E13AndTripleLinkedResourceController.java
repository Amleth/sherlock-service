package fr.cnrs.iremus.sherlock.controller.resource;

import fr.cnrs.iremus.sherlock.common.CIDOCCRM;
import fr.cnrs.iremus.sherlock.common.Sherlock;
import fr.cnrs.iremus.sherlock.pojo.resource.E13AndTripleLinkedResource;
import fr.cnrs.iremus.sherlock.service.DateService;
import fr.cnrs.iremus.sherlock.service.E13Service;
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

@Controller("/api/e13_and_triple_linked_resource")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class E13AndTripleLinkedResourceController {

    @Property(name = "jena")
    protected String jena;

    @Inject
    ResourceService resourceService;

    @Inject
    Sherlock sherlock;

    @Inject
    DateService dateService;

    @Inject
    E13Service e13Service;

    @Post
    @Produces(MediaType.APPLICATION_JSON)
    public String createLinkedResource(@Valid @Body E13AndTripleLinkedResource body, Authentication authentication) throws ParseException {
        Model m = ModelFactory.createDefaultModel();
        // new resource
        String newResourceIri = sherlock.makeIri();
        Resource newResource = m.createResource(newResourceIri);
        // e13 p1
        String e13p1Iri = sherlock.makeIri();
        Resource e13p1 = m.createResource(e13p1Iri);

        resourceService.insertNewResource(body, m, newResource, e13p1, authentication);

        // e13 linked resources
        resourceService.insertE13LinkedResources(body.getE13_linked_resources(), m, newResource, authentication);

        // resourcesLinked
        resourceService.insertTripleLinked(body.getLinked_triple(), m, newResource);

        String updateWithModel = sherlock.makeUpdateQuery(m);

        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

            // WRITE
            conn.update(updateWithModel);

            // AND READ IT BACK AS JSON-LD
            ConstructBuilder cb = new ConstructBuilder()
                    .addConstruct(newResource, "?r_p", "?r_o")
                    .addConstruct("?r_s", "?r_p2", newResource)
                    .addConstruct(e13p1, "?e13_p", "?e13_o")
                    .addConstruct("?e13a", "?e13a_p", "?e13a_o")
                    .addConstruct("?e13b", "?e13b_p", "?e13b_o")

                    .addGraph(sherlock.getGraph(), "?r_s", "?r_p2", newResource)
                    .addGraph(sherlock.getGraph(), newResource, "?r_p", "?r_o")
                    .addGraph(sherlock.getGraph(), e13p1, "?e13_p", "?e13_o")

                    .addOptional(
                            new ConstructBuilder()
                                    .addGraph(sherlock.getGraph(), "?e13a", CIDOCCRM.P141_assigned, newResource)
                                    .addGraph(sherlock.getGraph(), "?e13a", "?e13a_p", "?e13a_o")
                    )

                    .addOptional(
                            new ConstructBuilder()
                                    .addGraph(sherlock.getGraph(), "?e13b", CIDOCCRM.P140_assigned_attribute_to, newResource)
                                    .addGraph(sherlock.getGraph(), "?e13b", "?e13b_p", "?e13b_o"));
            Query q = cb.build();
            QueryExecution qe = conn.query(q);
            Model res = qe.execConstruct();

            return sherlock.modelToJson(res);
        }
    }
}
