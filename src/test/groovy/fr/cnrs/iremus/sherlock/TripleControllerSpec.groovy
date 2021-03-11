package fr.cnrs.iremus.sherlock

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

import javax.inject.Inject

@MicronautTest
class TripleControllerSpec extends Specification {
    @Inject
    @Client("/")
    RxHttpClient client
    @Inject
    Common common
    @Inject
    Sherlock sherlock;

    void 'test post uri to uri triple'() {
        when:
        common.fuck()
        String subjectIri = sherlock.makeIri()
        String propertyIri = CIDOCCRM.P1_is_identified_by
        String objectIri = sherlock.makeIri()

        String json = client.toBlocking().retrieve(common.makePostRequestWithAuthorization(client, '/triple', [
                "s"       : subjectIri,
                "p"       : propertyIri,
                "o"       : objectIri,
                "o_is_uri": true
        ]), String)

        then:
        Object response = new ObjectMapper().readValue(json, Object.class)
        response["@id"] == subjectIri
        response["P1_is_identified_by"] == objectIri
    }

    //Les erreurs >400 font échouer le test
    void 'test post triple route fails without specifying o_is_uri'() {
        when:
        common.fuck()
        String subjectIri = sherlock.makeIri()
        String propertyIri = CIDOCCRM.P1_is_identified_by
        String objectIri = sherlock.makeIri()
        /*String json = client.toBlocking().(common.makePostRequestWithAuthorization(client, '/triple', [
                "s": subjectIri,
                "p": propertyIri,
                "o": objectIri,
        ]))*/
        then:
        print("todo")
    }

    void 'test post uri to basic literal triple'() {
        when:
        common.fuck()
        String subjectIri = sherlock.makeIri()
        String propertyIri = CIDOCCRM.P1_is_identified_by
        String literalIri = "literal string"

        String json = client.toBlocking().retrieve(common.makePostRequestWithAuthorization(client, '/triple', [
                "s"       : subjectIri,
                "p"       : propertyIri,
                "o"       : literalIri,
                "o_is_uri": false
        ]), String)

        then:
        Object response = new ObjectMapper().readValue(json, Object.class)
        response["@id"] == subjectIri
        response["P1_is_identified_by"] == literalIri
    }

    void 'test post uri to typed literal triple'() {
        when:
        common.fuck()
        String subjectIri = sherlock.makeIri()
        String propertyIri = CIDOCCRM.P1_is_identified_by
        String literalIri = "2007-12-03T10:15:30Z"

        String json = client.toBlocking().retrieve(common.makePostRequestWithAuthorization(client, '/triple', [
                "s"       : subjectIri,
                "p"       : propertyIri,
                "o"       : literalIri,
                "o_is_uri": false,
                "o_type"  : "date"
        ]), String)

        then:
        Object response = new ObjectMapper().readValue(json, Object.class)
        response["@id"] == subjectIri
        response["P1_is_identified_by"] == literalIri
        response["@context"]["P1_is_identified_by"]["@type"] == "http://www.w3.org/2001/XMLSchema#dateTime"
    }

    void 'test post uri to translated literal triple'() {
        when:
        common.fuck()
        String subjectIri = sherlock.makeIri()
        String propertyIri = CIDOCCRM.P1_is_identified_by
        String literalIri = "pain"

        String json = client.toBlocking().retrieve(common.makePostRequestWithAuthorization(client, '/triple', [
                "s"       : subjectIri,
                "p"       : propertyIri,
                "o"       : literalIri,
                "o_is_uri": false,
                "o_lg"    : "fr"
        ]), String)

        then:
        Object response = new ObjectMapper().readValue(json, Object.class)
        response["@id"] == subjectIri
        response["P1_is_identified_by"]["@value"] == literalIri
        response["P1_is_identified_by"]["@language"] == "fr"
    }

    //TODO test deletion
    void 'test put triple'() {
        when:
        common.fuck()
        String oldSubjectIri = sherlock.makeIri()
        String oldPredicateIri = "crm:P1_is_identified_by"
        String oldObjectIri = sherlock.makeIri()
        String newSubjectIri = oldSubjectIri
        String newPredicateIri = "crm:P14_carried_out_by"
        String newObjectIri = sherlock.makeIri()

        String json = client.toBlocking().retrieve(common.makePutRequestWithAuthorization(client, '/triple', [
                "old_s": oldSubjectIri,
                "old_p": oldPredicateIri,
                "old_o": oldObjectIri,
                "new_s": newSubjectIri,
                "new_p": newPredicateIri,
                "new_o": newObjectIri,
        ]), String)

        then:

        Object response = new ObjectMapper().readValue(json, Object.class)
        response["@id"] == newSubjectIri
        response["P14_carried_out_by"] == newObjectIri
    }
}
