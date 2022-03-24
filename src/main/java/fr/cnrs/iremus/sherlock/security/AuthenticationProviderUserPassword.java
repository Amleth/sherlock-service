package fr.cnrs.iremus.sherlock.security;

import fr.cnrs.iremus.sherlock.security.MyContextAuthenticationMapper;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.AuthenticationProvider;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.Map;

@Singleton
public class AuthenticationProviderUserPassword implements AuthenticationProvider {

    @Override
    public Publisher<AuthenticationResponse> authenticate(@Nullable HttpRequest<?> httpRequest, AuthenticationRequest<?, ?> authenticationRequest) {
        return Flux.create(emitter -> {
            if (authenticationRequest.getIdentity().equals("sherlock") && authenticationRequest.getSecret().equals("kcolrehs")) {
                Map<String, Object> attributes = Map.of("uuid", "4b15a57d-8cae-43c5-8096-187b58d29327");
                MyContextAuthenticationMapper ud = new MyContextAuthenticationMapper();
                emitter.next(ud.map(ConvertibleValues.of(attributes), (String) authenticationRequest.getIdentity(), null));
                emitter.complete();
            } else {
                emitter.error(AuthenticationResponse.exception());
            }
        }, FluxSink.OverflowStrategy.ERROR);
    }
}
