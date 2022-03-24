package fr.cnrs.iremus.sherlock.pojo.resource;

import javax.validation.Constraint;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
public @interface E13AsLinkValidator {
    String message() default "Wrong body. Should contain either P140 or P141 with its type but not both";
}