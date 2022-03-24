package fr.cnrs.iremus.sherlock.pojo.triple;

import javax.validation.Constraint;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Constraint(validatedBy = {})
public @interface TripleValidator {
    String message() default "You cannot define datatype or language if your object is uri";
}