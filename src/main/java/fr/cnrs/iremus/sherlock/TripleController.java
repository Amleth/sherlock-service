package fr.cnrs.iremus.sherlock;

import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import org.apache.http.HttpException;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import javax.inject.Inject;
import java.util.Map;

@Controller("/triple")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class TripleController {

    @Property(name = "jena")
    protected String jena;

    @Inject
    DateService dateService;

    @Inject
    Sherlock sherlock;

    @Post
    @Produces(MediaType.APPLICATION_JSON)
    public String create(@Body Map<String, String> body) throws HttpException {

        // retrieve request body
        String newTripleSubjectIri = body.get("s");
        String newTriplePredicateIri = sherlock.resolvePrefix(body.get("p"));
        String newTripleObjectIriOrLiteral = body.get("o");
        if (newTripleSubjectIri == null || newTriplePredicateIri == null
                || newTripleObjectIriOrLiteral == null || body.get("o_is_uri") == null ) {
            throw new HttpException("missing parameter");
        }
        boolean newTripleObjectIsUri = body.get("o_is_uri").equals("true");
        String newTripleObjectType = body.get("o_type");
        String newTripleObjectLanguage = body.get("o_lg");

        // UPDATE QUERY
        Model m = ModelFactory.createDefaultModel();

        // new triple
        Resource newTripleSubject = m.createResource(newTripleSubjectIri);
        org.apache.jena.rdf.model.Property newTripleProperty = m.createProperty(newTriplePredicateIri); //alias pour import ?
        if (newTripleObjectIsUri) {
            Resource newTripleObject = m.createResource(newTripleObjectIriOrLiteral);
            m.add(newTripleSubject, newTripleProperty, newTripleObject);
        // is object a literal
        } else {
            Literal literal;
            // typed literal
            if (newTripleObjectType != null) {
                literal = sherlock.getTypedLiteral(m, newTripleObjectType, newTripleObjectIriOrLiteral);
            // literal with lexical form
            } else if (newTripleObjectLanguage != null) {
                literal = m.createLiteral(newTripleObjectIriOrLiteral, newTripleObjectLanguage);
            // basic literal
            } else {
                literal = m.createLiteral(newTripleObjectIriOrLiteral);
            }
            m.add(newTripleSubject, newTripleProperty, literal);
        }
        String updateWithModel = sherlock.makeUpdateQuery(m);

        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

            // WRITE
            conn.update(updateWithModel);

            return sherlock.modelToJson(m);
        }
    }

    @Error(exception = HttpException.class)
    public HttpResponse<String> handleError(Throwable e) {
        return HttpResponse.<String>status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}
