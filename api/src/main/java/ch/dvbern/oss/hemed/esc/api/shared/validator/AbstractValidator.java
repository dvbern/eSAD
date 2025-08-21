package ch.dvbern.oss.hemed.esc.api.shared.validator;

import java.lang.annotation.Annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@NoArgsConstructor
public abstract class AbstractValidator<A extends Annotation, E> implements ConstraintValidator<A, E> {

	protected A annotation = null;

	@Override
	public final void initialize(A constraintAnnotation) {
		this.annotation = constraintAnnotation;
	}

	@Override
	public final boolean isValid(@Nullable E e, @NonNull ConstraintValidatorContext context) {
		if (e == null) {
			return true;
		}
		return isEntityValid(e);
	}

	protected abstract boolean isEntityValid(@NonNull E e);
}
