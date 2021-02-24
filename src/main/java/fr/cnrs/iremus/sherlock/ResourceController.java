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
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
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
        String authenticatedUserUuid = (String) authentication.getAttributes().get("uuid");
        String now = dateService.getNow();
        String newResourceUri = sherlock.makeUri();
        String newResourceTypeUri = sherlock.resolvePrefix(body.get("rdf:type"));

        // UPDATE QUERY
        Model m = ModelFactory.createDefaultModel();
        Resource newResourceResource = m.createResource(newResourceUri);
        Resource authenticatedUserUri = m.createResource(sherlock.getResourcePrefix() + authenticatedUserUuid);
        m.add(newResourceResource, RDF.type, m.createResource(newResourceTypeUri));
        m.add(newResourceResource, DCTerms.creator, authenticatedUserUri);
        m.add(newResourceResource, DCTerms.created, now);
        String updateWithModel = sherlock.makeUpdateQuery(m);

        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

            // WRITE

            conn.update(updateWithModel);

            // AND READ IT BACK

            SelectBuilder sb = new SelectBuilder()
                    .addVar("*")
                    .addGraph("<http://data-iremus.huma-num.fr/graph/sherlock>", "<" + newResourceUri + ">", "?e32_p", "?e32_o")
                    .addBind("<" + newResourceUri + ">", "?e32");
            Query q = sb.build();
            QueryExecution qe = conn.query(q);
            ResultSet results = qe.execSelect();

            return sherlock.resultSetToString(results);
        }
    }
}
