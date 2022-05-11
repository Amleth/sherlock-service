package fr.cnrs.iremus.sherlock.controller;

import java.util.List;

import javax.validation.Valid;

import fr.cnrs.iremus.sherlock.service.DateService;
import fr.cnrs.iremus.sherlock.service.SelectionService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.authentication.Authentication;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;

import fr.cnrs.iremus.sherlock.common.CIDOCCRM;
import fr.cnrs.iremus.sherlock.common.Sherlock;
import fr.cnrs.iremus.sherlock.pojo.selection.SelectionCreate;
import io.micronaut.http.MediaType;
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

    @Inject
    DateService dateService;

    @Inject
    SelectionService selectionService;

    @Post
    @Produces(MediaType.APPLICATION_JSON)
    public String create(@Valid @Body SelectionCreate body, Authentication authentication) throws HttpException {
        // CREATE/GET IRIS
        List<String> children = body.getChildren().stream().map(child -> sherlock.resolvePrefix(child)).toList();
        String selectionIri = sherlock.makeIri();
        String now = dateService.getNow();
        String authenticatedUserUuid = (String) authentication.getAttributes().get("uuid");

        // BUILD MODEL
        Model m = ModelFactory.createDefaultModel();
        Resource authenticatedUser = m.createResource(sherlock.makeIri(authenticatedUserUuid));
        Resource selection = m.createResource(selectionIri);
        m.add(selection, RDF.type, CIDOCCRM.E28_Conceptual_Object);
        m.add(selection, DCTerms.created, now);
        m.add(selection, DCTerms.creator, authenticatedUser);
        children.forEach(child -> {
            Resource childResource = m.getResource(child);
            m.add(selection, CIDOCCRM.P106_is_composed_of, childResource);
        });

        String updateWithModel = sherlock.makeUpdateQuery(m);
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

            // WRITE
            conn.update(updateWithModel);

            return sherlock.modelToJson(selectionService.getSelectionTriplesByResource(selection));

        }
    }

    @Patch("/{selectionUuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public MutableHttpResponse<String> edit(@Valid @Body SelectionCreate body, @PathVariable String selectionUuid, Authentication authentication) throws HttpException {
        String now = dateService.getNow();
        String authenticatedUserUuid = (String) authentication.getAttributes().get("uuid");
        List<String> children = body.getChildren().stream().map(child -> sherlock.resolvePrefix(child)).toList();

        Model m = ModelFactory.createDefaultModel();
        Resource authenticatedUser = m.createResource(sherlock.makeIri(authenticatedUserUuid));
        Resource selection = m.getResource(sherlock.makeIri(selectionUuid));
        m.add(selection, DCTerms.modified, now);
        children.forEach(child -> {
            Resource childResource = m.getResource(child);
            m.add(selection, CIDOCCRM.P106_is_composed_of, childResource);
        });
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            /* Check request can be executed */

            Model currentModel = selectionService.getSelectionTriplesByResource(selection);
            if (!currentModel.containsResource(selection))
                return HttpResponse.notFound("This selection does not exist.");
            if (!currentModel.contains(selection, DCTerms.creator, authenticatedUser))
                return HttpResponse.unauthorized();

            /* Delete previous selection children */

            Model modelToRemove = selectionService.getSelectionP106Children(selection);
            String deleteWithModel = sherlock.makeDeleteQuery(modelToRemove);
            conn.update(deleteWithModel);

            /* Insert current children and DCTerms.modified */

            String updateWithModel = sherlock.makeUpdateQuery(m);
            conn.update(updateWithModel);

            return HttpResponse.ok(sherlock.modelToJson(selectionService.getSelectionTriplesByResource(selection)));
        }

    }
}