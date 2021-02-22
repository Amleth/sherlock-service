package fr.cnrs.iremus.sherlock;

import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;

import javax.inject.Singleton;
import java.io.ByteArrayOutputStream;

@Singleton
public class Sherlock {
    public String getCrmPrefix() {
        return "http://www.cidoc-crm.org/cidoc-crm/";
    }

    public String getGraph() {
        return "http://data-iremus.huma-num.fr/graph/sherlock";
    }

    public String getResourcePrefix() {
        return "http://data-iremus.huma-num.fr/id/";
    }

    public String makeUpdateQuery(Model m) {
        return "INSERT DATA { GRAPH <" + this.getGraph() + "> {" + this.modelToString(m) + "}}";
    }

    public String modelToString(Model m) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        m.write(baos, "Turtle");

        return baos.toString();
    }

    public String resultSetToString(ResultSet rs) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsJSON(outputStream, rs);

        return outputStream.toString();
    }

    public String resolvePrefix(String uri) {
        return uri.replace("crm:", this.getCrmPrefix());
    }
}
