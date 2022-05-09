package fr.cnrs.iremus.sherlock.selection

import fr.cnrs.iremus.sherlock.Common
import fr.cnrs.iremus.sherlock.J
import fr.cnrs.iremus.sherlock.common.CIDOCCRM
import fr.cnrs.iremus.sherlock.service.DateService
import io.micronaut.http.client.annotation.Client
import org.apache.jena.vocabulary.DCTerms
import spock.lang.Specification
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.rxjava2.http.client.RxHttpClient
import jakarta.inject.Inject
import fr.cnrs.iremus.sherlock.common.Sherlock

@MicronautTest
class SelectionControllerSpec extends Specification {

    @Inject
    @Client('/')
    RxHttpClient client

    @Inject
    DateService dateService

    @Inject
    Common common

    @Inject
    Sherlock sherlock

    void 'test post selection creates triples'() {
        when:
        common.eraseall()
        String documentContextIri = sherlock.makeIri()
        String child1Iri = sherlock.makeIri()
        String child2Iri = sherlock.makeIri()

        def token = common.getAccessToken()
        def response = common.post(token, '/sherlock/api/selection', [
                'sherlockns__has_document_context': documentContextIri,
                'children': [child1Iri, child2Iri],
        ])

        then:
        response[0]["@type"][0] == CIDOCCRM.E28_Conceptual_Object.URI
        response[0][CIDOCCRM.P106_is_composed_of.URI].find(child -> child["@id"] == child1Iri)
        response[0][CIDOCCRM.P106_is_composed_of.URI].find(child -> child["@id"] == child2Iri)
        dateService.isValidISODateTime(J.getLiteralValue(response[0], DCTerms.created))
        J.getIri(response[0], DCTerms.creator) == sherlock.makeIri("4b15a57d-8cae-43c5-8096-187b58d29327")
    }

}
