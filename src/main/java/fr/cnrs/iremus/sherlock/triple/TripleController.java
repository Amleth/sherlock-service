package fr.cnrs.iremus.sherlock.triple;

import com.fasterxml.jackson.core.JsonParseException;
import fr.cnrs.iremus.sherlock.DateService;
import fr.cnrs.iremus.sherlock.Sherlock;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.http.hateoas.Link;
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
import javax.validation.Valid;
import java.util.Map;

@Controller("/sherlock/api/triple")
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
    public String create(@Valid @Body Triple body) throws HttpException {

        // retrieve request body
        String newTriplePredicateIri = sherlock.resolvePrefix(body.getP());

        // UPDATE QUERY
        Model m = ModelFactory.createDefaultModel();

        // new triple
        Resource newTripleSubject = m.createResource(body.getS());
        org.apache.jena.rdf.model.Property newTripleProperty = m.createProperty(newTriplePredicateIri); //alias pour import ?
        if (body.getO_is_uri()) {
            Resource newTripleObject = m.createResource(body.getO());
            m.add(newTripleSubject, newTripleProperty, newTripleObject);
            // is object a literal
        } else {
            Literal literal;
            // literal with lexical form
            if (body.getO_lg() != null) {
                literal = m.createLiteral(body.getO(), body.getO_lg());
            }
            // typed literal
            else if (body.getO_type() != null) {
                literal = sherlock.getTypedLiteral(m, body.getO_type(), body.getO());
            }
            // basic literal
            else {
                literal = m.createLiteral(body.getO());
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

//    @Error(exception = HttpException.class)
//    public HttpResponse<String> handleError(Throwable e) {
//        return HttpResponse.<String>status(HttpStatus.BAD_REQUEST).body(e.getMessage());
//    }
}
