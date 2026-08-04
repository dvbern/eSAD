package ch.dvbern.oss.hemed.esc.api.shared.printer;

import java.util.Objects;
import java.util.Optional;

import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBemerkung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.UntersuchungBewegungsapparat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.durchfuehrung.UntersuchungDurchfuehrung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.UntersuchungGroesseGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenOtoskopie;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.kind.UntersuchungKindDaten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.laeusekontrolle.UntersuchungLaeusekontrolle;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.UntersuchungSchulrelevanteBeeintraechtigung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.UntersuchungSprache;
import ch.dvbern.oss.hemed.esc.api.features.anlass.triage.UntersuchungTriage;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;

import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@UtilityClass
public class ExtractorUtil {

    @NonNull
    public static Optional<UntersuchungHoerenAudiogramm> getUntersuchungHoerenAudiogrammOptional(
        @NonNull Einladung einladung
    ) {
        return getUntersuchungHoerenAudiogrammOptional(einladung.getUntersuchung());
    }

    @NonNull
    public static Optional<UntersuchungHoerenAudiogramm> getUntersuchungHoerenAudiogrammOptional(
        @Nullable Untersuchung untersuchung
    ) {
        if (untersuchung == null) {
            return Optional.empty();
        }

        UntersuchungHoerenAudiogramm hoerenAudiogramm = untersuchung.getUntersuchungHoerenAudiogramm();
        if (hoerenAudiogramm == null) {
            return Optional.empty();
        }

        return Optional.of(hoerenAudiogramm);
    }

    @NonNull
    public static Optional<UntersuchungHoerenOtoskopie> getUntersuchungHoerenOtoskopieOptional(
        @NonNull Einladung einladung
    ) {
        return getUntersuchungHoerenOtoskopieOptional(einladung.getUntersuchung());
    }

    @NonNull
    public static Optional<UntersuchungHoerenOtoskopie> getUntersuchungHoerenOtoskopieOptional(
        @Nullable Untersuchung untersuchung
    ) {
        if (untersuchung == null) {
            return Optional.empty();
        }

        UntersuchungHoerenOtoskopie hoerenOtoskopie = untersuchung.getUntersuchungHoerenOtoskopie();
        if (hoerenOtoskopie == null) {
            return Optional.empty();
        }

        return Optional.of(hoerenOtoskopie);
    }

    @NonNull
    public static Optional<UntersuchungSehenBeurteilung> getUntersuchungSehenOptional(
        @NonNull Einladung einladung
    ) {
        return getUntersuchungSehenOptional(einladung.getUntersuchung());
    }

    @NonNull
    public static Optional<UntersuchungSehenBeurteilung> getUntersuchungSehenOptional(
        @Nullable Untersuchung untersuchung
    ) {
        if (untersuchung == null) {
            return Optional.empty();
        }

        UntersuchungSehenBeurteilung untersuchungSehenBeurteilung = untersuchung.getUntersuchungSehenBeurteilung();
        if (untersuchungSehenBeurteilung == null) {
            return Optional.empty();
        }

        return Optional.of(untersuchungSehenBeurteilung);
    }

    @NonNull
    public static Optional<UntersuchungDurchfuehrung> getUntersuchungDurchfuehrungOptional(
        @NonNull Einladung einladung
    ) {
        return getUntersuchungDurchfuehrungOptional(einladung.getUntersuchung());
    }

    @NonNull
    public static Optional<UntersuchungDurchfuehrung> getUntersuchungDurchfuehrungOptional(
        @Nullable Untersuchung untersuchung
    ) {
        if (untersuchung == null) {
            return Optional.empty();
        }

        UntersuchungDurchfuehrung untersuchungDurchfuehrung = untersuchung.getUntersuchungDurchfuehrung();
        if (untersuchungDurchfuehrung == null) {
            return Optional.empty();
        }
        return Optional.of(untersuchungDurchfuehrung);
    }

    @NonNull
    public static Optional<UntersuchungImpfungen> getUntersuchungImpfungenOptional(
        @NonNull Einladung einladung
    ) {
        return getUntersuchungImpfungenOptional(einladung.getUntersuchung());
    }

    @NonNull
    public static Optional<UntersuchungImpfungen> getUntersuchungImpfungenOptional(
        @Nullable Untersuchung untersuchung) {
        if (untersuchung == null) {
            return Optional.empty();
        }

        UntersuchungImpfungen untersuchungImpfungen = untersuchung.getUntersuchungImpfungen();
        if (untersuchungImpfungen == null) {
            return Optional.empty();
        }

        return Optional.of(untersuchungImpfungen);
    }

    @NonNull
    public static Optional<UntersuchungKindDaten> getUntersuchungKindDatenOptional(
        @NonNull Einladung einladung
    ) {
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
        return getUntersuchungGroesseGewichtOptional(einladung.getUntersuchung());
    }

    @NonNull
    public static Optional<UntersuchungGroesseGewicht> getUntersuchungGroesseGewichtOptional(
        @Nullable Untersuchung untersuchung
    ) {
        if (untersuchung == null) {
            return Optional.empty();
        }

        UntersuchungGroesseGewicht untersuchungGroesseGewicht = untersuchung.getUntersuchungGroesseGewicht();
        if (untersuchungGroesseGewicht == null) {
            return Optional.empty();
        }
        return Optional.of(untersuchungGroesseGewicht);
    }

    @NonNull
    public static Optional<UntersuchungTriage> getUntersuchungTriageOptional(
        @NonNull Einladung einladung
    ) {
        return getUntersuchungTriageOptional(einladung.getUntersuchung());
    }

    @NonNull
    public static Optional<UntersuchungTriage> getUntersuchungTriageOptional(
        @Nullable Untersuchung untersuchung
    ) {
        if (untersuchung == null) {
            return Optional.empty();
        }

        UntersuchungTriage untersuchungTriage = untersuchung.getUntersuchungTriage();
        if (untersuchungTriage == null) {
            return Optional.empty();
        }
        return Optional.of(untersuchungTriage);
    }

    @NonNull
    public static Optional<UntersuchungMassnahmen> getUntersuchungMassnahmenOptional(
        @NonNull Einladung einladung
    ) {
        return getUntersuchungMassnahmenOptional(einladung.getUntersuchung());
    }

    @NonNull
    public static Optional<UntersuchungMassnahmen> getUntersuchungMassnahmenOptional(
        @Nullable Untersuchung untersuchung
    ) {
        if (untersuchung == null) {
            return Optional.empty();
        }

        UntersuchungMassnahmen untersuchungMassnahmen = untersuchung.getUntersuchungMassnahmen();
        if (untersuchungMassnahmen == null) {
            return Optional.empty();
        }
        return Optional.of(untersuchungMassnahmen);
    }

    @NonNull
    public static Optional<UntersuchungImpfungenMassnahmen> getUntersuchungImpfungenMassnahmenOptional(
        @NonNull Einladung einladung
    ) {
        return getUntersuchungImpfungenMassnahmenOptional(einladung.getUntersuchung());
    }

    @NonNull
    public static Optional<UntersuchungImpfungenMassnahmen> getUntersuchungImpfungenMassnahmenOptional(
        @Nullable Untersuchung untersuchung
    ) {
        if (untersuchung == null) {
            return Optional.empty();
        }

        UntersuchungImpfungen untersuchungImpfungen = untersuchung.getUntersuchungImpfungen();
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
        return getUntersuchungArztOptional(einladung.getUntersuchung());
    }

    @NonNull
    public static Optional<UntersuchungArztBeurteilung> getUntersuchungArztOptional(
        @Nullable Untersuchung untersuchung
    ) {
        if (untersuchung == null) {
            return Optional.empty();
        }

        UntersuchungArztBeurteilung untersuchungArzt = untersuchung.getUntersuchungArztBeurteilung();
        if (untersuchungArzt == null) {
            return Optional.empty();
        }
        return Optional.of(untersuchungArzt);
    }

    @NonNull
    public static Optional<UntersuchungArztBemerkung> getUntersuchungArztBemerkungOptional(
        @Nullable Untersuchung untersuchung
    ) {
        if (untersuchung == null) {
            return Optional.empty();
        }
        UntersuchungArztBemerkung untersuchungArzt = untersuchung.getUntersuchungArztBemerkung();
        if (untersuchungArzt == null) {
            return Optional.empty();
        }
        return Optional.of(untersuchungArzt);
    }

    @NonNull
    public static Optional<UntersuchungSprache> getUntersuchungSpracheOptional(
        @NonNull Einladung einladung
    ) {
        return getUntersuchungSpracheOptional(einladung.getUntersuchung());
    }

    @NonNull
    public static Optional<UntersuchungSprache> getUntersuchungSpracheOptional(
        @Nullable Untersuchung untersuchung
    ) {
        if (untersuchung == null) {
            return Optional.empty();
        }
        UntersuchungSprache untersuchungSprache = untersuchung.getUntersuchungSprache();
        if (untersuchungSprache == null) {
            return Optional.empty();
        }
        return Optional.of(untersuchungSprache);
    }

    @NonNull
    public static Optional<UntersuchungBewegungsapparat> getUntersuchungBewegungsapparatOptional(
        @NonNull Einladung einladung
    ) {
        return getUntersuchungBewegungsapparatOptional(einladung.getUntersuchung());
    }

    @NonNull
    public static Optional<UntersuchungBewegungsapparat> getUntersuchungBewegungsapparatOptional(
        @Nullable Untersuchung untersuchung
    ) {
        if (untersuchung == null) {
            return Optional.empty();
        }
        UntersuchungBewegungsapparat untersuchungBewegungsapparat = untersuchung.getUntersuchungBewegungsapparat();
        if (untersuchungBewegungsapparat == null) {
            return Optional.empty();
        }
        return Optional.of(untersuchungBewegungsapparat);
    }

    @NonNull
    public static Optional<UntersuchungSchulrelevanteBeeintraechtigung> getUntersuchungSchulrelevanteBeeintraechtigungOptional(
        @NonNull Einladung einladung
    ) {
        return getUntersuchungSchulrelevanteBeeintraechtigungOptional(einladung.getUntersuchung());
    }

    @NonNull
    public static Optional<UntersuchungSchulrelevanteBeeintraechtigung> getUntersuchungSchulrelevanteBeeintraechtigungOptional(
        @Nullable Untersuchung untersuchung
    ) {
        if (untersuchung == null) {
            return Optional.empty();
        }
        UntersuchungSchulrelevanteBeeintraechtigung beeintraechtigung = untersuchung.getUntersuchungSchulrelevanteBeeintraechtigung();
        if (beeintraechtigung == null) {
            return Optional.empty();
        }
        return Optional.of(beeintraechtigung);
    }

    @NonNull
    public static Optional<UntersuchungLaeusekontrolle> getUntersuchungLaeusekontrolleOptional(
        @Nullable Untersuchung untersuchung
    ) {
        if (untersuchung == null) {
            return Optional.empty();
        }

        UntersuchungLaeusekontrolle untersuchungLaeusekontrolle = untersuchung.getUntersuchungLaeusekontrolle();
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
