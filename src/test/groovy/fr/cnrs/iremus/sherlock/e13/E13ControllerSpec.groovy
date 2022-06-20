package fr.cnrs.iremus.sherlock.e13

import fr.cnrs.iremus.sherlock.Common
import fr.cnrs.iremus.sherlock.J
import fr.cnrs.iremus.sherlock.ValidateUUID
import fr.cnrs.iremus.sherlock.common.CIDOCCRM
import fr.cnrs.iremus.sherlock.common.Sherlock
import fr.cnrs.iremus.sherlock.service.DateService
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.client.annotation.Client
import io.micronaut.rxjava2.http.client.RxHttpClient
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import org.apache.jena.vocabulary.DCTerms
import spock.lang.Specification

@MicronautTest
class E13ControllerSpec extends Specification {
    @Inject
    @Client("/")
    RxHttpClient client
    @Inject
    Common common
    @Inject
    DateService dateService
    @Inject
    Sherlock sherlock

    void 'test it works'() {
        when:
        common.eraseall()

        String annotatedResourceIri = sherlock.makeIri()
        String annotationProperty = DCTerms.title.toString()
        String annotationValue = "J'aime les framboises"

        def response = common.post('/sherlock/api/e13', [
                "p140"     : annotatedResourceIri,
                "p177"     : annotationProperty,
                "p141"     : annotationValue,
                "p141_type": "literal"
        ])

        then:
        response.size == 1
        response[0]["@id"].startsWith(sherlock.getResourcePrefix())
        ValidateUUID.isValid(response[0]["@id"].split("/").last())
        sherlock.resolvePrefix(response[0]["@type"]) == CIDOCCRM.E13_Attribute_Assignment.toString()
        dateService.isValidISODateTime(J.getLiteralValue(response[0], DCTerms.created))
        J.getIri(response[0], CIDOCCRM.P14_carried_out_by) == sherlock.makeIri("4b15a57d-8cae-43c5-8096-187b58d29327")
        J.getIri(response[0], CIDOCCRM.P140_assigned_attribute_to) == annotatedResourceIri
        J.getIri(response[0], CIDOCCRM.P177_assigned_property_of_type) == annotationProperty
        J.getLiteralValue(response[0], CIDOCCRM.P141_assigned) == annotationValue
    }
}
