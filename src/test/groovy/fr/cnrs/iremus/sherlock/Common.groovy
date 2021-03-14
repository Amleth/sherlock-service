package fr.cnrs.iremus.sherlock

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.security.authentication.UsernamePasswordCredentials
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken
import org.apache.jena.atlas.web.HttpException
import org.apache.jena.rdfconnection.RDFConnectionFuseki
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder

import javax.inject.Inject

class Common {

    @Inject
    @Client("/")
    RxHttpClient client
    @Inject
    Sherlock sherlock;

    @Deprecated
    String getAccessToken(client) {
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials('sherlock', 'password')
        HttpRequest request = HttpRequest.POST('/sherlock/api/login', creds)
        HttpResponse<BearerAccessRefreshToken> rsp = client.toBlocking().exchange(request, BearerAccessRefreshToken)
        BearerAccessRefreshToken bearerAccessRefreshToken = rsp.body()
        String accessToken = bearerAccessRefreshToken.accessToken
        return accessToken
    }

    @Deprecated
    HttpRequest makePostRequestWithAuthorization(client, String uri, requestBody) {
        HttpRequest requestWithAuthorization = HttpRequest.POST(uri, requestBody)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .bearerAuth(getAccessToken(client))
        return requestWithAuthorization
    }

    @Deprecated
    HttpRequest makePutRequestWithAuthorization(client, String uri, requestBody) {
        HttpRequest requestWithAuthorization = HttpRequest.PUT(uri, requestBody)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .bearerAuth(getAccessToken(client))
        return requestWithAuthorization
    }

    String getAccessToken(String username, String password) {
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(username, password)
        HttpRequest request = HttpRequest.POST('/sherlock/api/login', creds)
        HttpResponse<BearerAccessRefreshToken> rsp = client.toBlocking().exchange(request, BearerAccessRefreshToken)
        BearerAccessRefreshToken bearerAccessRefreshToken = rsp.body()
        String accessToken = bearerAccessRefreshToken.accessToken
        return accessToken
    }

    Object parse(String json) {
        return new ObjectMapper().readValue(json, Object.class)
    }

    Object post(String accessToken, String route, Map body) {
        HttpRequest request = HttpRequest.POST(route, body)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .bearerAuth(accessToken)
        String response = client.toBlocking().retrieve(request)
        return parse(response)
    }

    void fuck() {
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination('http://localhost:3030/iremus');
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            conn.delete(sherlock.getGraph().toString());
        }
        catch (HttpException e) {
        }
    }
}
