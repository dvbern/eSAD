package ch.dvbern.oss.hemed.esc.api.shared.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(FIELD)
@Retention(RUNTIME)
@Constraint(validatedBy = CheckEsadIdValidator.class)
@Documented
public @interface CheckEsadId {
    String message() default "{esc.validation.esadid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
