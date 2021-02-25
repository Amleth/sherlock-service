package fr.cnrs.iremus.sherlock;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class CIDOCCRM {
    public static final String NS = "http://www.cidoc-crm.org/cidoc-crm/";
    private static final Model m_model = ModelFactory.createDefaultModel();

    public static final Resource E1_CRM_Entity = m_model.createResource(NS + "E1_CRM_Entity");
    public static final Resource E13_Attribute_Assignment = m_model.createResource(NS + "E13_Attribute_Assignment");
    public static final Resource P1_is_identified_by = m_model.createResource(NS + "P1_is_identified_by");
    public static final Property P14_carried_out_by = m_model.createProperty(NS + "P14_carried_out_by");
    public static final Property P140_assigned_attribute_to = m_model.createProperty(NS + "P140_assigned_attribute_to");
    public static final Property P141_assigned = m_model.createProperty(NS + "P141_assigned");
    public static final Property P177_assigned_property_type = m_model.createProperty(NS + "P177_assigned_property_type");

    public static String getURI() {
        return NS;
    }
}
