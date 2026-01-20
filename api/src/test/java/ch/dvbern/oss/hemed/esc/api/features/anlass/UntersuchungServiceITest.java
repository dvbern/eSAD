package ch.dvbern.oss.hemed.esc.api.features.anlass;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheitService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class UntersuchungServiceITest extends AbstractITest {

    private final UntersuchungService untersuchungService;
    private final EinladungService einladungService;
    private final FixtureService fixtureService;
    private final UntersuchungImpfstatusKrankheitService untersuchungImpfstatusKrankheitService;

    @Test
    @TestAsAdminBS
    void getOrCreateUntersuchung_non_existing_should_be_created() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        Assertions.assertThat(einladung.getUntersuchung()).isNull();
        // ACT
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladung.getId());
        // ASSERT
        Assertions.assertThat(untersuchung).isNotNull();
        Einladung einladungWithUntersuchung = einladungService.getById(einladung.getId());
        Assertions.assertThat(einladungWithUntersuchung.getUntersuchung()).isNotNull();
    }

    @Test
    @TestAsAdminBS
    void getOrCreateUntersuchung_non_existing_should_create_impfungen() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        Assertions.assertThat(einladung.getUntersuchung()).isNull();
        // ACT
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladung.getId());
        // ASSERT
        List<UntersuchungImpfstatusKrankheit> krannkheiten = untersuchungImpfstatusKrankheitService.findByUntersuchung(untersuchung.getId());
        Assertions.assertThat(untersuchung.getUntersuchungImpfungen()).isNotNull();
        Assertions.assertThat(krannkheiten).hasSize(Krankheit.values().length);
    }

    @Test
    @TestAsAdminBS
    void getOrCreateUntersuchung_existing_should_be_returned() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        Untersuchung existingUntersuchung = untersuchungService.getOrCreateUntersuchung(einladung.getId());
        // ACT
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladung.getId());
        // ASSERT
        Assertions.assertThat(untersuchung).isNotNull();
        Assertions.assertThat(untersuchung.getId()).isEqualTo(existingUntersuchung.getId());
    }
}
