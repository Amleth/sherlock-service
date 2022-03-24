package fr.cnrs.iremus.sherlock.pojo.resource;

import javax.validation.Constraint;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
public @interface LinkedResourcesValidator {
    String message() default "Wrong body. Should be <s> and <p>, or <p> and <o>";
}