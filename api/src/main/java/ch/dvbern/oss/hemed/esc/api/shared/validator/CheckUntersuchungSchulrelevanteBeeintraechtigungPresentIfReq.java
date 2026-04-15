package ch.dvbern.oss.hemed.esc.api.shared.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ TYPE, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = CheckUntersuchungSchulrelevanteBeeintraechtigungPresentIfReqValidator.class)
@Documented
public @interface CheckUntersuchungSchulrelevanteBeeintraechtigungPresentIfReq {

    String message() default "{esc.validation.checkUntersuchungSchulrelevanteBeeintraechtigungPresent}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
