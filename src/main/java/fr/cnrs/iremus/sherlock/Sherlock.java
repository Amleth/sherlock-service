package fr.cnrs.iremus.sherlock;

import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import javax.inject.Singleton;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

@Singleton
public class Sherlock {
    private static final Model m_model = ModelFactory.createDefaultModel();

    public Resource getGraph() {
        return m_model.createResource("http://data-iremus.huma-num.fr/graph/sherlock");
    }

    public String getResourcePrefix() {
        return "http://data-iremus.huma-num.fr/id/";
    }

    public String makeIri(String id) {
        return "http://data-iremus.huma-num.fr/id/" + id;
    }

    public String makeIri() {
        return this.makeIri(UUID.randomUUID().toString());
    }

    public String makeUpdateQuery(Model m) {
        return "INSERT DATA { GRAPH <" + this.getGraph() + "> {" + this.modelToString(m) + "}}";
    }

    public String modelToString(Model m) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        m.write(baos, "Turtle");

        return baos.toString();
    }

    public String resultSetToJson(ResultSet rs) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsJSON(outputStream, rs);

        return outputStream.toString();
    }

    public String modelToJson(Model m) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        RDFDataMgr.write(outputStream, m, RDFFormat.JSONLD);

        return outputStream.toString();
    }

    public String resolvePrefix(String uri) {
        return uri
                .replace("crm:", CIDOCCRM.getURI())
                .replace("dcterms:", DCTerms.getURI())
                .replace("rdf:", RDF.getURI())
                .replace("rdfs:", RDFS.getURI());
    }
}
