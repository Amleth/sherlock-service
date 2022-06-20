package fr.cnrs.iremus.sherlock.triple

import com.fasterxml.jackson.databind.ObjectMapper
import fr.cnrs.iremus.sherlock.Common
import fr.cnrs.iremus.sherlock.J
import fr.cnrs.iremus.sherlock.common.CIDOCCRM
import fr.cnrs.iremus.sherlock.common.Sherlock
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.rxjava2.http.client.RxHttpClient
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import spock.lang.Specification

@MicronautTest
class TripleControllerSpec extends Specification {
    @Inject
    @Client("/")
    RxHttpClient client
    @Inject
    Common common
    @Inject
    Sherlock sherlock

    void 'test post uri to uri triple'() {
        when:
        common.eraseall()
        String subjectIri = sherlock.makeIri()
        String propertyIri = CIDOCCRM.P1_is_identified_by
        String objectIri = sherlock.makeIri()

        def response = common.post('/sherlock/api/triple', [
                "s"          : subjectIri,
                "p"          : propertyIri,
                "o"          : objectIri,
                "object_type": "uri"
        ])

        then:
        response[0]["@id"] == subjectIri
        J.getIri(response[0], CIDOCCRM.P1_is_identified_by) == objectIri
    }

    //Les erreurs >400 font échouer le test
    void 'test post triple route fails without specifying object type'() {
        when:
        common.eraseall()
        String subjectIri = sherlock.makeIri()
        String propertyIri = CIDOCCRM.P1_is_identified_by
        String objectIri = sherlock.makeIri()
        common.post('/sherlock/api/triple', [
                "s": subjectIri,
                "p": propertyIri,
                "o": objectIri
        ])

        then:
        HttpClientResponseException e = thrown()
        e.getStatus().getCode() == 400
    }

    void 'test post uri to basic literal triple'() {
        when:
        common.eraseall()
        String subjectIri = sherlock.makeIri()
        String propertyIri = CIDOCCRM.P1_is_identified_by
        String aLiteral = "literal string"

        def response = common.post('/sherlock/api/triple', [
                "s"          : subjectIri,
                "p"          : propertyIri,
                "o"          : aLiteral,
                "object_type": "literal"
        ])

        then:
        response[0]["@id"] == subjectIri
        J.getLiteralValue(response[0], CIDOCCRM.P1_is_identified_by) == aLiteral
    }

    void 'test post uri to typed literal triple'() {
        when:
        common.eraseall()
        String subjectIri = sherlock.makeIri()
        String propertyIri = CIDOCCRM.P1_is_identified_by
        String aLiteral = "2007-12-03T10:15:30Z"

        def response = common.post('/sherlock/api/triple', [
                "s"          : subjectIri,
                "p"          : propertyIri,
                "o"          : aLiteral,
                "object_type": "literal",
                "o_datatype" : "date"
        ])

        then:
        response[0]["@id"] == subjectIri
        J.getLiteralValue(response[0], CIDOCCRM.P1_is_identified_by) == aLiteral
        J.getLiteralType(response[0], CIDOCCRM.P1_is_identified_by) == "http://www.w3.org/2001/XMLSchema#dateTime"
    }

    void 'test post uri to translated literal triple'() {
        when:
        common.eraseall()
        String subjectIri = sherlock.makeIri()
        String propertyIri = CIDOCCRM.P1_is_identified_by
        String aLiteral = "pain"

        def response = common.post('/sherlock/api/triple', [
                "s"          : subjectIri,
                "p"          : propertyIri,
                "o"          : aLiteral,
                "object_type": "literal",
                "o_lg"       : "fr"
        ])

        then:
        response[0]["@id"] == subjectIri
        J.getLiteralValue(response[0], CIDOCCRM.P1_is_identified_by) == aLiteral
        J.getLiteralLang(response[0], CIDOCCRM.P1_is_identified_by) == "fr"
    }

    //TODO test deletion
    void 'test put triple'() {
        when:
        common.eraseall()
        String oldSubjectIri = sherlock.makeIri()
        String oldPredicateIri = "crm:P1_is_identified_by"
        String oldObjectIri = sherlock.makeIri()
        String newSubjectIri = oldSubjectIri
        String newPredicateIri = "crm:P14_carried_out_by"
        String newObjectIri = sherlock.makeIri()

        def response = common.put('/sherlock/api/triple', [
                "old_s": oldSubjectIri,
                "old_p": oldPredicateIri,
                "old_o": oldObjectIri,
                "new_s": newSubjectIri,
                "new_p": newPredicateIri,
                "new_o": newObjectIri
        ])

        then:
        Model m = sherlock.getGraph().getModel()
        Resource r = m.getResource(oldSubjectIri)
        response[0]["@id"] == newSubjectIri
        J.getIri(response[0], CIDOCCRM.P14_carried_out_by) == newObjectIri
    }

    void 'test incoherent body triple creation'() {
        when:
        common.eraseall()
        String subjectIri = sherlock.makeIri()
        String propertyIri = CIDOCCRM.P1_is_identified_by
        String objectLiteral = "pain"

        def response = common.post('/sherlock/api/triple', [
                "s"          : subjectIri,
                "p"          : propertyIri,
                "o"          : objectLiteral,
                "object_type": "uri",
                "o_lg"       : "fr"
        ])

        then:
        HttpClientResponseException e = thrown()
        def m = new ObjectMapper().readValue(e.response.body(), Object.class)["message"]
        m == "body: You cannot define datatype or language if your object is uri"
    }
}
