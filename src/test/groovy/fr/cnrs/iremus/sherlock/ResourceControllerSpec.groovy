package fr.cnrs.iremus.sherlock

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

import javax.inject.Inject

@MicronautTest
class ResourceControllerSpec extends Specification {
    @Inject
    @Client("/")
    RxHttpClient client
    @Inject
    Common common
    @Inject
    DateService dateService;
    @Inject
    Sherlock sherlock;

    void 'test it works'() {
        when:
        common.fuck()
        String json = client.toBlocking().retrieve(common.makePostRequestWithAuthorization(client, '/resource', Map.of(
                "rdf:type", "crm:E32_Authority_Document",
                "crm:P1_is_identified_by", "Ma liste de concepts"
        )))

        then:
        Object response = new ObjectMapper().readValue(json, Object.class)

        Object e32 = response["@graph"].find { it["@type"] == "http://www.cidoc-crm.org/cidoc-crm/E32_Authority_Document" }
        e32.keySet().size() == 4
        e32["@id"].startsWith(sherlock.getResourcePrefix())
        e32["creator"] == sherlock.makeIri("4b15a57d-8cae-43c5-8096-187b58d29327")
        dateService.isValidISODateTime(e32["created"])

        Object e13 = response["@graph"].find { it["@type"] == "http://www.cidoc-crm.org/cidoc-crm/E13_Attribute_Assignment" }
        e13.keySet().size() == 7
        e13["@id"].startsWith(sherlock.getResourcePrefix())
        //TODO
    }
}
