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
//        json.results.bindings[0].resource.value.startsWith(sherlock.getResourcePrefix())
//        json.results.bindings[0].type.value == sherlock.resolvePrefix("crm:E32_Authority_Document")
//        json.results.bindings[0].creator.value == sherlock.getResourcePrefix() + "4b15a57d-8cae-43c5-8096-187b58d29327"
//        dateService.isValidISODateTime(json.results.bindings[0].created.value)
    }
}
