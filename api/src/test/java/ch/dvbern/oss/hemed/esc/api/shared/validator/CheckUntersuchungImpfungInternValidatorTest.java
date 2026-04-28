package ch.dvbern.oss.hemed.esc.api.shared.validator;

import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungInternBuilder;
import java.time.LocalDate;

import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfStatus;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.ImpfstoffCharge;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.ImpfstoffChargeBuilder;
import org.assertj.core.api.Assertions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CheckUntersuchungImpfungInternValidatorTest {

    @ParameterizedTest
    @CsvSource({
        "GEPLANT    	            ,	        ,false   ,false  ,  2025-03-03 ,true",
        "GEPLANT    	            ,2024-01-01 ,false   ,false  ,  2025-03-03 ,true",
        "NICHT_DURCHGEFUEHRT    	,	        ,false   ,false  ,  2025-03-03 ,true",
        "NICHT_DURCHGEFUEHRT    	,2024-01-01 ,false   ,false  ,  2025-03-03 ,true",
        "DURCHGEFUEHRT	            ,       	,false   ,false  ,  2025-03-03 ,false",
        "DURCHGEFUEHRT	            ,2024-01-01	,false   ,false  ,  2025-03-03 ,false",
        "DURCHGEFUEHRT	            ,       	,true    ,false  ,  2025-03-03 ,false",
        "DURCHGEFUEHRT	            ,2024-01-01	,true    ,true   ,  2025-03-03 ,true",
    })
    void validate(
        @NonNull ImpfStatus impfStatus,
        @Nullable LocalDate datumImpfung,
        boolean hasCharge,
        boolean hasUserDurchgefuehrt,
        @NonNull LocalDate now,
        boolean expectedValid
    ) {
        ImpfstoffCharge charge = hasCharge ? ImpfstoffChargeBuilder.defaultImpfstoffCharge() : null;
        Benutzer userDurchgefuehrt = hasUserDurchgefuehrt ? BenutzerBuilder.defaultBenutzer() : null;

        UntersuchungImpfungIntern impfung = UntersuchungImpfungInternBuilder.defaultUntersuchungImpfungIntern(now);
        impfung.setImpfStatus(impfStatus);
        impfung.setDatumImpfung(datumImpfung);
        impfung.setCharge(charge);
        impfung.setUserDurchgefuehrt(userDurchgefuehrt);
        impfung.getUntersuchungImpfungen().getUntersuchung().getAnlass().getAnlassConfig()
            .setImpfungenListe(RequirementSetting.REQUIRED);

        CheckUntersuchungImpfungInternValidator
            validatorUnderTest = new CheckUntersuchungImpfungInternValidator();
        var result = validatorUnderTest.isEntityValid(impfung);
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }
}
