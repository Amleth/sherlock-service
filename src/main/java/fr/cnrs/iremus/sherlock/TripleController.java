package fr.cnrs.iremus.sherlock;

import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.annotation.Error;
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

    @Put
    @Produces(MediaType.APPLICATION_JSON)
    public String replace(@Body Map<String, String> body) throws HttpException {

        // retrieve request body
        String oldSubjectIri = body.get("old_s");
        String oldPredicateIri = sherlock.resolvePrefix(body.get("old_p"));
        String oldObjectIri = body.get("old_o");
        String newSubjectIri = body.get("new_s");
        String newPredicateIri = sherlock.resolvePrefix(body.get("new_p"));
        String newObjectIri = body.get("new_o");

        if (oldSubjectIri == null || oldPredicateIri == null
                || oldObjectIri == null || newSubjectIri == null
                || newPredicateIri == null || newObjectIri == null) {
            throw new HttpException("missing parameter");
        }

        // UPDATE QUERY
        Model m = ModelFactory.createDefaultModel();

        // remove old triple
        Resource oldSubject = m.createResource(oldSubjectIri);
        org.apache.jena.rdf.model.Property oldProperty = m.createProperty(oldPredicateIri);
        Resource oldObject = m.createResource(oldObjectIri);
        m.add(oldSubject, oldProperty, oldObject);
        String deleteWithModel = sherlock.makeDeleteQuery(m);
        m.remove(oldSubject, oldProperty, oldObject);

        // add new triple
        Resource newSubject = m.createResource(newSubjectIri);
        org.apache.jena.rdf.model.Property newProperty = m.createProperty(newPredicateIri);
        Resource newObject = m.createResource(newObjectIri);
        m.add(newSubject, newProperty, newObject);
        String updateWithModel = sherlock.makeUpdateQuery(m);

        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

            // WRITE
            conn.update(updateWithModel);
            conn.update(deleteWithModel);

            return sherlock.modelToJson(m);
        }
    }

    @Error(exception = HttpException.class)
    public HttpResponse<String> handleError(Throwable e) {
        return HttpResponse.<String>status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}
