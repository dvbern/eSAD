package ch.dvbern.dvbstarter.shared.types.id;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class IdOrderingTest extends AbstractITest {

    @Inject
    EntityManager em;

    private static final ID<IdOrderingParent> PARENT_ID =
        ID.parse("99999999-9999-9999-9999-999999999999", IdOrderingParent.class);
    private static final ID<IdOrderingChild> CHILD1_ID =
        ID.parse("11111111-1111-1111-1111-111111111111", IdOrderingChild.class);
    private static final ID<IdOrderingChild> CHILD2_ID =
        ID.parse("22222222-2222-2222-2222-222222222222", IdOrderingChild.class);
    private static final ID<IdOrderingChild> CHILD3_ID =
        ID.parse("33333333-3333-3333-3333-333333333333", IdOrderingChild.class);

    private static final IdOrderingParent PARENT = new IdOrderingParent();
    private static final IdOrderingChild CHILD_1 = new IdOrderingChild();
    private static final IdOrderingChild CHILD_2 = new IdOrderingChild();
    private static final IdOrderingChild CHILD_3 = new IdOrderingChild();

    @BeforeAll
    static void beforeAll() {
        PARENT.setId(PARENT_ID);
        CHILD_1.setId(CHILD1_ID);
        CHILD_2.setId(CHILD2_ID);
        CHILD_3.setId(CHILD3_ID);
    }

    @BeforeEach
    @Transactional
    void beforeEach() {
        var oldParent = em.find(IdOrderingParent.class, PARENT_ID.getUUID());
        if (oldParent != null) {
            em.remove(oldParent);
        }
    }

    void setupEntities() {
        CHILD_1.setParent(PARENT);
        CHILD_2.setParent(PARENT);
        CHILD_3.setParent(PARENT);
        PARENT.getChildren()
            .addAll(List.of(CHILD_1, CHILD_2, CHILD_3));
        em.persist(PARENT);
    }

    @Test
    @Transactional
    @TestAsAdminBS
    void ordering_orders_by_orderIndex() {
        setupEntities();

        CHILD_1.setOrderIndex(1);
        CHILD_2.setOrderIndex(2);
        CHILD_3.setOrderIndex(3);

        em.persist(PARENT);
        em.flush();
        em.clear();

        var persisted = em.find(IdOrderingParent.class, PARENT_ID.getUUID());

        assertThat(persisted)
            .isEqualTo(PARENT);
        assertThat(persisted.getChildren())
            .containsExactly(
                CHILD_1,
                CHILD_2,
                CHILD_3
            );
    }

    @Test
    @Transactional
    @TestAsAdminBS
    void ordering_orders_by_orderIndex_reversed() {
        setupEntities();

        CHILD_1.setOrderIndex(3);
        CHILD_2.setOrderIndex(2);
        CHILD_3.setOrderIndex(1);

        em.persist(PARENT);
        em.flush();
        em.clear();

        var persisted = em.find(IdOrderingParent.class, PARENT_ID.getUUID());

        assertThat(persisted)
            .isEqualTo(PARENT);
        assertThat(persisted.getChildren())
            .containsExactly(
                CHILD_3,
                CHILD_2,
                CHILD_1
            );
    }
}
