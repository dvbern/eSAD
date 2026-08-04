package ch.dvbern.oss.hemed.esc.api.shared.aufgabe;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.types.InformationEltern;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.features.labels.LabelService;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.types.AufgabeStatus;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.types.Path;
import ch.dvbern.oss.hemed.esc.api.shared.util.KindValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutoAufgabeService {

    private final AufgabeService aufgabeService;
    private final BenutzerService benutzerService;
    private final LabelService labelService;
    private final ConfigService configService;
    private final TL tl;
    private final AppClock appClock;

    private static final String ANLASS = "anlass";
    private static final String KIND = "kind";

    public void createAufgabeDoppelkontrolleImpfentscheid(
        @NonNull Einladung einladung,
        @NonNull Benutzer empfaenger
    ) {
        Kind kind = einladung.getDossier().getKind();
        KindValidationUtil.throwErrorIfKindIsInactive(
            kind,
            AppValidationMessage.cannotCreateEntityForInactiveKind(
                kind,
                new Aufgabe()
            ));
        Anlass anlass = einladung.getAnlass();
        String title = tl.translate(
            "aufgabe.doppelkontrolle.impfentscheid",
            KIND, kind.getNameVorname(),
            ANLASS, anlass.getFullAnlassTitel(tl));
        createAutomatischeAufgabe(
            title,
            Label.ID_LABEL_DOPPELKONTROLLE_IMPFENTSCHEID,
            empfaenger,
            anlass,
            kind,
            null,
            buildAnlassUntersuchungUrl(einladung)
        );
    }

    public void createAufgabeGruppenFolgetermin(@NonNull Anlass anlass) {
        List<Aufgabe> existing =
            this.aufgabeService.findAufgabenForAnlassAndLabel(anlass, Label.ID_LABEL_GRUPPEN_FOLGETERMIN);
        if (existing.size() > 1) {
            throw new IllegalStateException("Es gibt mehr als eine Aufgabe für den Anlass " + anlass.getId());
        }
        Benutzer mpa = anlass.getMpa();
        if (mpa == null) {
            throw new AppValidationException(AppValidationMessage.noMpaForAufgabeEmpfaenger(
                anlass.getFullAnlassTitel(tl),
                anlass.getId()
            ));
        }
        String titel = tl.translate(
            "aufgabe.folgetermin.titel",
            ANLASS, anlass.getFullAnlassTitel(tl));
        Aufgabe aufgabe;
        if (existing.size() == 1) {
            aufgabe = existing.getFirst();
            updateAutomatischeAufgabe(aufgabe, titel, null);
        } else {
            createAutomatischeAufgabe(
                titel,
                Label.ID_LABEL_GRUPPEN_FOLGETERMIN,
                mpa,
                anlass,
                null,
                null,
                null
            );
        }
    }

    public void createOrUpdateAufgabeMassnahmeInformationEltern(
        @NonNull UntersuchungMassnahmen massnahmen,
        @NonNull Kind kind,
        @NonNull Anlass anlass
    ) {
        String title = tl.translate(
            "aufgabe.massnahme.information.eltern",
            KIND, kind.getNameVorname(),
            ANLASS, anlass.getFullAnlassTitel(tl));
        String bemerkung = null;
        if (massnahmen.getInformationEltern() != null && massnahmen.getInformationEltern() != InformationEltern.KEINE) {
            bemerkung = tl.translate(
                "aufgabe.massnahme.information.eltern.bemerkung",
                "informationEltern",
                tl.translate("aufgabe.massnahme.information.eltern.key." + massnahmen.getInformationEltern()));
        }
        if (massnahmen.getAufgabeInformationEltern() != null) {
            updateAutomatischeAufgabe(massnahmen.getAufgabeInformationEltern(), title, bemerkung);
        } else {
            Aufgabe aufgabe =
                createAutomatischeAufgabe(title, null, getCurrentBenutzer(), anlass, kind, bemerkung, null);
            massnahmen.setAufgabeInformationEltern(aufgabe);
        }
    }

    public void createOrUpdateAufgabeMassnahmeUeberweisungHausarzt(
        @NonNull UntersuchungMassnahmen massnahmen,
        @NonNull Kind kind,
        @NonNull Anlass anlass
    ) {
        String title = tl.translate(
            "aufgabe.massnahme.ueberweisung.hausarzt",
            KIND, kind.getNameVorname(),
            ANLASS, anlass.getFullAnlassTitel(tl));
        if (massnahmen.getAufgabeUeberweisungHausarzt() != null) {
            updateAutomatischeAufgabe(massnahmen.getAufgabeUeberweisungHausarzt(), title, null);
        } else {
            Aufgabe aufgabe = createAutomatischeAufgabe(title, null, getCurrentBenutzer(), anlass, kind, null, null);
            massnahmen.setAufgabeUeberweisungHausarzt(aufgabe);
        }
    }

    public void createOrUpdateAufgabeMassnahmeUeberweisungAugenarzt(
        @NonNull UntersuchungMassnahmen massnahmen,
        @NonNull Kind kind,
        @NonNull Anlass anlass
    ) {
        String title = tl.translate(
            "aufgabe.massnahme.ueberweisung.augenarzt",
            KIND, kind.getNameVorname(),
            ANLASS, anlass.getFullAnlassTitel(tl));
        if (massnahmen.getAufgabeUeberweisungAugenarzt() != null) {
            updateAutomatischeAufgabe(massnahmen.getAufgabeUeberweisungAugenarzt(), title, null);
        } else {
            Aufgabe aufgabe = createAutomatischeAufgabe(title, null, getCurrentBenutzer(), anlass, kind, null, null);
            massnahmen.setAufgabeUeberweisungAugenarzt(aufgabe);
        }
    }

    public void createOrUpdateAufgabeMassnahmeNachkontrolle(
        @NonNull UntersuchungMassnahmen massnahmen,
        @NonNull Kind kind,
        @NonNull Anlass anlass
    ) {
        String title = tl.translate(
            "aufgabe.massnahme.nachkontrolle",
            KIND, kind.getNameVorname(),
            ANLASS, anlass.getFullAnlassTitel(tl));
        if (massnahmen.getAufgabeNachkontrolle() != null) {
            updateAutomatischeAufgabe(massnahmen.getAufgabeNachkontrolle(), title, null);
        } else {
            Aufgabe aufgabe = createAutomatischeAufgabe(title, null, getCurrentBenutzer(), anlass, kind, null, null);
            massnahmen.setAufgabeNachkontrolle(aufgabe);
        }
    }

    public void createOrUpdateAufgabeMassnahmeImpfenInformationEltern(
        @NonNull UntersuchungImpfungenMassnahmen massnahmen,
        @NonNull Einladung einladung
    ) {
        Anlass anlass = einladung.getAnlass();
        Kind kind = einladung.getDossier().getKind();
        String title = tl.translate(
            "aufgabe.massnahme.impfen.information.eltern",
            KIND, kind.getNameVorname(),
            ANLASS, anlass.getFullAnlassTitel(tl));
        if (massnahmen.getAufgabeInformationEltern() != null) {
            updateAutomatischeAufgabe(massnahmen.getAufgabeInformationEltern(), title, null);
        } else {
            Aufgabe aufgabe = createAutomatischeAufgabe(
                title, null, getCurrentBenutzer(), anlass, kind, null, buildAnlassUntersuchungUrl(
                    einladung));
            massnahmen.setAufgabeInformationEltern(aufgabe);
        }
    }

    public void createOrUpdateAufgabeMassnahmeImpfenInformationHausarzt(
        @NonNull UntersuchungImpfungenMassnahmen massnahmen,
        @NonNull Einladung einladung
    ) {
        Anlass anlass = einladung.getAnlass();
        Kind kind = einladung.getDossier().getKind();
        String title = tl.translate(
            "aufgabe.massnahme.impfen.information.hausarzt",
            KIND, kind.getNameVorname(),
            ANLASS, anlass.getFullAnlassTitel(tl));
        if (massnahmen.getAufgabeInformationHausarzt() != null) {
            updateAutomatischeAufgabe(massnahmen.getAufgabeInformationHausarzt(), title, null);
        } else {
            Aufgabe aufgabe = createAutomatischeAufgabe(
                title, null, getCurrentBenutzer(), anlass, kind, null, buildAnlassUntersuchungUrl(
                    einladung));
            massnahmen.setAufgabeInformationHausarzt(aufgabe);
        }
    }

    public void createOrUpdateAufgabeMassnahmeImpfenRueckfrageHausarzt(
        @NonNull UntersuchungImpfungenMassnahmen massnahmen,
        @NonNull Einladung einladung
    ) {
        Anlass anlass = einladung.getAnlass();
        Kind kind = einladung.getDossier().getKind();
        String title = tl.translate(
            "aufgabe.massnahme.impfen.rueckfrage.hausarzt",
            KIND, kind.getNameVorname(),
            ANLASS, anlass.getFullAnlassTitel(tl));
        if (massnahmen.getAufgabeRueckfrageHausarzt() != null) {
            updateAutomatischeAufgabe(massnahmen.getAufgabeRueckfrageHausarzt(), title, null);
        } else {
            Aufgabe aufgabe = createAutomatischeAufgabe(
                title, null, getCurrentBenutzer(), anlass, kind, null, buildAnlassUntersuchungUrl(
                    einladung));
            massnahmen.setAufgabeRueckfrageHausarzt(aufgabe);
        }
    }

    public void createAufgabeMassnahmeImpfenEinzelTerminErstellen(@NonNull Einladung einladung) {
        Anlass anlass = einladung.getAnlass();
        Kind kind = einladung.getDossier().getKind();
        String title = tl.translate(
            "aufgabe.massnahme.impfen.einzeltermin.erstellen",
            KIND, kind.getNameVorname(),
            ANLASS, anlass.getFullAnlassTitel(tl));
        createAutomatischeAufgabe(
            title,
            null,
            getCurrentBenutzer(),
            anlass,
            kind,
            null,
            buildAnlassUntersuchungUrl(einladung));
    }

    public void createOrUpdateAufgabeEinladungStatusNachzuegler(@NonNull Einladung einladung) {
        Anlass anlass = einladung.getAnlass();
        Kind kind = einladung.getDossier().getKind();
        String title = tl.translate(
            "aufgabe.einladung.status.nachzuegler",
            KIND, kind.getNameVorname(),
            ANLASS, anlass.getFullAnlassTitel(tl));
        Optional<Aufgabe> aufgabeOptional =
            aufgabeService.findSingleAufgabeForKindWithLabel(kind, Label.ID_LABEL_NACHZUEGLER);
        if (aufgabeOptional.isPresent()) {
            updateAutomatischeAufgabe(aufgabeOptional.get(), title, null);
        } else {
            createAutomatischeAufgabe(
                title,
                Label.ID_LABEL_NACHZUEGLER,
                getCurrentBenutzer(),
                anlass,
                kind,
                null,
                buildAnlassUntersuchungUrl(einladung));
        }
    }

    public void aufgabeAbschliessen(@Nullable Aufgabe aufgabe) {
        if (aufgabe != null) {
            aufgabe.disableOptimisticLockingCheck();
            aufgabe.setAufgabeStatus(AufgabeStatus.ABGESCHLOSSEN);
            aufgabeService.persist(aufgabe);
        }
    }

    private Aufgabe createAutomatischeAufgabe(
        @NonNull String titel,
        @Nullable ID<Label> labelID,
        @NonNull Benutzer empfaenger,
        @Nullable Anlass anlass,
        @Nullable Kind kind,
        @Nullable String bemerkung,
        @Nullable URL link
    ) {
        Aufgabe aufgabe = new Aufgabe(
            titel,
            bemerkung,
            AufgabeStatus.OFFEN,
            appClock.getToday(),
            getCurrentBenutzer(),
            empfaenger,
            labelID != null ? Set.of(labelService.getById(labelID)) : Collections.emptySet(),
            kind,
            anlass,
            new ArrayList<>(),
            link);
        return aufgabeService.persist(aufgabe);
    }

    private void updateAutomatischeAufgabe(
        @NonNull Aufgabe aufgabe,
        @NonNull String titel,
        @Nullable String bemerkung
    ) {
        aufgabe.setTitel(titel);
        aufgabe.setBemerkung(getAufgabeBemerkung(aufgabe.getBemerkung(), bemerkung));
        aufgabe.setFaelligBis(appClock.getToday());
        aufgabe.disableOptimisticLockingCheck();
        aufgabeService.persist(aufgabe);
    }

    @Nullable
    private String getAufgabeBemerkung(@Nullable String oldBemerkung, @Nullable String newBemerkung) {
        if (oldBemerkung == null && newBemerkung == null) {
            return null;
        }
        if (oldBemerkung != null && newBemerkung == null) {
            return oldBemerkung;
        }
        if (oldBemerkung == null) {
            return newBemerkung;
        }
        return oldBemerkung + '\n' + newBemerkung;
    }

    @NonNull
    private Benutzer getCurrentBenutzer() {
        return benutzerService.getCurrentBenutzerOrThrowException();
    }

    @NonNull
    private URL buildAnlassUntersuchungUrl(@NonNull Einladung einladung) {
        String anlassId = einladung.getAnlass().getId().getId();
        String einladungId = einladung.getId().getId();
        try {
            URIBuilder builder = new URIBuilder();
            builder.setScheme(configService.getPublicUrl().getProtocol());
            builder.setHost(configService.getPublicUrl().getHost());
            if (configService.getPublicUrl().getPort() != -1) {
                builder.setPort(configService.getPublicUrl().getPort());
            }
            builder.setPathSegments(
                Path.ANLASS.getValue(),
                anlassId,
                Path.ANLASS_UNTERSUCHUNG.getValue(),
                Path.ANLASS_UNTERSUCHUNG_EINLADUNG.getValue(),
                einladungId,
                Path.ANLASS_UNTERSUCHUNG_EINLADUNG_IMPFUNGEN.getValue());
            builder.setParameter(Path.ANLASS_UNTERSUCHUNG_EINLADUNG_IDS_SEGMENT.getValue(), einladungId);
            return builder.build().toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new AppFailureException(
                AppFailureMessage.internalError("Could not build anlass url for " + einladungId), e);
        }
    }
}
