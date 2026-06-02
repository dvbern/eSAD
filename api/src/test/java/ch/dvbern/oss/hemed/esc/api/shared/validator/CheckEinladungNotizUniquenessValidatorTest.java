package ch.dvbern.oss.hemed.esc.api.shared.validator;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizTyp;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.EinladungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.NotizBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class CheckEinladungNotizUniquenessValidatorTest {

    @ParameterizedTest
    @CsvSource({
        "DEFAULT                ,true",
        "FRAGEBOGEN             ,false",
        "IMPFAUSWEIS            ,false",
        "ANMELDUNG_BESTAETIGUNG ,false",
        "ABMELDUNG              ,false",
        "IMPFEINVERSTAENDNIS    ,false"
    })
    void validateNotizenUniqueness(
        @NonNull NotizTyp notizTyp,
        boolean expectedValid
    ) {
        // ARRANGE
        Notiz notiz1 = NotizBuilder.create(notizBuilder -> notizBuilder.withNotizTyp(notizTyp));
        Notiz notiz2 = NotizBuilder.create(notizBuilder -> notizBuilder.withNotizTyp(notizTyp));

        Einladung einladung = EinladungBuilder.create(builder -> builder.withNotiz(notiz1).withNotiz(notiz2));

        // ACT
        CheckEinladungNotizUniquenessValidator validator = new CheckEinladungNotizUniquenessValidator();
        boolean result = validator.isEntityValid(einladung);

        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }
}

