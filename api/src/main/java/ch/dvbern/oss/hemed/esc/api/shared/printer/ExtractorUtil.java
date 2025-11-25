package ch.dvbern.oss.hemed.esc.api.shared.printer;

import java.util.Optional;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.durchfuehrung.UntersuchungDurchfuehrung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.kind.UntersuchungKindDaten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.triage.UntersuchungTriage;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBemerkung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.UntersuchungGroesseGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenOtoskopie;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;

@UtilityClass
public class ExtractorUtil {

    @NonNull
    public static Optional<UntersuchungHoerenAudiogramm> getUntersuchungHoerenAudiogrammOptional(
        @NonNull Einladung einladung) {
        if (einladung.getUntersuchung() == null) {
            return Optional.empty();
        }

        UntersuchungHoerenAudiogramm hoerenAudiogramm = einladung.getUntersuchung().getUntersuchungHoerenAudiogramm();
        if (hoerenAudiogramm == null) {
            return Optional.empty();
        }

        return Optional.of(hoerenAudiogramm);
    }

    @NonNull
    public static Optional<UntersuchungHoerenOtoskopie> getUntersuchungHoerenOtoskopieOptional(
        @NonNull Einladung einladung) {
        if (einladung.getUntersuchung() == null) {
            return Optional.empty();
        }

        UntersuchungHoerenOtoskopie hoerenOtoskopie = einladung.getUntersuchung().getUntersuchungHoerenOtoskopie();
        if (hoerenOtoskopie == null) {
            return Optional.empty();
        }

        return Optional.of(hoerenOtoskopie);
    }

    @NonNull
    public static Optional<UntersuchungSehenBeurteilung> getUntersuchungSehenOptional(@NonNull Einladung einladung) {
        if (einladung.getUntersuchung() == null) {
            return Optional.empty();
        }

        UntersuchungSehenBeurteilung
				untersuchungSehenBeurteilung = einladung.getUntersuchung().getUntersuchungSehenBeurteilung();
        if (untersuchungSehenBeurteilung == null) {
            return Optional.empty();
        }

        return Optional.of(untersuchungSehenBeurteilung);
    }

    @NonNull
    public static Optional<UntersuchungDurchfuehrung> getUntersuchungDurchfuehrungOptional(
        @NonNull Einladung einladung
    ) {
        if (einladung.getUntersuchung() == null) {
            return Optional.empty();
        }

        UntersuchungDurchfuehrung untersuchungDurchfuehrung =
            einladung.getUntersuchung().getUntersuchungDurchfuehrung();
        if (untersuchungDurchfuehrung == null) {
            return Optional.empty();
        }
        return Optional.of(untersuchungDurchfuehrung);
    }

    @NonNull
    public static Optional<UntersuchungImpfungen> getUntersuchungImpfungenOptional(@NonNull Einladung einladung) {
        if (einladung.getUntersuchung() == null) {
            return Optional.empty();
        }

        UntersuchungImpfungen untersuchungImpfungen = einladung.getUntersuchung().getUntersuchungImpfungen();
        if (untersuchungImpfungen == null) {
            return Optional.empty();
        }

        return Optional.of(untersuchungImpfungen);
    }

    @NonNull
    public static Optional<UntersuchungKindDaten> getUntersuchungKindDatenOptional(@NonNull Einladung einladung) {
        if (einladung.getUntersuchung() == null) {
            return Optional.empty();
        }

        UntersuchungKindDaten untersuchungKindDaten = einladung.getUntersuchung().getUntersuchungKindDaten();
        if (untersuchungKindDaten == null) {
            return Optional.empty();
        }
        return Optional.of(untersuchungKindDaten);
    }

    @NonNull
    public static Optional<UntersuchungGroesseGewicht> getUntersuchungGroesseGewichtOptional(
        @NonNull Einladung einladung
    ) {
        if (einladung.getUntersuchung() == null) {
            return Optional.empty();
        }

        UntersuchungGroesseGewicht untersuchungGroesseGewicht =
            einladung.getUntersuchung().getUntersuchungGroesseGewicht();
        if (untersuchungGroesseGewicht == null) {
            return Optional.empty();
        }
        return Optional.of(untersuchungGroesseGewicht);
    }

    @NonNull
    public static Optional<UntersuchungTriage> getUntersuchungTriageOptional(
        @NonNull Einladung einladung
    ) {
        if (einladung.getUntersuchung() == null) {
            return Optional.empty();
        }

        UntersuchungTriage untersuchungTriage =
            einladung.getUntersuchung().getUntersuchungTriage();
        if (untersuchungTriage == null) {
            return Optional.empty();
        }
        return Optional.of(untersuchungTriage);
    }

    @NonNull
    public static Optional<UntersuchungMassnahmen> getUntersuchungMassnahmenOptional(
        @NonNull Einladung einladung
    ) {
        if (einladung.getUntersuchung() == null) {
            return Optional.empty();
        }

        UntersuchungMassnahmen untersuchungMassnahmen =
            einladung.getUntersuchung().getUntersuchungMassnahmen();
        if (untersuchungMassnahmen == null) {
            return Optional.empty();
        }
        return Optional.of(untersuchungMassnahmen);
    }

    @NonNull
    public static Optional<UntersuchungImpfungenMassnahmen> getUntersuchungImpfungenMassnahmenOptional(
        @NonNull Einladung einladung
    ) {
        if (einladung.getUntersuchung() == null) {
            return Optional.empty();
        }

        UntersuchungImpfungen untersuchungImpfungen = einladung.getUntersuchung().getUntersuchungImpfungen();
        if (untersuchungImpfungen == null) {
            return Optional.empty();
        }
        UntersuchungImpfungenMassnahmen untersuchungImpfungenMassnahmen =
            untersuchungImpfungen.getUntersuchungImpfungenMassnahmen();
        if (untersuchungImpfungenMassnahmen == null) {
            return Optional.empty();
        }

        return Optional.of(untersuchungImpfungenMassnahmen);
    }

    @NonNull
    public static Optional<UntersuchungArztBeurteilung> getUntersuchungArztOptional(
        @NonNull Einladung einladung
    ) {
        if (einladung.getUntersuchung() == null) {
            return Optional.empty();
        }

        UntersuchungArztBeurteilung untersuchungArzt =
            einladung.getUntersuchung().getUntersuchungArztBeurteilung();
        if (untersuchungArzt == null) {
            return Optional.empty();
        }
        return Optional.of(untersuchungArzt);
    }

    @NonNull
    public static Optional<UntersuchungArztBemerkung> getUntersuchungArztBemerkungOptional(
        @NonNull Einladung einladung
    ) {
        if (einladung.getUntersuchung() == null) {
            return Optional.empty();
        }
        UntersuchungArztBemerkung untersuchungArzt = einladung.getUntersuchung().getUntersuchungArztBemerkung();
        if (untersuchungArzt == null) {
            return Optional.empty();
        }
        return Optional.of(untersuchungArzt);
    }

    @NonNull
    public static Optional<Schule> getKindSchuleDatenOptional(@NonNull Kind kind) {
        Schule schule = kind.getSchule();
        if (schule == null) {
            return Optional.empty();
        }
        return Optional.of(schule);
    }

    @NonNull
    public static Optional<Klasse> getKindKlasseDatenOptional(@NonNull Kind kind) {
        Klasse klasse = kind.getKlasse();
        if (klasse == null) {
            return Optional.empty();
        }
        return Optional.of(klasse);
    }
}
