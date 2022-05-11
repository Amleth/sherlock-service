package fr.cnrs.iremus.sherlock.service;

import fr.cnrs.iremus.sherlock.common.CIDOCCRM;
import fr.cnrs.iremus.sherlock.common.Sherlock;
import jakarta.inject.Inject;
import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;

public class SelectionService {

    @io.micronaut.context.annotation.Property(name = "jena")
    protected String jena;
    @Inject
    Sherlock sherlock;
    public Model getSelectionTriplesByResource(Resource selection) {
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

            // WRITE
            ConstructBuilder cb = new ConstructBuilder()
                    .addConstruct(selection, "?selection_p", "?selection_o")
                    .addGraph(sherlock.getGraph(), selection, "?selection_p", "?selection_o");
            Query q = cb.build();
            QueryExecution qe = conn.query(q);

            return qe.execConstruct();
        }
    }
    public Model getSelectionP106Children(Resource selection) {
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

            // WRITE
            ConstructBuilder cb = new ConstructBuilder()
                    .addConstruct(selection, CIDOCCRM.P106_is_composed_of, "?selection_o")
                    .addGraph(sherlock.getGraph(), selection, CIDOCCRM.P106_is_composed_of, "?selection_o");
            Query q = cb.build();
            QueryExecution qe = conn.query(q);

            return qe.execConstruct();
        }
    }
}
