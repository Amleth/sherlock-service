package fr.cnrs.iremus.sherlock.controller;

import java.util.List;

import javax.validation.Valid;

import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.vocabulary.RDF;

import fr.cnrs.iremus.sherlock.common.CIDOCCRM;
import fr.cnrs.iremus.sherlock.common.Sherlock;
import fr.cnrs.iremus.sherlock.pojo.selection.SelectionCreate;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.exceptions.HttpException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/selection")
public class SelectionController {
    @io.micronaut.context.annotation.Property(name = "jena")
    protected String jena;

    @Inject
    Sherlock sherlock;

    @Post
    @Produces(MediaType.APPLICATION_JSON)
    public String create(@Valid @Body SelectionCreate body) throws HttpException {
        // CREATE/GET IRIS
        String documentContextIri = sherlock.resolvePrefix(body.getSherlockns__has_document_context());
        List<String> children = body.getChildren().stream().map(child -> sherlock.resolvePrefix(child)).toList();
        String selectionIri = sherlock.makeIri();

        // BUILD MODEL
        Model m = ModelFactory.createDefaultModel();
        Resource selection = m.createResource(selectionIri);
        Resource documentContextResource = m.getResource(documentContextIri);
        m.add(selection, RDF.type, CIDOCCRM.E28_Conceptual_Object);
        m.add(selection, Sherlock.sheP_has_document_context, documentContextResource);
        children.forEach(child -> {
            Resource childResource = m.getResource(child);
            m.add(selection, CIDOCCRM.P106_is_composed_of, childResource);
        });

        String updateWithModel = sherlock.makeUpdateQuery(m);
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

            // WRITE
            conn.update(updateWithModel);

            ConstructBuilder cb = new ConstructBuilder()
                    .addConstruct(selection, "?selection_p", "?selection_o")
                    .addGraph(sherlock.getGraph(), selection, "?selection_p", "?selection_o");
            Query q = cb.build();
            QueryExecution qe = conn.query(q);
            Model res = qe.execConstruct();

            return sherlock.modelToJson(res);
        }
    }
}