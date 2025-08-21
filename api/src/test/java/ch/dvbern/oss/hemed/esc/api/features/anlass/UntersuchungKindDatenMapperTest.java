package ch.dvbern.oss.hemed.esc.api.features.anlass;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungKindDatenJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KlasseBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungKindDatenBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class UntersuchungKindDatenMapperTest {

    private final FixedClockProvider clockProvider = FixedClockProvider.now();
    private final AppClock appClock = new AppClock(clockProvider);

    @Test
    void toJson_should_return_kind_data_if_untersuchungdaten_not_yet_set() {
        // ARRANGE
        Untersuchung untersuchung = UntersuchungBuilder.defaultUntersuchung(appClock.offsetDateTime().now().toLocalDate());
        untersuchung.getEinladung().getDossier().getKind().setName("Name auf Dossier");
        // ACT
        UntersuchungKindDatenJson untersuchungKindDatenJson =
            UntersuchungKindDatenMapper.toJson(untersuchung, appClock.offsetDateTime().now().toLocalDate());
        // ASSERT
        Assertions.assertThat(untersuchungKindDatenJson.getName()).isEqualTo("Name auf Dossier");
    }

    @Test
    void toJson_should_return_kind_data_with_klasseninfo_if_set() {
        // ARRANGE
        Klasse klasse = KlasseBuilder.defaultKlasse();
        Untersuchung untersuchung = UntersuchungBuilder.defaultUntersuchung(appClock.offsetDateTime().now().toLocalDate());
        untersuchung.getEinladung().getDossier().getKind().setKlasse(klasse);
        // ACT
        UntersuchungKindDatenJson untersuchungKindDatenJson =
            UntersuchungKindDatenMapper.toJson(untersuchung, appClock.offsetDateTime().now().toLocalDate());
        // ASSERT
        Assertions.assertThat(untersuchungKindDatenJson.getKlasse()).isEqualTo(klasse.getBezeichnung());
        Assertions.assertThat(untersuchungKindDatenJson.getSchulhausnummer())
            .isEqualTo(klasse.getSchule().getSchulhausnummer());
        Assertions.assertThat(untersuchungKindDatenJson.getKlassentyp()).isEqualTo(klasse.getKlasseSchulart());
    }

    @Test
    void toJson_should_return_untersuchungdaten_if_untersuchungdaten_already_set() {
        // ARRANGE
        Untersuchung untersuchung = UntersuchungBuilder.defaultUntersuchung(appClock.offsetDateTime().now().toLocalDate());
        untersuchung.getEinladung().getDossier().getKind().setName("Name auf Dossier");
        untersuchung.setUntersuchungKindDaten(UntersuchungKindDatenBuilder.defaultUntersuchungDaten());
        Assertions.assertThat(untersuchung.getUntersuchungKindDaten()).isNotNull();
        untersuchung.getUntersuchungKindDaten().setName("Name auf Untersuchung");
        // ACT
        UntersuchungKindDatenJson untersuchungKindDatenJson =
            UntersuchungKindDatenMapper.toJson(untersuchung, appClock.offsetDateTime().now().toLocalDate());
        // ASSERT
        Assertions.assertThat(untersuchungKindDatenJson.getName()).isEqualTo("Name auf Untersuchung");
    }
}
