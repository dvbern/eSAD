package ch.dvbern.oss.hemed.esc.api.shared.archunit;

import java.lang.annotation.Annotation;
import java.util.function.Function;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMember;
import com.tngtech.archunit.core.domain.properties.HasAnnotations;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.lang.conditions.ArchConditions.have;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

@DisplayNameGeneration(ReplaceUnderscores.class)
class MetricsTest {

    private static final DescribedPredicate<JavaClass> REQUIRE_METRICS = annotatedWith(JaxResource.class).forSubtype();

    // FIXME: starter: decide if this is relevant to the project
    @Test
    @Disabled("This test is disabled because this is project specific")
    void metrics_annotations_are_present_on_required_methods() {
        var rule = methods()
            .that()
            .areDeclaredInClassesThat(REQUIRE_METRICS)
            .should()
            .beAnnotatedWith(Counted.class)
            .andShould()
            .beAnnotatedWith(Timed.class);

        rule.check(ArchConst.APP_CLASSES);
    }

    // FIXME: starter: decide if this is relevant to the project
    @Test
    @Disabled("This test is disabled because this is project specific")
    void metrics_annotations_are_present_on_classes() {
        var rule = classes()
            .that(REQUIRE_METRICS)
            .should()
            .beAnnotatedWith(Counted.class)
            .andShould()
            .beAnnotatedWith(Timed.class);

        rule.check(ArchConst.APP_CLASSES);
    }

    @Test
    void counted_annotations_on_classes_follow_conventions() {
        var rule = classes()
            .that()
            .areAnnotatedWith(Counted.class)
            .should(have(annotationWithPropertyEndingWithSuffix(Counted.class, Counted::value, "Count")));

        rule.allowEmptyShould(true).check(ArchConst.APP_CLASSES);
    }

    @Test
    void counted_annotations_on_methods_follow_conventions() {
        var rule = methods()
            .that()
            .areAnnotatedWith(Counted.class)
            .should(have(annotationWithPropertyEndingWithSuffix(Counted.class, Counted::value, "Count")));

        rule.check(ArchConst.APP_CLASSES);
    }

    @Test
    void timer_annotations_on_classes_follow_convention() {
        var rule = classes()
            .that()
            .areAnnotatedWith(Timed.class)
            .should(have(annotationWithPropertyEndingWithSuffix(Timed.class, Timed::value, "MS")));

        rule.check(ArchConst.APP_CLASSES);
    }

    @Test
    void timer_annotations_on_methods_follow_convention() {
        var rule = methods()
            .that()
            .areAnnotatedWith(Timed.class)
            .should(have(annotationWithPropertyEndingWithSuffix(Timed.class, Timed::value, "MS")));

        rule.check(ArchConst.APP_CLASSES);
    }

    /**
     * Ensures that the annotation "annotationClass" is present and its property "propertyName" ends with the given
     * suffix.
     */
    private <T extends Annotation> DescribedPredicate<HasAnnotations<?>> annotationWithPropertyEndingWithSuffix(
        Class<T> annotationClazz,
        Function<T, String> propertyName,
        String suffix
    ) {
        return DescribedPredicate.describe(
            "annotation '%s' with the property having suffix '%s'".formatted(annotationClazz.getSimpleName(), suffix),
            annotated -> {
                var annotation = annotated.getAnnotationOfType(annotationClazz);
                var name = propertyName.apply(annotation);
                // Everything that HasAnnotations also HasName (i.e.: JavaClass, JavaMethod, ...)
                var annotatedName = switch (annotated) {
                    case JavaClass javaClass -> javaClass.getSimpleName();
                    case JavaMember javaMember -> javaMember.getName();
                    default -> throw new IllegalArgumentException("Unsupported type: " + annotated.getClass());
                };
                var expectedName = annotatedName + suffix;

                return name.equals(expectedName);
            }
        );
    }
}
