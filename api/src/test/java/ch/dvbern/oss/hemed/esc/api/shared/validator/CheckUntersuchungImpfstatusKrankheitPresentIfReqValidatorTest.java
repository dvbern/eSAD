package ch.dvbern.oss.hemed.esc.api.shared.validator;

import java.time.LocalDate;

import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfstatusKrankheitBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungenBuilder;
import org.assertj.core.api.Assertions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CheckUntersuchungImpfstatusKrankheitPresentIfReqValidatorTest {

    @ParameterizedTest
    @CsvSource({
        "REQUIRED	    ,	    ,2000-05-01 ,false   ,false ,true", // Datum alleine ist erlaubt!
        "REQUIRED	    ,3	    ,           ,false   ,false ,false",
        "REQUIRED	    ,3	    ,2000-05-01 ,false   ,false ,true",
        "REQUIRED	    ,	    ,2000-05-01 ,true    ,false ,true",
        "REQUIRED	    ,3	    ,           ,true    ,false ,true",
        "REQUIRED	    ,	    ,2000-05-01 ,false   ,true  ,true",
        "REQUIRED	    ,3	    ,           ,false   ,true  ,true",

        "OPTIONAL	    ,	    ,2000-05-01 ,false   ,false ,true",
        "OPTIONAL	    ,3	    ,           ,false   ,false ,true",
        "OPTIONAL	    ,3	    ,2000-05-01 ,false   ,false ,true",
        "OPTIONAL	    ,	    ,2000-05-01 ,true    ,false ,true",
        "OPTIONAL	    ,3	    ,           ,true    ,false ,true",
        "OPTIONAL	    ,	    ,2000-05-01 ,false   ,true  ,true",
        "OPTIONAL	    ,3	    ,           ,false   ,true  ,true",

        "NOT_VISIBLE	    ,	    ,2000-05-01 ,false   ,false ,true",
        "NOT_VISIBLE	    ,3	    ,           ,false   ,false ,true",
        "NOT_VISIBLE	    ,3	    ,2000-05-01 ,false   ,false ,true",
        "NOT_VISIBLE	    ,	    ,2000-05-01 ,true    ,false ,true",
        "NOT_VISIBLE	    ,3	    ,           ,true    ,false ,true",
        "NOT_VISIBLE	    ,	    ,2000-05-01 ,false   ,true  ,true",
        "NOT_VISIBLE	    ,3	    ,           ,false   ,true  ,true",
    })
    void validateGroesse(
        @NonNull RequirementSetting requirementSetting,
        @Nullable Integer anzahlImpfungen,
        @Nullable LocalDate datumLetzteImpfung,
        boolean impfausweisUnvollstaendig,
        boolean impfausweisNichtVorhanden,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungImpfungen impfungen = UntersuchungImpfungenBuilder.create(
            builder -> builder
                .withImpfausweisUnvollstaendig(impfausweisUnvollstaendig)
                .withImpfausweisNichtVorhanden(impfausweisNichtVorhanden));
        UntersuchungImpfstatusKrankheit entity = UntersuchungImpfstatusKrankheitBuilder.create(
            builder -> builder
                .forUntersuchungImpfungen(impfungen)
                .withAnzahlImpfungen(anzahlImpfungen)
                .withDatumLetzteImpfung(datumLetzteImpfung));
        impfungen.getUntersuchung().getAnlass().getAnlassConfig().setImpfungenUebersicht(requirementSetting);
        // ACT
        CheckUntersuchungImpfstatusKrankheitPresentIfReqValidator validator = new CheckUntersuchungImpfstatusKrankheitPresentIfReqValidator();
        var result = validator.isEntityValid(entity);
        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }
}
