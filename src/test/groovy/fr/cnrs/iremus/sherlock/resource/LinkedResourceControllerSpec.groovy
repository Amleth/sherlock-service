package fr.cnrs.iremus.sherlock.resource

import fr.cnrs.iremus.sherlock.Common
import fr.cnrs.iremus.sherlock.J
import fr.cnrs.iremus.sherlock.common.CIDOCCRM
import fr.cnrs.iremus.sherlock.common.Sherlock
import fr.cnrs.iremus.sherlock.service.DateService
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.rxjava2.http.client.RxHttpClient
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import org.apache.jena.vocabulary.DCTerms
import spock.lang.Specification

@MicronautTest
class LinkedResourceControllerSpec extends Specification {
    @Inject
    @Client("/")
    RxHttpClient client
    @Inject
    Common common
    @Inject
    DateService dateService
    @Inject
    Sherlock sherlock

    void 'test base linked_resource call'() {
        when:
        common.eraseall()
        def token = common.getAccessToken("sherlock", "kcolrehs")

        def response = common.post(token, '/sherlock/api/linked_resource', [
                type            : "crm:E1_CRM_Entity",
                p1              : "Mon truc",
                linked_resources: [
                        [
                                p: DCTerms.title.toString(),
                                o: "http://www.google.com"
                        ],
                        [
                                p: DCTerms.requires.toString(),
                                s: "http://www.blabla.com"
                        ]
                ]
        ])

        then:
        def e1AsSubject = J.getOneByType(response, CIDOCCRM.E1_CRM_Entity)
        dateService.isValidISODateTime(J.getLiteralValue(e1AsSubject, DCTerms.created))
        J.getIri(e1AsSubject, DCTerms.creator) == sherlock.makeIri("4b15a57d-8cae-43c5-8096-187b58d29327")
        J.getIri(e1AsSubject, DCTerms.title) == "http://www.google.com"

        def e1AsObject = response.find { it["@id"] == "http://www.blabla.com" }
        J.getIri(e1AsObject, DCTerms.requires) == e1AsSubject["@id"]

        def e13 = J.getOneByType(response, CIDOCCRM.E13_Attribute_Assignment)
        J.getIri(e13, CIDOCCRM.P140_assigned_attribute_to) == e1AsSubject["@id"]
        J.getLiteralValue(e13, CIDOCCRM.P141_assigned) == "Mon truc"
    }

    void 'test with full triple linked'() {
        when:
        common.eraseall()
        def token = common.getAccessToken("sherlock", "kcolrehs")

        common.post(token, '/sherlock/api/linked_resource', [
                type            : "crm:E1_CRM_Entity",
                p1              : "Mon truc",
                linked_resources: [
                        [
                                s: "http://www.blabla.com",
                                p: DCTerms.requires.toString(),
                                o: "http://www.blabla.com"
                        ]
                ]
        ])

        then:
        HttpClientResponseException e = thrown()
        e.message == "body.linked_resources: Wrong body. Should be <s> and <p>, or <p> and <o>"
    }

    void 'test without resource linked'() {
        when:
        common.eraseall()
        def token = common.getAccessToken("sherlock", "kcolrehs")

        common.post(token, '/sherlock/api/linked_resource', [
                type: "crm:E1_CRM_Entity",
                p1  : "Mon truc",
        ])

        then:
        HttpClientResponseException e = thrown()
        e.message == "Bad Request"
    }
}
