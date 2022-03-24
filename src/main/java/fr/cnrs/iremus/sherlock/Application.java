package fr.cnrs.iremus.sherlock;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

@OpenAPIDefinition(
        info = @Info(
                title = "SHERLOCK API",
                version = "0.0",
                description = "SHERLOCK API",
                license = @License(name = "Apache 2.0", url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(url = "http://iremus.cnrs.fr", name = "Thomas Bottini", email = "thomas.bottini•cnrs.fr")
        )
)
public class Application {
    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
}
