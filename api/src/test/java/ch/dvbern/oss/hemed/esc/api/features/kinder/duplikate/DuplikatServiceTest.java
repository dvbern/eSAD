package ch.dvbern.oss.hemed.esc.api.features.kinder.duplikate;

import java.time.LocalDate;
import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@QuarkusTest
@RequiredArgsConstructor
class DuplikatServiceTest extends AbstractITest {

    private final FixtureService fixtureService;
    private final DuplikatService duplikatService;

    private static Kind existing1 = null;
    private static Kind existing2 = null;
    private static Kind existing3 = null;
    private static Kind existing4 = null;
    private static Kind existing5 = null;

    @SuppressWarnings("NonThreadSafeLazyInitialization")
    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        if (existing1 == null) {
            existing1 = fixtureService.createKind("MaxOnlyUsedInThisTestclass", "Mustermann", LocalDate.of(2000, 1, 1), Geschlecht.MAENNLICH);
        }
        if (existing2 == null) {
            existing2 = fixtureService.createKind("OttoOnlyUsedInThisTestclass LeoOnlyUsedInThisTestclass", "Meier-Haller", LocalDate.of(2000, 2, 1), Geschlecht.MAENNLICH);
        }
        if (existing3 == null) {
            existing3 = fixtureService.createKind("LéonieOnlyUsedInThisTestclass Bärtha", "Fröhlich", LocalDate.of(2000, 3, 1), Geschlecht.WEIBLICH);
        }
        if (existing4 == null) {
            existing4 = fixtureService.createKind("MaxOnlyUsedInThisTestclass", "Mustermann", LocalDate.of(2000, 1, 1), Geschlecht.DIVERS);
        }
        if (existing5 == null) {
            existing5 = fixtureService.createKind("MaxOnlyUsedInThisTestclass", "Mustermann", LocalDate.of(2000, 1, 1), Geschlecht.UNBEKANNT);
        }
    }

    @TestAsAdminBS
    @ParameterizedTest
    @CsvSource({
        "MaxOnlyUsedInThisTestclass        ,Mustermann     ,2000-01-01  ,MAENNLICH  ,3",   // matches MAENNLICH", UNBEKANNT or DIVERS
        "MaaaxOnlyUsedInThisTestclass      ,Mustermann     ,2000-01-01  ,MAENNLICH  ,0 ",
        "MaxOnlyUsedInThisTestclass        ,Musterfrau     ,2000-01-01  ,MAENNLICH  ,0 ",
        "MaxOnlyUsedInThisTestclass        ,Mustermann     ,2000-02-01  ,MAENNLICH  ,0 ",
        "MaxOnlyUsedInThisTestclass        ,Mustermann     ,2000-01-01  ,WEIBLICH   ,2 ",  // matches UNBEKANNT or DIVERS
    })
    void allArgumentsMustMatch(
        @NonNull String vorname,
        @NonNull String name,
        @NonNull LocalDate geburtsdatum,
        @NonNull Geschlecht geschlecht,
        int anzahlMatchesErwartet
    ) {
        // ARRANGE
        Kind kindToCreate = new Kind();
        kindToCreate.setName(name);
        kindToCreate.setVorname(vorname);
        kindToCreate.setGeburtsdatum(geburtsdatum);
        kindToCreate.setGeschlecht(geschlecht);
        // ACT
        List<Kind> kindSearchJsons = duplikatService.searchPotentialKindDuplikate(kindToCreate);
        // ASSERT
        Assertions.assertThat(kindSearchJsons).size().isEqualTo(anzahlMatchesErwartet);
    }

    @TestAsAdminBS
    @ParameterizedTest
    @CsvSource({
    "OttoOnlyUsedInThisTestclass                    , Meier-Haller , 2000-02-01 , MAENNLICH , 1" ,
    "LeoOnlyUsedInThisTestclass                     , Meier-Haller , 2000-02-01 , MAENNLICH , 1" ,
    "LeoOnlyUsedInThisTestclass ArminOnlyUsedInThisTestclass , Meier-Haller , 2000-02-01 , MAENNLICH , 1" ,
    "OttoOnlyUsedInThisTestclass LeoOnlyUsedInThisTestclass  , Meier        , 2000-02-01 , MAENNLICH , 1" ,
    "OttoOnlyUsedInThisTestclass LeoOnlyUsedInThisTestclass  , Haller       , 2000-02-01 , MAENNLICH , 1" ,
    "OttoOnlyUsedInThisTestclass LeoOnlyUsedInThisTestclass  , Haller-Gugus , 2000-02-01 , MAENNLICH , 1" ,
    "OttoOnlyUsedInThisTestclass LeoOnlyUsedInThisTestclass  , Dada-Meier   , 2000-02-01 , MAENNLICH , 1" ,
    })

    void doppelnamen_must_match_partly(
        @NonNull String vorname,
        @NonNull String name,
        @NonNull LocalDate geburtsdatum,
        @NonNull Geschlecht geschlecht,
        int anzahlMatchesErwartet
    ) {
        // ARRANGE
        Kind kindToCreate = new Kind();
        kindToCreate.setName(name);
        kindToCreate.setVorname(vorname);
        kindToCreate.setGeburtsdatum(geburtsdatum);
        kindToCreate.setGeschlecht(geschlecht);
        // ACT
        List<Kind> kindSearchJsons = duplikatService.searchPotentialKindDuplikate(kindToCreate);
        // ASSERT
        Assertions.assertThat(kindSearchJsons).size().isEqualTo(anzahlMatchesErwartet);
    }

    @TestAsAdminBS
    @ParameterizedTest
    @CsvSource({
        "LéonieOnlyUsedInThisTestclass  Bärtha      ,Fröhlich     ,2000-03-01  ,WEIBLICH  ,1",
        "LéonieOnlyUsedInThisTestclass  Baertha     ,Froehlich    ,2000-03-01  ,WEIBLICH  ,1",
        "LeonieOnlyUsedInThisTestclass  Baertha     ,Froehlich    ,2000-03-01  ,WEIBLICH  ,1 ",
        "LeonieOnlyUsedInThisTestclass  Bartha      ,Frohlich     ,2000-03-01  ,WEIBLICH  ,0 ",
    })
    void test_sonderzeichen(
        @NonNull String vorname,
        @NonNull String name,
        @NonNull LocalDate geburtsdatum,
        @NonNull Geschlecht geschlecht,
        int anzahlMatchesErwartet
    ) {
        // ARRANGE
        Kind kindToCreate = new Kind();
        kindToCreate.setName(name);
        kindToCreate.setVorname(vorname);
        kindToCreate.setGeburtsdatum(geburtsdatum);
        kindToCreate.setGeschlecht(geschlecht);
        // ACT
        List<Kind> kindSearchJsons = duplikatService.searchPotentialKindDuplikate(kindToCreate);
        // ASSERT
        Assertions.assertThat(kindSearchJsons).size().isEqualTo(anzahlMatchesErwartet);
    }

    @TestAsAdminBS
    @ParameterizedTest
    @CsvSource({
        "MaxOnlyUsedInThisTestclass        ,Mustermann     ,2000-01-01  ,MAENNLICH  ,3",   // matches MAENNLICH, UNBEKANNT or DIVERS
        "MaxOnlyUsedInThisTestclass        ,Mustermann     ,2000-01-01  ,WEIBLICH   ,2",   // matches UNBEKANNT or DIVERS
        "MaxOnlyUsedInThisTestclass        ,Mustermann     ,2000-01-01  ,UNBEKANNT  ,3",   // matches UNBEKANNT or DIVERS and MAENNLICH
        "MaxOnlyUsedInThisTestclass        ,Mustermann     ,2000-01-01  ,DIVERS     ,3",   // matches UNBEKANNT or DIVERS and MAENNLICH
    })
    void geschlecht_only_counts_if_WEIBLICH_or_MAENNLICH(
        @NonNull String vorname,
        @NonNull String name,
        @NonNull LocalDate geburtsdatum,
        @NonNull Geschlecht geschlecht,
        int anzahlMatchesErwartet
    ) {
        // ARRANGE
        Kind kindToCreate = new Kind(); // with random id
        kindToCreate.setName(name);
        kindToCreate.setVorname(vorname);
        kindToCreate.setGeburtsdatum(geburtsdatum);
        kindToCreate.setGeschlecht(geschlecht);
        // ACT
        List<Kind> kindSearchJsons = duplikatService.searchPotentialKindDuplikate(kindToCreate);
        // ASSERT
        Assertions.assertThat(kindSearchJsons).size().isEqualTo(anzahlMatchesErwartet);
    }
}
