package ch.dvbern.oss.hemed.esc.api.testutil.spss;

import java.util.stream.Stream;

import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Blutdruck;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinhuepfen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinstand;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Herzauskultation;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Motorik;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Verhalten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileBmi;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGroesse;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.Dezibel;
import org.junit.jupiter.params.provider.Arguments;

public final class SpssTestUtil {

    private SpssTestUtil() {
    }

    public static Stream<Arguments> dezibelTestCases() {
        return Stream.of(
            Arguments.of(Dezibel.DB10, "10"),
            Arguments.of(Dezibel.DB20, "20"),
            Arguments.of(Dezibel.DB30, "30"),
            Arguments.of(Dezibel.DB40, "40"),
            Arguments.of(Dezibel.DB50, "50"),
            Arguments.of(Dezibel.DB60, "60"),
            Arguments.of(Dezibel.MORE_THAN_DB60, "70")
        );
    }

    public static Stream<Arguments> perzentilBmiTestCases() {
        return Stream.of(
            Arguments.of(PerzentileBmi.NORMAL, "1"),
            Arguments.of(PerzentileBmi.PERZENTILE_TIEF, "2"),
            Arguments.of(PerzentileBmi.PERZENTILE_HOCH, "3"),
            Arguments.of(PerzentileBmi.NICHT_GEPRUEFT, "4")
        );
    }

    public static Stream<Arguments> einbeinhuepfenTestCases() {
        return Stream.of(
            Arguments.of(Einbeinhuepfen.SICHER, "1"),
            Arguments.of(Einbeinhuepfen.UNSICHER_UNGEUEBT, "2"),
            Arguments.of(Einbeinhuepfen.NICHT_MOEGLICH, "3"),
            Arguments.of(Einbeinhuepfen.ANDERES, "4"),
            Arguments.of(Einbeinhuepfen.NICHT_GEPRUEFT, "4")
        );
    }

    public static Stream<Arguments> einbeinstandTestCases() {
        return Stream.of(
            Arguments.of(Einbeinstand.SICHER, "1"),
            Arguments.of(Einbeinstand.UNSICHER_UNGEUEBT, "2"),
            Arguments.of(Einbeinstand.NICHT_MOEGLICH, "3"),
            Arguments.of(Einbeinstand.ANDERES, "4"),
            Arguments.of(Einbeinstand.NICHT_GEPRUEFT, "4")
        );
    }

    public static Stream<Arguments> perzentileGewichtTestCases() {
        return Stream.of(
            Arguments.of(PerzentileGewicht.NORMAL, "1"),
            Arguments.of(PerzentileGewicht.PERZENTILE_TIEF, "5"),
            Arguments.of(PerzentileGewicht.PERZENTILE_HOCH, "6"),
            Arguments.of(PerzentileGewicht.NICHT_GEPRUEFT, "4")
        );
    }

    public static Stream<Arguments> perzentileGroesseTestCases() {
        return Stream.of(
            Arguments.of(PerzentileGroesse.NORMAL, "1"),
            Arguments.of(PerzentileGroesse.PERZENTILE_TIEF, "5"),
            Arguments.of(PerzentileGroesse.PERZENTILE_HOCH, "6"),
            Arguments.of(PerzentileGroesse.NICHT_GEPRUEFT, "4")
        );
    }

    public static Stream<Arguments> blutdruckTestCases() {
        return Stream.of(
            Arguments.of(Blutdruck.NORMAL, "1"),
            Arguments.of(Blutdruck.AUFFAELLIG, "2"),
            Arguments.of(Blutdruck.NICHT_GEPRUEFT, "3")
        );
    }

    public static Stream<Arguments> HerzauskultationTestCases() {
        return Stream.of(
            Arguments.of(Herzauskultation.UNAUFFAELLIG, "1"),
            Arguments.of(Herzauskultation.AKZIDENTELLES_SYSTOLIKUM, "2"),
            Arguments.of(Herzauskultation.ANDERES, "5"),
            Arguments.of(Herzauskultation.NICHT_GEPRUEFT, "6")
        );
    }

    public static Stream<Arguments> MotorikTestCases() {
        return Stream.of(
            Arguments.of(Motorik.NORMAL, "1"),
            Arguments.of(Motorik.AUFFAELLIG, "2"),
            Arguments.of(Motorik.NICHT_GEPRUEFT, "3")
        );
    }

    public static Stream<Arguments> VerhaltenTestCases() {
        return Stream.of(
            Arguments.of(Verhalten.UNAUFFAELLIG, "1"),
            Arguments.of(Verhalten.AUFFAELLIG, "2"),
            Arguments.of(Verhalten.NICHT_GEPRUEFT, "3")
        );
    }

}
