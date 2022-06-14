package fr.cnrs.iremus.sherlock.pojo.user.config;

import javax.validation.Constraint;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Constraint(validatedBy = {})
public @interface UserConfigValidator {
    String message() default "Please set one of body.emoji or body.color";
}
