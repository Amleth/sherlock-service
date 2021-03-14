package fr.cnrs.iremus.sherlock;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/sherlock/api/")
public class HomeController {

    @Get
    @Produces(MediaType.TEXT_PLAIN)
    public String index(Authentication authentication) {
        return (String) authentication.getAttributes().get("uuid");
    }
}