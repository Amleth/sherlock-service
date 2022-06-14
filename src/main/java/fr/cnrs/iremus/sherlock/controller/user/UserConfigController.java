package fr.cnrs.iremus.sherlock.controller.user;

import fr.cnrs.iremus.sherlock.common.Sherlock;
import fr.cnrs.iremus.sherlock.pojo.user.config.UserConfigEdit;
import fr.cnrs.iremus.sherlock.service.UserService;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import org.apache.http.HttpException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.lang.sparql_11.ParseException;

import javax.validation.Valid;

@Controller("/api/user/config")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class UserConfigController {
    @Property(name = "jena")
    protected String jena;

    @Inject
    Sherlock sherlock;

    @Inject
    UserService userService;

    @Put
    @Produces(MediaType.APPLICATION_JSON)
    public MutableHttpResponse<String> edit(@Valid @Body UserConfigEdit body, Authentication authentication) throws HttpException, ParseException {
        String authenticatedUserUuid = (String) authentication.getAttributes().get("uuid");

        Model m = ModelFactory.createDefaultModel();
        Resource authenticatedUser = m.createResource(sherlock.makeIri(authenticatedUserUuid));
        if (userService.getUserByUuid( (String) authentication.getAttributes().get("uuid")) == null) return HttpResponse.badRequest("No user found. Please reconnect");
        if (body.getEmoji() != null) userService.editEmoji(authenticatedUser, body.getEmoji());
        if (body.getColor() != null) userService.editHexColor(authenticatedUser, body.getColor());
        return HttpResponse.ok("User updated");
    }
}
