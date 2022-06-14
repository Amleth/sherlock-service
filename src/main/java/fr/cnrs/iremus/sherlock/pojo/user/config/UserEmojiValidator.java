package fr.cnrs.iremus.sherlock.pojo.user.config;

import javax.validation.Constraint;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Constraint(validatedBy = {})
public @interface UserEmojiValidator {
    String message() default "body.emoji does not match the specific pattern";
}

