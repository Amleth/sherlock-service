package fr.cnrs.iremus.sherlock.external.authentication;

import com.nimbusds.jose.shaded.json.JSONObject;
import fr.cnrs.iremus.sherlock.service.OrcidRefreshToken;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.rxjava2.http.client.RxHttpClient;
import io.reactivex.Flowable;
import io.reactivex.Single;
import jakarta.inject.Singleton;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

@Singleton
public class OrcidApiClient {
    private final RxHttpClient httpClient;

    @Property(name="micronaut.security.oauth2.clients.orcid.client_id")
    private String clientId;

    @Property(name="micronaut.security.oauth2.clients.orcid.client_secret")
    private String clientSecret;

    public OrcidApiClient() throws MalformedURLException {
        httpClient = RxHttpClient.create(new URL("https://orcid.org/"));
    }

    Single<OrcidRefreshToken> refreshToken(String refreshToken) {
        JSONObject obj = new JSONObject();
        obj.put("client_id", clientId);
        obj.put("client_secret", clientSecret);
        obj.put("grant_type", "refresh_token");
        obj.put("refresh_token", refreshToken);
        Flowable<HttpResponse<OrcidRefreshToken>> exchange = httpClient.exchange(HttpRequest.POST("https://orcid.org/oauth/token", obj).contentType(MediaType.APPLICATION_FORM_URLENCODED), OrcidRefreshToken.class);
        Flowable<OrcidRefreshToken> map = exchange.map(response -> Objects.requireNonNull(response.body()));
        return map.singleOrError();
    }
}
