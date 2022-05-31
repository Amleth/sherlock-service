package fr.cnrs.iremus.sherlock.security

import com.nimbusds.jwt.JWTParser
import com.nimbusds.jwt.SignedJWT
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.rxjava2.http.client.RxHttpClient
import io.micronaut.security.authentication.UsernamePasswordCredentials
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Ignore
import spock.lang.Specification

@MicronautTest
class JwtAuthenticationSpec extends Specification {
    @Inject
    @Client("/")
    RxHttpClient client

    @Ignore("Default user is now authenticated in test cases (see groovy.fr.cnrs.iremus.sherlock.SecurityBypassFilter)")
    void 'Accessing a secured URL without authenticating returns unauthorized'() {
        when:
        client.toBlocking().exchange(HttpRequest.GET('/sherlock/api/',))

        then:
        HttpClientResponseException e = thrown()
        e.status == HttpStatus.UNAUTHORIZED
    }

    @Ignore("Authentication is now using OAuth2 protocol and not basic auth")
    void "upon successful authentication, a Json Web token is issued to the user"() {
        when: 'Login endpoint is called with valid credentials'
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials("sherlock", "kcolrehs")
        HttpRequest request = HttpRequest.POST('/sherlock/api/login', creds)
        HttpResponse<BearerAccessRefreshToken> rsp = client.toBlocking().exchange(request, BearerAccessRefreshToken)

        then: 'the endpoint can be accessed'
        rsp.status == HttpStatus.OK

        when:
        BearerAccessRefreshToken bearerAccessRefreshToken = rsp.body()

        then:
        bearerAccessRefreshToken.username == 'sherlock'
        bearerAccessRefreshToken.accessToken

        and: 'the access token is a signed JWT'
        JWTParser.parse(bearerAccessRefreshToken.accessToken) instanceof SignedJWT

        when: 'passing the access token as in the Authorization HTTP Header with the prefix Bearer allows the user to access a secured endpoint'
        String accessToken = bearerAccessRefreshToken.accessToken
        HttpRequest requestWithAuthorization = HttpRequest.GET('/sherlock/api/')
                .accept(MediaType.TEXT_PLAIN)
                .bearerAuth(accessToken)
        HttpResponse<String> response = client.toBlocking().exchange(requestWithAuthorization, String)

        then:
        response.status == HttpStatus.OK
        response.body() == '4b15a57d-8cae-43c5-8096-187b58d29327'
    }
}
