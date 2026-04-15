package ch.dvbern.oss.hemed.esc.api.shared.printer;

import java.util.Objects;
import java.util.Optional;

import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.UntersuchungBewegungsapparat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.durchfuehrung.UntersuchungDurchfuehrung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.kind.UntersuchungKindDaten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.laeusekontrolle.UntersuchungLaeusekontrolle;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.UntersuchungSchulrelevanteBeeintraechtigung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.UntersuchungSprache;
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
            return Optional.of(UntersuchungKindDaten.ofKind(einladung.getDossier().getKind()));
        }

        UntersuchungKindDaten untersuchungKindDaten = einladung.getUntersuchung().getUntersuchungKindDaten();
        return Optional.of(Objects.requireNonNullElseGet(
            untersuchungKindDaten,
            () -> UntersuchungKindDaten.ofKind(einladung.getDossier().getKind())));
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
    public static Optional<UntersuchungSprache> getUntersuchungSpracheOptional(
        @NonNull Einladung einladung
    ) {
        if (einladung.getUntersuchung() == null) {
            return Optional.empty();
        }
        UntersuchungSprache untersuchungSprache = einladung.getUntersuchung().getUntersuchungSprache();
        if (untersuchungSprache == null) {
            return Optional.empty();
        }
        return Optional.of(untersuchungSprache);
    }

    @NonNull
    public static Optional<UntersuchungBewegungsapparat> getUntersuchungBewegungsapparatOptional(
        @NonNull Einladung einladung
    ) {
        if (einladung.getUntersuchung() == null) {
            return Optional.empty();
        }
        UntersuchungBewegungsapparat untersuchungBewegungsapparat =
            einladung.getUntersuchung().getUntersuchungBewegungsapparat();
        if (untersuchungBewegungsapparat == null) {
            return Optional.empty();
        }
        return Optional.of(untersuchungBewegungsapparat);
    }

    @NonNull
    public static Optional<UntersuchungSchulrelevanteBeeintraechtigung> getUntersuchungSchulrelevanteBeeintraechtigungOptional(
        @NonNull Einladung einladung
    ) {
        if (einladung.getUntersuchung() == null) {
            return Optional.empty();
        }
        UntersuchungSchulrelevanteBeeintraechtigung beeintraechtigung = einladung.getUntersuchung().getUntersuchungSchulrelevanteBeeintraechtigung();
        if (beeintraechtigung == null) {
            return Optional.empty();
        }
        return Optional.of(beeintraechtigung);
    }

    @NonNull
    public static Optional<UntersuchungLaeusekontrolle> getUntersuchungLaeusekontrolleOptional(
        @NonNull Einladung einladung
    ) {
        if (einladung.getUntersuchung() == null) {
            return Optional.empty();
        }

        UntersuchungLaeusekontrolle untersuchungLaeusekontrolle =
            einladung.getUntersuchung().getUntersuchungLaeusekontrolle();
        if (untersuchungLaeusekontrolle == null) {
            return Optional.empty();
        }
        return Optional.of(untersuchungLaeusekontrolle);
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
