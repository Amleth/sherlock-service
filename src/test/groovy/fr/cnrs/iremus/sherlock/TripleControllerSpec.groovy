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

    void 'test uri to uri triple'() {
        when:
        common.fuck()
        String subjectIri = sherlock.makeIri()
        String propertyIri = CIDOCCRM.P1_is_identified_by
        String objectIri = sherlock.makeIri()

        String json = client.toBlocking().retrieve(common.makePostRequestWithAuthorization(client, '/triple', [
                "s": subjectIri,
                "p": propertyIri,
                "o": objectIri,
                "o_is_uri": true
        ]), String)

        then:
        Object response = new ObjectMapper().readValue(json, Object.class)
        response["@id"] == subjectIri
        response["P1_is_identified_by"] == objectIri
    }

    //Les erreurs >400 font échouer le test
    void 'test triple route fails without specifying o_is_uri'() {
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

    void 'test uri to basic literal triple'() {
        when:
        common.fuck()
        String subjectIri = sherlock.makeIri()
        String propertyIri = CIDOCCRM.P1_is_identified_by
        String literalIri = "literal string"

        String json = client.toBlocking().retrieve(common.makePostRequestWithAuthorization(client, '/triple', [
                "s": subjectIri,
                "p": propertyIri,
                "o": literalIri,
                "o_is_uri": false
        ]), String)

        then:
        Object response = new ObjectMapper().readValue(json, Object.class)
        response["@id"] == subjectIri
        response["P1_is_identified_by"] == literalIri
    }

    void 'test uri to typed literal triple'() {
        when:
        common.fuck()
        String subjectIri = sherlock.makeIri()
        String propertyIri = CIDOCCRM.P1_is_identified_by
        String literalIri = "2007-12-03T10:15:30Z"

        String json = client.toBlocking().retrieve(common.makePostRequestWithAuthorization(client, '/triple', [
                "s": subjectIri,
                "p": propertyIri,
                "o": literalIri,
                "o_is_uri": false,
                "o_type": "date"
        ]), String)

        then:
        Object response = new ObjectMapper().readValue(json, Object.class)
        response["@id"] == subjectIri
        response["P1_is_identified_by"] == literalIri
        response["@context"]["P1_is_identified_by"]["@type"] == "http://www.w3.org/2001/XMLSchema#dateTime"
    }

    void 'test uri to translated literal triple'() {
        when:
        common.fuck()
        String subjectIri = sherlock.makeIri()
        String propertyIri = CIDOCCRM.P1_is_identified_by
        String literalIri = "pain"

        String json = client.toBlocking().retrieve(common.makePostRequestWithAuthorization(client, '/triple', [
                "s": subjectIri,
                "p": propertyIri,
                "o": literalIri,
                "o_is_uri": false,
                "o_lg": "fr"
        ]), String)

        then:
        Object response = new ObjectMapper().readValue(json, Object.class)
        response["@id"] == subjectIri
        response["P1_is_identified_by"]["@value"] == literalIri
        response["P1_is_identified_by"]["@language"] == "fr"
    }

}
