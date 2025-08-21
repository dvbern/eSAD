package ch.dvbern.oss.hemed.esc.api.shared.archunit;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.SaveJsonWithOptimisticLocking;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@DisplayNameGeneration(ReplaceUnderscores.class)
class OptimisticLockingArchRulesTest {

    @Test
    void classes_implementing_SaveJsonWithOptimisticLocking_should_be_named_SaveJson() {
        var rule = classes()
            .that()
            .implement(SaveJsonWithOptimisticLocking.class)
            .should()
            .haveSimpleNameEndingWith("SaveJson");

        rule.check(ArchConst.APP_CLASSES);
    }

    @Test
    void classes_implementing_SaveJsonWithOptimisticLocking_should_be_annotated_with_JaxDTO() {
        var rule = classes()
            .that()
            .implement(SaveJsonWithOptimisticLocking.class)
            .should()
            .beAnnotatedWith(JaxDTO.class);

        rule.check(ArchConst.APP_CLASSES);
    }

    @Test
    void classes_implementing_EntityWithOptimisticLocking_should_be_entities() {
        var rule = classes()
            .that()
            .implement(EntityWithOptimisticLocking.class)
            .should()
            .beAnnotatedWith(Entity.class);

        rule.check(ArchConst.APP_CLASSES);
    }
}
