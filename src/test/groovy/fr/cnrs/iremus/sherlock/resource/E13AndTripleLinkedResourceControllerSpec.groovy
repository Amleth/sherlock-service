package fr.cnrs.iremus.sherlock.resource

import fr.cnrs.iremus.sherlock.Common
import fr.cnrs.iremus.sherlock.J
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
class E13AndTripleLinkedResourceControllerSpec extends Specification {
    @Inject
    @Client("/")
    RxHttpClient client
    @Inject
    Common common
    @Inject
    DateService dateService
    @Inject
    Sherlock sherlock

    void 'test base e13AndTripleLinkedResource call'() {
        when:
        common.eraseall()
        def token = common.getAccessToken("sherlock", "kcolrehs")
        String iri = sherlock.makeIri()
        def response = common.post(token, '/sherlock/api/e13_and_triple_linked_resource', [
                type                : "crm:E1_CRM_Entity",
                p1                  : "Mon truc",
                e13_linked_resources: [
                        [
                                p177     : DCTerms.title.toString(),
                                p141     : "Titre",
                                p141_type: "literal"
                        ]
                ],
                linked_triple       : [
                        [
                                s: iri,
                                p: CIDOCCRM.P71_lists.toString(),
                        ]
                ]
        ])

        then:

        //region E13Linked_resource check
        def e1 = J.getOneByType(response, CIDOCCRM.E1_CRM_Entity)

        def e13WithNewResourceAsP140 = J.getAllByPO(response, CIDOCCRM.P140_assigned_attribute_to, e1["@id"])
        e13WithNewResourceAsP140.size() == 2

        def e13p1 = J.getOneByPO(e13WithNewResourceAsP140, CIDOCCRM.P177_assigned_property_of_type, CIDOCCRM.P1_is_identified_by)
        J.getLiteralValue(e13p1, CIDOCCRM.P141_assigned) == "Mon truc"

        def e13title = J.getOneByPO(e13WithNewResourceAsP140, CIDOCCRM.P177_assigned_property_of_type, DCTerms.title)
        J.getLiteralValue(e13title, CIDOCCRM.P141_assigned) == "Titre"

        //endregion

        //region linkedTriple check
        def e32 = response.find { it["@id"] == iri }
        J.getIri(e32, CIDOCCRM.P71_lists) == e1["@id"]
        //end region
    }
}