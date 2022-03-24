package fr.cnrs.iremus.sherlock.security;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.ldap.ContextAuthenticationMapper;
import io.micronaut.security.ldap.DefaultContextAuthenticationMapper;
import jakarta.inject.Singleton;

import java.util.Set;

@Singleton
@Replaces(DefaultContextAuthenticationMapper.class)
public class MyContextAuthenticationMapper implements ContextAuthenticationMapper {

    @Override
    public AuthenticationResponse map(ConvertibleValues<Object> attributes, String username, Set<String> groups) {
        return AuthenticationResponse.success(username, attributes.asMap());
    }
}