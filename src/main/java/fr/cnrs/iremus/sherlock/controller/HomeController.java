package fr.cnrs.iremus.sherlock.controller;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/")
public class HomeController {

    @Produces(MediaType.TEXT_PLAIN)
    @Get
    public String index(@Nullable Authentication authentication) {
        return (String) authentication.getAttributes().get("uuid");
    }
}