package fr.cnrs.iremus.sherlock;


import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.*;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Map;

@Singleton
public class AuthenticationProviderUserPassword implements AuthenticationProvider {

    @Override
    public Publisher<AuthenticationResponse> authenticate(@Nullable HttpRequest<?> httpRequest, AuthenticationRequest<?, ?> authenticationRequest) {
        return Flowable.create(emitter -> {
            if (authenticationRequest.getIdentity().equals("sherlock") && authenticationRequest.getSecret().equals("password")) {
                UserDetails ud = new UserDetails((String) authenticationRequest.getIdentity(), new ArrayList<>());
                ud.setAttributes(Map.of("uuid", "4b15a57d-8cae-43c5-8096-187b58d29327"));
                emitter.onNext(ud);
                emitter.onComplete();
            } else if (authenticationRequest.getIdentity().equals("hudson") && authenticationRequest.getSecret().equals("password")) {
                UserDetails ud = new UserDetails((String) authenticationRequest.getIdentity(), new ArrayList<>());
                ud.setAttributes(Map.of("uuid", "2aaa240d-57e2-4bc1-96e7-714126a5bcff"));
                emitter.onNext(ud);
                emitter.onComplete();
            } else {
                emitter.onError(new AuthenticationException(new AuthenticationFailed()));
            }
        }, BackpressureStrategy.ERROR);
    }
}
