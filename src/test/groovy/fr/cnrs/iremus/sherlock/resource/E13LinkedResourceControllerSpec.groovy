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
class E13LinkedResourceControllerSpec extends Specification {
    @Inject
    @Client("/")
    RxHttpClient client
    @Inject
    Common common
    @Inject
    DateService dateService
    @Inject
    Sherlock sherlock

    void 'test base e13_linked_resource call'() {
        when:
        common.eraseall()
        String iri = sherlock.makeIri()
        def response = common.post('/sherlock/api/e13_linked_resource', [
                type                : "crm:E1_CRM_Entity",
                p1                  : "Mon truc",
                e13_linked_resources: [
                        [
                                p177     : DCTerms.title.toString(),
                                p141     : "Titre",
                                p141_type: "literal"
                        ],
                        [
                                p177: CIDOCCRM.P150_defines_typical_parts_of.toString(),
                                p140: iri,
                        ]
                ]
        ])

        then:
        def e1 = J.getOneByType(response, CIDOCCRM.E1_CRM_Entity.toString())

        def e13WithNewResourceAsP140 = J.getAllByPO(response, CIDOCCRM.P140_assigned_attribute_to, e1["@id"])
        e13WithNewResourceAsP140.size() == 2

        def e13p1 = J.getOneByPO(e13WithNewResourceAsP140, CIDOCCRM.P177_assigned_property_of_type, CIDOCCRM.P1_is_identified_by)
        J.getLiteralValue(e13p1, CIDOCCRM.P141_assigned) == "Mon truc"

        def e13title = J.getOneByPO(e13WithNewResourceAsP140, CIDOCCRM.P177_assigned_property_of_type, DCTerms.title)
        J.getLiteralValue(e13title, CIDOCCRM.P141_assigned) == "Titre"

        def e13WithExistingResourceAsP140 = J.getAllByPO(response, CIDOCCRM.P140_assigned_attribute_to, iri)
        e13WithExistingResourceAsP140.size() == 1

        def e13p150 = J.getOneByPO(e13WithExistingResourceAsP140, CIDOCCRM.P177_assigned_property_of_type, CIDOCCRM.P150_defines_typical_parts_of)
        println J.getIri(e13p150, CIDOCCRM.P141_assigned)
        J.getIri(e13p150, CIDOCCRM.P141_assigned) == e1["@id"]
    }

    void 'test e13_linked_resource call fails if e13_linked_resources malformed 2'() {
        when:
        common.eraseall()
        String iri = sherlock.makeIri()
        def response = common.post('/sherlock/api/e13_linked_resource', [
                type                : "crm:E1_CRM_Entity",
                p1                  : "Mon truc",
                e13_linked_resources: [
                        [
                                p177: DCTerms.title.toString(),
                                p141: "Titre",
                        ],
                        [
                                p177: CIDOCCRM.P150_defines_typical_parts_of.toString(),
                                p140: iri,
                                p141: "Titre",
                        ]
                ]
        ])

        then:
        HttpClientResponseException e = thrown()
        e.message == "body.e13_linked_resources: Wrong body. Should contain either P140 or P141 with its type but not both"
    }

    void 'test e13_linked_resource call fails if e13_linked_resources malformed 1'() {
        when:
        common.eraseall()
        String iri = sherlock.makeIri()
        def response = common.post('/sherlock/api/e13_linked_resource', [
                type                : "crm:E1_CRM_Entity",
                p1                  : "Mon truc",
                e13_linked_resources: [
                        [
                                p177: DCTerms.title.toString(),
                                p141: "Titre",
                        ],
                        [
                                p177: CIDOCCRM.P150_defines_typical_parts_of.toString(),
                                p140: iri,
                        ]
                ]
        ])

        then:
        HttpClientResponseException e = thrown()
        e.message == "body.e13_linked_resources: Wrong body. Should contain either P140 or P141 with its type but not both"
    }
}