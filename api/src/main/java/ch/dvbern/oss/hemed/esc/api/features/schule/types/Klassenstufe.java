package ch.dvbern.oss.hemed.esc.api.features.schule.types;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.klasseexternal.EscadaKlasseStufe;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.klasseexternal.PupilKlasseStufe;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;

@RequiredArgsConstructor
public enum Klassenstufe {

    KINDERGARTEN_1(EscadaKlasseStufe.KINDERGARTEN, PupilKlasseStufe.KINDERGARTEN, 1),
    KINDERGARTEN_2(EscadaKlasseStufe.KINDERGARTEN, PupilKlasseStufe.KINDERGARTEN,2),
    PRIMAR_KLASSE_1(EscadaKlasseStufe.PRIMARSCHULE, PupilKlasseStufe.PRIMARSCHULE, 1),
    PRIMAR_KLASSE_2(EscadaKlasseStufe.PRIMARSCHULE, PupilKlasseStufe.PRIMARSCHULE ,2),
    PRIMAR_KLASSE_3(EscadaKlasseStufe.PRIMARSCHULE, PupilKlasseStufe.PRIMARSCHULE ,3),
    PRIMAR_KLASSE_4(EscadaKlasseStufe.PRIMARSCHULE, PupilKlasseStufe.PRIMARSCHULE ,4),
    PRIMAR_KLASSE_5(EscadaKlasseStufe.PRIMARSCHULE, PupilKlasseStufe.PRIMARSCHULE ,5),
    PRIMAR_KLASSE_6(EscadaKlasseStufe.PRIMARSCHULE, PupilKlasseStufe.PRIMARSCHULE ,6),
    SEK_I_KLASSE_1(EscadaKlasseStufe.SEKUNDARSTUFE, PupilKlasseStufe.SEKUNDARSTUFE, 1),
    SEK_I_KLASSE_2(EscadaKlasseStufe.SEKUNDARSTUFE, PupilKlasseStufe.SEKUNDARSTUFE,2),
    SEK_I_KLASSE_3(EscadaKlasseStufe.SEKUNDARSTUFE, PupilKlasseStufe.SEKUNDARSTUFE,3),
    SEK_II_KLASSE_1(EscadaKlasseStufe.SEKUNDARSTUFE_II, PupilKlasseStufe.SEKUNDARSTUFE_II,1),
    SEK_II_KLASSE_2(EscadaKlasseStufe.SEKUNDARSTUFE_II, PupilKlasseStufe.SEKUNDARSTUFE_II,2),
    SEK_II_KLASSE_3(EscadaKlasseStufe.SEKUNDARSTUFE_II, PupilKlasseStufe.SEKUNDARSTUFE_II,3),
    SEK_II_KLASSE_4(EscadaKlasseStufe.SEKUNDARSTUFE_II, PupilKlasseStufe.SEKUNDARSTUFE_II, 4),
    ANDERE(null, null, 0);

    private final EscadaKlasseStufe matchingEscadaKlassenStufe;
    private final PupilKlasseStufe matchingPupilKlassenStufe;
    private final int programmjahr;

    public static Klassenstufe fromEscadaImport(EscadaKlasseStufe escadaKlasseStufe, int programmjahr) {
        return fromExternalValue(Klassenstufe::getEscadaKlasseStufe, escadaKlasseStufe, programmjahr, "escada");
    }

    public static Klassenstufe fromPupilImport(PupilKlasseStufe pupilKlasseStufe, int programmjahr) {
        return fromExternalValue(Klassenstufe::getPupilKlasseStufe, pupilKlasseStufe, programmjahr, "pupil");
    }

    private static <E extends Enum<E>> Klassenstufe fromExternalValue(Function<Klassenstufe, E> mapper, E externalValue, int programmjahr, String externalSystemName) {
        return Arrays.stream(Klassenstufe.values())
            .filter(
            klassenStuffe ->
                klassenStuffe.programmjahr == programmjahr && mapper.apply(klassenStuffe) == externalValue
            ).findFirst()
            .orElseThrow(() -> new AppFailureException(AppFailureMessage.internalError(
            "Unmapped KlasseStufe from %s klassenstufe '%s', programmjahr %s".formatted(
                externalSystemName,
                externalValue,
                programmjahr))
        ));
    }


    @NonNull
    public EscadaKlasseStufe getEscadaKlasseStufe() {
        return matchingEscadaKlassenStufe;
    }

    @NonNull
    public PupilKlasseStufe getPupilKlasseStufe() {
        return matchingPupilKlassenStufe;
    }
}
