package ch.dvbern.oss.hemed.esc.api.shared.archunit;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

import static ch.dvbern.oss.hemed.esc.api.shared.archunit.predicates.AnyOf.anyOf;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@DisplayNameGeneration(ReplaceUnderscores.class)
class UnifiedNullableAnnotationsTest {

    private static final DescribedPredicate<JavaClass> LOMBOK_ANNOTATION = anyOf(NonNull.class)
        .as("Lombok Nullability Annotation");

    private static final DescribedPredicate<JavaClass> JETBRAINS_ANNOTATION = anyOf(Nullable.class, NotNull.class)
        .as("Jetbrains Annotation");

    @Test
    void nullability_checkerframework_depend() {
        var rule = noClasses()
            .should()
            .dependOnClassesThat(are(LOMBOK_ANNOTATION))
            .orShould()
            .dependOnClassesThat(are(JETBRAINS_ANNOTATION))
            .because("we should use only Checkerframework like "
                + org.checkerframework.checker.nullness.qual.Nullable.class
                + " or "
                + org.checkerframework.checker.nullness.qual.NonNull.class);

        rule.check(ArchConst.APP_CLASSES);
    }
}
