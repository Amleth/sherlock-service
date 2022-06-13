package fr.cnrs.iremus.sherlock.controller;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;

@Controller("/api/")
@Secured(SecurityRule.IS_ANONYMOUS)
public class HomeController {

    @Produces(MediaType.TEXT_HTML)
    @Get
    public String index(@Nullable Authentication authentication) {
        return authentication != null
                ? (String) authentication.getAttributes().get("uuid")
                : "<a href='/oauth/login/orcid'> Please connect</a>";
    }
}