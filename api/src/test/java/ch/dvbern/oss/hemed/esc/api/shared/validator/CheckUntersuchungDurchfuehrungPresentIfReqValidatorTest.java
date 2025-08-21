package ch.dvbern.oss.hemed.esc.api.shared.validator;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungDurchfuehrung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungDurchfuehrungBuilder;
import org.assertj.core.api.Assertions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CheckUntersuchungDurchfuehrungPresentIfReqValidatorTest {

    private final FixedClockProvider clockProvider = FixedClockProvider.now();
    private final AppClock appClock = new AppClock(clockProvider);



    @ParameterizedTest
    @CsvSource({
        "REQUIRED	,false	,true   ,false",
        "REQUIRED	,true	,false  ,false",
        "REQUIRED	,false	,false  ,false",
        "REQUIRED	,true	,true   ,true",
        "OPTIONAL	,false  ,true   ,true",
        "OPTIONAL	,true	,false  ,true",
        "OPTIONAL	,false  ,false  ,true",
        "OPTIONAL	,true	,true   ,true",
        "NOT_VISIBLE,false  ,true   ,true",
        "NOT_VISIBLE,true	,false  ,true",
        "NOT_VISIBLE,false  ,false  ,true",
        "NOT_VISIBLE,true	,true   ,true",
    })
    void validate(
        @NonNull RequirementSetting requirementSetting,
        boolean hasArzt,
        boolean hasMpa,
        boolean expectedValid
    ) {
        Benutzer arzt = hasArzt ? BenutzerBuilder.defaultBenutzer() : null;
        Benutzer mpa = hasMpa ? BenutzerBuilder.defaultBenutzer() : null;
        UntersuchungDurchfuehrung durchfuehrung = UntersuchungDurchfuehrungBuilder.defaultUntersuchungDurchfuehrung(appClock.offsetDateTime().now().toLocalDate());
        durchfuehrung.setArzt(arzt);
        durchfuehrung.setMpa(mpa);

        durchfuehrung.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setDatenUntersuchung(requirementSetting);

        CheckUntersuchungDurchfuehrungPresentIfReqValidator
            validatorUnderTest = new CheckUntersuchungDurchfuehrungPresentIfReqValidator();
        var result = validatorUnderTest.isEntityValid(durchfuehrung);
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }
}
