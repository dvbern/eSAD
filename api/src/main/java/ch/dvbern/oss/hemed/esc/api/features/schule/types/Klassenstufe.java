package ch.dvbern.oss.hemed.esc.api.features.schule.types;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.klasseexternal.EscadaKlasseStufe;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@RequiredArgsConstructor
public enum Klassenstufe {

    KINDERGARTEN_1(EscadaKlasseStufe.KINDERGARTEN,1),
    KINDERGARTEN_2(EscadaKlasseStufe.KINDERGARTEN,2),
    PRIMAR_KLASSE_1(EscadaKlasseStufe.PRIMARSCHULE,1),
    PRIMAR_KLASSE_2(EscadaKlasseStufe.PRIMARSCHULE,2),
    PRIMAR_KLASSE_3(EscadaKlasseStufe.PRIMARSCHULE,3),
    PRIMAR_KLASSE_4(EscadaKlasseStufe.PRIMARSCHULE,4),
    PRIMAR_KLASSE_5(EscadaKlasseStufe.PRIMARSCHULE,5),
    PRIMAR_KLASSE_6(EscadaKlasseStufe.PRIMARSCHULE,6),
    SEK_I_KLASSE_1(EscadaKlasseStufe.SEKUNDARSTUFE,1),
    SEK_I_KLASSE_2(EscadaKlasseStufe.SEKUNDARSTUFE,2),
    SEK_I_KLASSE_3(EscadaKlasseStufe.SEKUNDARSTUFE,3),
    SEK_II_KLASSE_1(EscadaKlasseStufe.SEKUNDARSTUFE_II,1),
    SEK_II_KLASSE_2(EscadaKlasseStufe.SEKUNDARSTUFE_II,2),
    SEK_II_KLASSE_3(EscadaKlasseStufe.SEKUNDARSTUFE_II,3),
    SEK_II_KLASSE_4(EscadaKlasseStufe.SEKUNDARSTUFE_II, 4);

    private final EscadaKlasseStufe matchingEscadaKlassenStufe;
    private final int programmjahr;

    public static Klassenstufe fromEscadaImport(EscadaKlasseStufe escadaKlasseStufe, int programmjahr) {
        for (Klassenstufe stufe : Klassenstufe.values()) {
            if (stufe.matchingEscadaKlassenStufe == escadaKlasseStufe && stufe.programmjahr == programmjahr) {
                return stufe;
            }
        }
        throw new AppFailureException(AppFailureMessage.internalError(
            "Unmapped KlasseStufe from escada klassenstufe '%s', programmjahr %s".formatted(
                escadaKlasseStufe,
                programmjahr))
        );
    }

    @NonNull
    public EscadaKlasseStufe getEscadaKlasseStufe() {
        return matchingEscadaKlassenStufe;
    }
}
