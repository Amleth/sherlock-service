package fr.cnrs.iremus.sherlock.common;

import fr.cnrs.iremus.sherlock.pojo.e13.E13AsLink;
import fr.cnrs.iremus.sherlock.pojo.resource.E13AsLinkValidator;
import fr.cnrs.iremus.sherlock.pojo.resource.LinkedResourcesValidator;
import fr.cnrs.iremus.sherlock.pojo.triple.TripleCreate;
import fr.cnrs.iremus.sherlock.pojo.triple.TripleValidator;
import fr.cnrs.iremus.sherlock.pojo.user.config.UserColorValidator;
import fr.cnrs.iremus.sherlock.pojo.user.config.UserConfigEdit;
import fr.cnrs.iremus.sherlock.pojo.user.config.UserConfigValidator;
import fr.cnrs.iremus.sherlock.pojo.user.config.UserEmojiValidator;
import fr.cnrs.iremus.sherlock.service.ValidatorService;
import io.micronaut.context.annotation.Factory;
import io.micronaut.validation.validator.constraints.ConstraintValidator;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;

@Factory
public class ValidatorFactory {
    @Inject
    ValidatorService validatorService;

    @Singleton
    ConstraintValidator<TripleValidator, TripleCreate> tripleValidator() {
        return (value, annotationMetadata, context) -> (
                value != null
                        &&
                        (
                                value.getObject_type() == null
                                        ||
                                        (
                                                !value.getObject_type().equals(ResourceType.URI)
                                                        ||
                                                        (
                                                                value.getO_datatype() == null
                                                                        &&
                                                                        value.getO_lg() == null
                                                        )
                                        )
                        )
        );
    }

    @Singleton
    ConstraintValidator<LinkedResourcesValidator, List<Triple>> LinkedResourcesValidator() {
        return (value, annotationMetadata, context) -> {
            return value != null
                    &&
                    value.stream().allMatch(this::isTripleValid);
        };
    }

    @Singleton
    ConstraintValidator<E13AsLinkValidator, List<E13AsLink>> E13AsLinkValidator() {
        return (value, annotationMetadata, context) -> {
            return value != null
                    &&
                    value.stream().allMatch(this::E13AsLinkValid);
        };
    }

    @Singleton
    ConstraintValidator<UserConfigValidator, UserConfigEdit> userConfigValidator() {
        return (value, annotationMetadata, context) -> {
            assert value != null;
            return value.getEmoji() != null || value.getColor() != null;
        };
    }

    @Singleton
    ConstraintValidator<UserEmojiValidator, String> userEmojiValidator() {
        return (value, annotationMetadata, context) -> value == null || validatorService.isUnicodePattern(value);
    }

    @Singleton
    ConstraintValidator<UserColorValidator, String> userColorValidator() {
        return (value, annotationMetadata, context) -> value == null || validatorService.isHexColorCode(value);
    }

    private boolean E13AsLinkValid(E13AsLink e13AsLink) {
        return (
                e13AsLink.getP140() == null
                        &&
                        e13AsLink.getP141() != null
                        &&
                        e13AsLink.getP141_type() != null
        )
                ||
                (
                        e13AsLink.getP140() != null
                                &&
                                e13AsLink.getP141() == null
                );
    }

    private boolean isTripleValid(Triple triple) {
        return triple.getP() != null
                && (
                (
                        triple.getS() == null
                                &&
                                triple.getO() != null
                )
                        ||
                        (
                                triple.getS() != null
                                        &&
                                        triple.getO() == null
                        )
        );
    }
}