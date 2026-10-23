package ch.dvbern.oss.hemed.esc.api.features.kinder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import io.quarkus.arc.Arc;
import io.quarkus.arc.ManagedContext;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@DisplayNameGeneration(ReplaceUnderscores.class)
class NachkontrolleServiceTest extends AbstractITest {

    @Inject
    NachkontrolleService nachkontrolleService;

    @Inject
    FixtureService fixtureService;

    @Inject
    CurrentIdentityAssociation identityAssociation;

    @Inject
    TenantContext tenants;

    @Test
    @TestAsAdminBS
    void requests_needing_a_new_nachkontrolle_at_the_same_time_all_get_the_same_one() throws Exception {
        // a Dossier without a Nachkontrolle, like every Dossier before it is first opened
        ID<Dossier> dossierId = fixtureService.createDossier().getId();

        // Loading the Einladungen of an Anlass creates the Nachkontrolle of every Kind in it, opening a
        // dossier creates that Kind's - two users doing either at the same time used to collide.
        int threads = 4;
        var startTogether = new CountDownLatch(1);
        SecurityIdentity identity = identityAssociation.getIdentity();
        var executor = Executors.newFixedThreadPool(threads);
        try {
            List<Future<ID<Nachkontrolle>>> results = IntStream.range(0, threads)
                .mapToObj(i -> executor.submit(() -> {
                    startTogether.await();
                    return inOwnRequestContext(
                        identity,
                        () -> nachkontrolleService.getOrCreateByDossier(dossierId).getId());
                }))
                .toList();

            startTogether.countDown();

            Set<ID<Nachkontrolle>> nachkontrollen = new HashSet<>();
            for (Future<ID<Nachkontrolle>> result : results) {
                // fails with a violation of uc_nachkontrolledossier_id_col when two of them insert
                nachkontrollen.add(result.get(30, TimeUnit.SECONDS));
            }
            assertThat(nachkontrollen).hasSize(1);
        } finally {
            executor.shutdownNow();
        }
    }

    /**
     * The services are request scoped, so a call from another thread needs a request context of its own,
     * with the Tenant and the identity the test runs as.
     */
    private <T> T inOwnRequestContext(SecurityIdentity identity, Callable<T> call) throws Exception {
        ManagedContext requestContext = Arc.container().requestContext();
        requestContext.activate();
        try {
            tenants.setCurrentTenant(TenantEnum.BS);
            identityAssociation.setIdentity(identity);
            return call.call();
        } finally {
            requestContext.terminate();
        }
    }
}
