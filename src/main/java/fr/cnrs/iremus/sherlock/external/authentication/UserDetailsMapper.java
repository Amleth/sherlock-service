package fr.cnrs.iremus.sherlock.external.authentication;

import fr.cnrs.iremus.sherlock.common.Sherlock;
import fr.cnrs.iremus.sherlock.service.OrcidRefreshToken;
import fr.cnrs.iremus.sherlock.service.UserService;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.oauth2.endpoint.authorization.state.State;
import io.micronaut.security.oauth2.endpoint.token.response.OauthAuthenticationMapper;
import io.micronaut.security.oauth2.endpoint.token.response.TokenResponse;
import io.reactivex.Single;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;

import java.util.Map;

@Named("orcid")
@Singleton
public class UserDetailsMapper implements OauthAuthenticationMapper {
    @Inject
    UserService userService;

    @Inject
    Sherlock sherlock;
    private final OrcidApiClient orcidApiClient;


    public UserDetailsMapper(OrcidApiClient orcidApiClient) {
        this.orcidApiClient = orcidApiClient;
    }
    @Override
    public Publisher<AuthenticationResponse> createAuthenticationResponse(TokenResponse tokenResponse, State state) {


        Single<OrcidRefreshToken> orcidRefreshTokenSingle = orcidApiClient.refreshToken(tokenResponse.getRefreshToken());
        try {
            OrcidRefreshToken orcidRefreshToken = orcidRefreshTokenSingle.blockingGet();
            String userUuid = sherlock.getUuidFromSherlockUri(userService.createUserIfNotExists(orcidRefreshToken.getOrcid()));
            return Publishers.just(AuthenticationResponse.success(
                    orcidRefreshToken.getName(),
                    Map.ofEntries(
                            Map.entry("orcid",orcidRefreshToken.getOrcid()),
                            Map.entry("uuid", userUuid)
                    )));
        } catch (Exception exception) {
            return Publishers.just(AuthenticationResponse.failure(exception.getMessage()));
        }
    }
}