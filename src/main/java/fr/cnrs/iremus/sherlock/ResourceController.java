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
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;

import javax.inject.Inject;
import java.util.Map;
import java.util.UUID;

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
    public String create(@Body Map<String, String> body, Authentication authentication) {
        String authenticatedUserUuid = (String) authentication.getAttributes().get("uuid");
        String now = dateService.getNow();
        String newResourceUri = sherlock.getResourcePrefix() + UUID.randomUUID().toString();
        String newResourceTypeUri = sherlock.resolvePrefix(body.get("rdf:type"));

        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

            // WRITE

            Model m = ModelFactory.createDefaultModel();
            Resource newResourceResource = m.createResource(newResourceUri);
            Resource authenticatedUserUri = m.createResource(sherlock.getResourcePrefix() + authenticatedUserUuid);
            m.add(newResourceResource, RDF.type, m.createResource(newResourceTypeUri));
            m.add(newResourceResource, DCTerms.creator, authenticatedUserUri);
            m.add(newResourceResource, DCTerms.created, now);
            String updateWithModel = sherlock.makeUpdateQuery(m);
            conn.update(updateWithModel);

            // AND READ IT BACK

            ParameterizedSparqlString pss = new ParameterizedSparqlString();
            pss.setCommandText("SELECT " +
                    "?resource ?type ?creator ?created" +
                    " WHERE { GRAPH ?graph {" +
                    "?r rdf:type ?type ." +
                    "?r dcterms:creator ?creator ." +
                    "?r dcterms:created ?created ." +
                    "BIND (?r AS ?resource)" +
                    "} }");
            pss.setNsPrefix("dcterms", DCTerms.getURI());
            pss.setNsPrefix("rdf", RDF.getURI());
            pss.setIri("graph", sherlock.getGraph());
            pss.setIri("r", newResourceUri);

            QueryExecution qe = conn.query(pss.asQuery());
            ResultSet results = qe.execSelect();

            return sherlock.resultSetToString(results);
        }
    }
}
