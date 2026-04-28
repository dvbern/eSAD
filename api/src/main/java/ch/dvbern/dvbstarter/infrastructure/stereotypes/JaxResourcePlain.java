package ch.dvbern.dvbstarter.infrastructure.stereotypes;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.enterprise.inject.Stereotype;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Prefer @JaxResource if you are Building and endpoint, this stereotype is for plain JAX-RS resources without transactional support.
 */
@Stereotype
@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface JaxResourcePlain {
}
