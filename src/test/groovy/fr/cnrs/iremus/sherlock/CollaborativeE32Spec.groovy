package fr.cnrs.iremus.sherlock


import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

import javax.inject.Inject

@MicronautTest
class CollaborativeE32Spec extends Specification {

    @Inject
    Common common

    static final String U1 = "64b56930-8f55-41b1-bed0-58552a98663c"
    static final String U2 = "606697dc-4075-41cf-97e6-2c87edf5a004"

    void 'test it works'() {
        when:
        common.fuck()

        String u1Token = common.getAccessToken("sherlock", "password")
        String u2Token = common.getAccessToken("hudson", "password")

        // U1 crée un E32
        def E32 = common.post(u1Token, '/resource', [
                type               : "crm:E32_Authority_Document",
                p1_is_identified_by: "Mon thésaurus"
        ])

        // U1 crée un premier concept
        def E55A = common.post(u1Token, '/linked_resource', [
                type               : "crm:E55_Type",
                p1_is_identified_by: "Mon premier concept",
                "links"            : [
                        [
                                p140_assigned_attribute_to : E32["@id"],
                                p177_assigned_property_type: Sherlock.sheP_a_pour_entité_de_plus_haut_niveau.toString()
                        ],
                        [
                                p140_assigned_attribute_to : E32["@id"],
                                p177_assigned_property_type: CIDOCCRM.P71_lists.toString()
                        ]
                ]
        ])

        // U1 crée un deuxième concept
        def E55B = common.post(u1Token, '/linked_resource', [
                type               : "crm:E55_Type",
                p1_is_identified_by: "Mon deuxième concept",
                "links"            : [
                        [
                                p177_assigned_property_type: CIDOCCRM.P127_has_broader_term,
                                p141_assigned_attribute_to : E55A["@id"]
                        ], [
                                p140_assigned_attribute_to : E32["@id"],
                                p177_assigned_property_type: CIDOCCRM.P71_lists.toString()
                        ]
                ]
        ])

        // U2 s'abonne au E32 créé par U1
        def sub = common.post(u1Token, '/triple', [
                s     : U2,
                p     : Sherlock.sheP_subscribe,
                o     : E32["@id"],
                o_type: "uri"
        ])

        then:
        true
    }
}
