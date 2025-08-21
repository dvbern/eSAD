package ch.dvbern.oss.hemed.esc.api.shared.archunit;

import ch.dvbern.oss.hemed.esc.api.features.escada.EscadaResource;
import ch.dvbern.oss.hemed.esc.api.features.example.developer.DatabaseResource;
import ch.dvbern.oss.hemed.esc.api.features.example.developer.SuperAdminRunnerResource;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated;
import jakarta.enterprise.inject.Stereotype;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

import static ch.dvbern.oss.hemed.esc.api.shared.archunit.ArchConst.APP_CLASSES;
import static ch.dvbern.oss.hemed.esc.api.shared.archunit.LayeringTest.NO_TRANSACTION_SERVICE;
import static ch.dvbern.oss.hemed.esc.api.shared.archunit.LayeringTest.REPOSITORY;
import static ch.dvbern.oss.hemed.esc.api.shared.archunit.LayeringTest.SERVICE;
import static com.tngtech.archunit.base.DescribedPredicate.and;
import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.metaAnnotatedWith;
import static com.tngtech.archunit.lang.conditions.ArchConditions.be;
import static com.tngtech.archunit.lang.conditions.ArchConditions.fullyQualifiedName;
import static com.tngtech.archunit.lang.conditions.ArchConditions.not;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.library.ProxyRules.no_classes_should_directly_call_other_methods_declared_in_the_same_class_that;

@DisplayNameGeneration(ReplaceUnderscores.class)
class TransactionsTest {

    // NOTE: this does currently no cover cases when e.g. interface methods are annotated

    private static final DescribedPredicate<CanBeAnnotated> TRANSACTIONAL = metaAnnotatedWith(Transactional.class);

    @Test
    void transactional_boundary_on_classes() {
        var rule = classes()
            .that(are(not(SERVICE.or(REPOSITORY))))
            .and()
            .areNotAnnotatedWith(Stereotype.class)
            .should()
            .notBeAnnotatedWith(Transactional.class);

        rule.check(APP_CLASSES);
    }

    @Test
    void transactional_boundary_on_methods() {
        var rule = methods()
            .that()
            .haveNameNotMatching("getMyBenutzerData").and().haveNameNotMatching("reloadDuplikate")
            .and()
            .areDeclaredInClassesThat(are(
                and(
                    not(SERVICE.or(REPOSITORY).or(NO_TRANSACTION_SERVICE)
                        .or(fullyQualifiedName(DatabaseResource.class.getName()))
                        .or(fullyQualifiedName(EscadaResource.class.getName()))
                        .or(fullyQualifiedName(SuperAdminRunnerResource.class.getName()))
                    ),
                    not(annotatedWith(Stereotype.class))
                )
            ))
            .should()
            .notBeAnnotatedWith(Transactional.class);

        rule.check(APP_CLASSES);
    }

    @Test
    void transactional_boundary_on_methods_in_no_transaction_service() {
        var rule = methods()
            .that()
            .areDeclaredInClassesThat(
                are(NO_TRANSACTION_SERVICE)
                    .and(not(annotatedWith(Stereotype.class))))
            .should()
            .notBeAnnotatedWith(Transactional.class);

        rule.check(APP_CLASSES);
    }

    @Test
    void transactional_only_public() {
        var rule = methods()
            .that()
            .areNotPublic()
            .should(not(be(TRANSACTIONAL)));

        rule.check(ArchConst.APP_CLASSES);
    }

    @Test
    void transactional_never_called_inside_self() {
        var rule = no_classes_should_directly_call_other_methods_declared_in_the_same_class_that(are(TRANSACTIONAL));

        rule.check(ArchConst.APP_CLASSES);
    }
}
