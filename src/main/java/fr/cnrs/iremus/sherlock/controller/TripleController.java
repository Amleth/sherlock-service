package fr.cnrs.iremus.sherlock.controller;

import fr.cnrs.iremus.sherlock.common.ResourceType;
import fr.cnrs.iremus.sherlock.common.Sherlock;
import fr.cnrs.iremus.sherlock.pojo.triple.TripleCreate;
import fr.cnrs.iremus.sherlock.pojo.triple.TripleReplace;
import fr.cnrs.iremus.sherlock.service.DateService;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import org.apache.http.HttpException;
import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.UpdateBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.update.Update;

import javax.validation.Valid;

@Controller("/api/triple")
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
    public String create(@Valid @Body TripleCreate body) throws HttpException {
        // retrieve request body
        String newTriplePredicateIri = sherlock.resolvePrefix(body.getP());

        // update query
        Model m = ModelFactory.createDefaultModel();

        // new triple
        Resource newTripleSubject = m.createResource(body.getS());
        org.apache.jena.rdf.model.Property newTripleProperty = m.createProperty(newTriplePredicateIri);
        if (body.getObject_type().equals(ResourceType.URI)) {
            Resource newTripleObject = m.createResource(body.getO());
            m.add(newTripleSubject, newTripleProperty, newTripleObject);
        }
        // is object a literal?
        else {
            Literal literal;
            // literal with lexical form
            if (body.getO_lg() != null) {
                literal = m.createLiteral(body.getO(), body.getO_lg());
            }
            // typed literal
            else if (body.getO_datatype() != null) {
                literal = sherlock.getTypedLiteral(m, body.getO_datatype(), body.getO());
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
            conn.update(updateWithModel);

            return sherlock.modelToJson(m);
        }
    }

    @Put
    @Produces(MediaType.APPLICATION_JSON)
    public String replace(@Valid @Body TripleReplace body) throws HttpException, ParseException {

        // retrieve request body
        String oldSubjectIri = body.getOld_s();
        String oldPredicateIri = sherlock.resolvePrefix(body.getOld_p());
        String oldObjectIri = body.getOld_o();
        String newSubjectIri = body.getNew_s();
        String newPredicateIri = sherlock.resolvePrefix(body.getNew_p());
        String newObjectIri = body.getNew_o();

        // update query
        Model m = ModelFactory.createDefaultModel();
        UpdateBuilder updateBuilder = new UpdateBuilder();

        // remove old triple
        Resource oldSubject = m.createResource(oldSubjectIri);
        org.apache.jena.rdf.model.Property oldProperty = m.createProperty(oldPredicateIri);
        Resource oldObject = m.createResource(oldObjectIri);
        updateBuilder.with(sherlock.getGraph());
        updateBuilder.addDelete(oldSubject, oldProperty, oldObject);
        updateBuilder.addWhere(oldSubject, oldProperty, oldObject);

        // add new triple
        Resource newSubject = m.createResource(newSubjectIri);
        org.apache.jena.rdf.model.Property newProperty = m.createProperty(newPredicateIri);
        Resource newObject = m.createResource(newObjectIri);
        updateBuilder.addInsert(newSubject, newProperty, newObject);

        Update update = updateBuilder.build();
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            conn.update(update);

            ConstructBuilder cb = new ConstructBuilder().addConstruct(newSubject, newProperty, newObject);
            Query q = cb.build();
            QueryExecution qe = conn.query(q);
            Model res = qe.execConstruct();

            return sherlock.modelToJson(res);

        }
    }
}
