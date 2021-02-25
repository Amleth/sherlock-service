package fr.cnrs.iremus.sherlock

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.security.authentication.UsernamePasswordCredentials
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken
import org.apache.jena.atlas.web.HttpException
import org.apache.jena.rdfconnection.RDFConnectionFuseki
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder

import javax.inject.Inject

class Common {

    @Inject
    Sherlock sherlock;

    String getAccessToken(client) {
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials('sherlock', 'password')
        HttpRequest request = HttpRequest.POST('/login', creds)
        HttpResponse<BearerAccessRefreshToken> rsp = client.toBlocking().exchange(request, BearerAccessRefreshToken)
        BearerAccessRefreshToken bearerAccessRefreshToken = rsp.body()
        String accessToken = bearerAccessRefreshToken.accessToken
        return accessToken
    }

    HttpRequest makePostRequestWithAuthorization(client, String uri, requestBody) {
        HttpRequest requestWithAuthorization = HttpRequest.POST(uri, requestBody)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .bearerAuth(getAccessToken(client))
        return requestWithAuthorization
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
