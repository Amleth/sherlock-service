package fr.cnrs.iremus.sherlock


import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import org.apache.jena.vocabulary.DCTerms
import spock.lang.Specification

import javax.inject.Inject

@MicronautTest
class E13ControllerSpec extends Specification {
    @Inject
    @Client("/")
    RxHttpClient client
    @Inject
    Common common
    @Inject
    DateService dateService;
    @Inject
    Sherlock sherlock;

    void 'test it works'() {
        when:
        common.fuck()

        String annotatedResourceIri = sherlock.makeIri()
        String annotationProperty = DCTerms.title.toString()
        String annotationValue = "J'aime les framboises"

        def token = common.getAccessToken("sherlock", "password")

        def response = common.post(token, '/sherlock/api/e13', [
                "p140_assigned_attribute_to" : annotatedResourceIri,
                "p177_assigned_property_type": annotationProperty,
                "p141_assigned"              : annotationValue,
                "p141_type"                  : "literal"
        ])

        then:
        response["@id"].startsWith(sherlock.getResourcePrefix())
        ValidateUUID.isValid(response["@id"].split("/").last())
        response["@type"] == CIDOCCRM.E13_Attribute_Assignment.toString()
        dateService.isValidISODateTime(response["created"])
        response["P14_carried_out_by"] == sherlock.makeIri("4b15a57d-8cae-43c5-8096-187b58d29327")
        response["P140_assigned_attribute_to"] == annotatedResourceIri
        response["P177_assigned_property_type"] == annotationProperty
        response["P141_assigned"] == annotationValue
    }
}
