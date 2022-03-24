package fr.cnrs.iremus.sherlock.resource

import fr.cnrs.iremus.sherlock.Common
import fr.cnrs.iremus.sherlock.J
import fr.cnrs.iremus.sherlock.ValidateUUID
import fr.cnrs.iremus.sherlock.common.CIDOCCRM
import fr.cnrs.iremus.sherlock.common.Sherlock
import fr.cnrs.iremus.sherlock.service.DateService
import io.micronaut.http.client.annotation.Client
import io.micronaut.rxjava2.http.client.RxHttpClient
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import org.apache.jena.vocabulary.DCTerms
import spock.lang.Specification

@MicronautTest
class ResourceControllerSpec extends Specification {
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

        def token = common.getAccessToken("sherlock", "kcolrehs")

        def response = common.post(token, '/sherlock/api/resource', [
                type: "crm:E32_Authority_Document",
                p1  : "Ma liste de concepts"
        ])

        then:
        def e32 = J.getOneByType(response, CIDOCCRM.E32_Authority_Document)
        e32.keySet().size() == 4
        e32["@id"].startsWith(sherlock.getResourcePrefix())
        ValidateUUID.isValid(e32["@id"].split("/").last())
        J.getIri(e32, DCTerms.creator) == sherlock.makeIri("4b15a57d-8cae-43c5-8096-187b58d29327")
        dateService.isValidISODateTime(J.getLiteralValue(e32, DCTerms.created))

        def e13 = J.getOneByType(response, CIDOCCRM.E13_Attribute_Assignment)
        e13.keySet().size() == 7
        e13["@id"].startsWith(sherlock.getResourcePrefix())
        ValidateUUID.isValid(e13["@id"].split("/").last())
        dateService.isValidISODateTime(J.getLiteralValue(e13, DCTerms.created))
        J.getIri(e13, CIDOCCRM.P14_carried_out_by) == sherlock.makeIri("4b15a57d-8cae-43c5-8096-187b58d29327")
        J.getIri(e13, CIDOCCRM.P140_assigned_attribute_to) == e32["@id"]
        J.getLiteralValue(e13, CIDOCCRM.P141_assigned) == "Ma liste de concepts"
        J.getIri(e13, CIDOCCRM.P177_assigned_property_of_type) == CIDOCCRM.P1_is_identified_by.toString()
    }
}
