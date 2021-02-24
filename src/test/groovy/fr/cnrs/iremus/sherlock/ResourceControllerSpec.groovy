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
        String response = client.toBlocking().retrieve(common.makePostRequestWithAuthorization(client, '/resource', Map.of("rdf:type", "crm:E32_Authority_Document")))

        then:
        Object json = new ObjectMapper().readValue(response, Object.class)
        json.results.bindings.size() == 3
        json.results.bindings.every { it["e32"].value.startsWith(sherlock.getResourcePrefix()) }
        json.results.bindings.find { it["e32_p"].value == sherlock.resolvePrefix("rdf:type") }["e32_o"].value == sherlock.resolvePrefix("crm:E32_Authority_Document")
        json.results.bindings.find { it["e32_p"].value == sherlock.resolvePrefix("dcterms:creator") }["e32_o"].value == sherlock.getResourcePrefix() + "4b15a57d-8cae-43c5-8096-187b58d29327"
        dateService.isValidISODateTime(json.results.bindings.find { it["e32_p"].value == sherlock.resolvePrefix("dcterms:created") }["e32_o"].value)
    }
}
