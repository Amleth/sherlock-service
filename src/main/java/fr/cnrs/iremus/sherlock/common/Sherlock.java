package fr.cnrs.iremus.sherlock.common;

import jakarta.inject.Singleton;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Singleton
public class Sherlock {
    public static final String NS = "http://data-iremus.huma-num.fr/ns/sherlock#";
    private static final Model m_model = ModelFactory.createDefaultModel();
    public static final Resource sheP_a_pour_entite_de_plus_haut_niveau = m_model.createResource(NS + "sheP_a_pour_entité_de_plus_haut_niveau");
    public static final Resource sheP_subscribe = m_model.createResource(NS + "sheP_subscribe");
    public static final Property sheP_has_document_context = m_model.createProperty(NS + "sheP_has_document_context");

    public Resource getGraph() {
        return m_model.createResource("http://data-iremus.huma-num.fr/graph/sherlock");
    }

    public Resource getUserGraph() {
        return m_model.createResource("http://data-iremus.huma-num.fr/graph/users");
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

    public String makeUpdateQuery(Model m, Resource graph) {
        return "INSERT DATA { GRAPH <" + graph + "> {" + this.modelToString(m) + "}}";
    }

    public String makeDeleteQuery(Model m) {
        return "DELETE WHERE { GRAPH <" + this.getGraph() + "> {" + this.modelToString(m) + "}}";
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
        m.setNsPrefix("crm", CIDOCCRM.NS);
        m.setNsPrefix("iremus", this.getResourcePrefix());
        RDFDataMgr.write(outputStream, m, RDFFormat.JSONLD_EXPAND_FLAT);

        return outputStream.toString();
    }

    public String resolvePrefix(String uri) {
        return uri.replace("crm:", CIDOCCRM.getURI()).replace("dcterms:", DCTerms.getURI()).replace("rdf:", RDF.getURI()).replace("rdfs:", RDFS.getURI()).replace("sherlock:", NS);
    }

    public Literal getTypedLiteral(Model m, Datatype datatype, String literal) {
        switch (datatype) {
            case STRING:
                return m.createTypedLiteral(literal);
            case INTEGER:
                return m.createTypedLiteral(Integer.valueOf(literal));
            case DATE:
                Calendar calendar = Calendar.getInstance();
                Date date = Date.from(Instant.parse(literal));
                calendar.setTime(date);
                new XSDDateTime(calendar);
                return m.createTypedLiteral(calendar);
            default:
                return m.createLiteral(literal);
        }
    }

    public String getUuidFromSherlockUri(String uri) {
        String[] uriSplit = uri.split("/");
        return uriSplit[uriSplit.length - 1];
    }
}
