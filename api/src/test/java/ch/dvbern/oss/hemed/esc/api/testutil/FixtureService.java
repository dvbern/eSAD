/*
 *
 * Copyright (C) 2025 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.testutil;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.dvbstarter.shared.types.sozialversicherungsnummer.SozialversicherungsNummer;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.durchfuehrung.UntersuchungDurchfuehrung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.durchfuehrung.UntersuchungDurchfuehrungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheitService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungInternService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungenService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.kind.UntersuchungKindDaten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.kind.UntersuchungKindDatenService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungRepo;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBemerkungFragebogen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBemerkungFragebogenService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBeurteilungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.triage.UntersuchungTriage;
import ch.dvbern.oss.hemed.esc.api.features.anlass.triage.UntersuchungTriageService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBemerkung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBemerkungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBeurteilungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Bewegungsapparat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Blutdruck;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinhuepfen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinstand;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Motorik;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfigRepo;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileBmi;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGroesse;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.UntersuchungGroesseGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.UntersuchungGroesseGewichtService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.Dezibel;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenZusaetzlicheAngaben;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.OtoskopieResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogrammService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenOtoskopie;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenOtoskopieService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.laeusekontrolle.types.LaeusekontrolleBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.laeusekontrolle.UntersuchungLaeusekontrolle;
import ch.dvbern.oss.hemed.esc.api.features.anlass.laeusekontrolle.UntersuchungLaeusekontrolleService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.types.InformationEltern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmenService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmenService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.types.ArtAbmeldung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.BrilleLinsenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.types.EinladungStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.FarbsehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.types.GrundAbmeldung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.InspektionKonvergenzResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.MotilitaetResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StereotestResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusAuftretenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusAuspraegungResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusResultat;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.Briefvorlage;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.BriefvorlageService;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.BriefvorlageType;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.Impfstoff;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.ImpfstoffCharge;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.ImpfstoffChargeService;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.ImpfstoffService;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.DossierService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Nachkontrolle;
import ch.dvbern.oss.hemed.esc.api.features.kinder.NachkontrolleService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKindService;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.features.labels.LabelService;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizService;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizTyp;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.SchuleService;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.KlasseService;
import ch.dvbern.oss.hemed.esc.api.features.schule.kontaktpersonschule.KontaktPersonSchule;
import ch.dvbern.oss.hemed.esc.api.features.schule.kontaktpersonschule.KontaktPersonSchuleService;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.Lehrperson;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.LehrpersonService;
import ch.dvbern.oss.hemed.esc.api.features.schule.quartier.Quartier;
import ch.dvbern.oss.hemed.esc.api.features.schule.quartier.QuartierService;
import ch.dvbern.oss.hemed.esc.api.features.schule.types.Klassenstufe;
import ch.dvbern.oss.hemed.esc.api.features.schule.types.KontaktPersonSchuleFunktion;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.Aufgabe;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.AufgabeService;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileUploadWrapper;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AufgabeBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BriefvorlageBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.DossierBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.EinladungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.ImpfstoffBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.ImpfstoffChargeBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KindBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KlasseBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KontaktPersonKindBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KontaktPersonSchuleBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.LehrpersonBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.NachkontrolleBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.NotizBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.QuartierBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.SchuleBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungArztBemerkungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungArztBeurteilungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungDurchfuehrungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungGroesseGewichtBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungHoerenAudiogrammBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungHoerenOtoskopieBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfstatusKrankheitBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungInternBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungenBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungenMassnahmenBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungLaeusekontrolleBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungMassnahmenBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungSehenBemerkungFragebogenBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungSehenBeurteilungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungTriageBuilder;
import jakarta.enterprise.context.RequestScoped;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.jetbrains.annotations.NotNull;

@RequestScoped
@Transactional
@RequiredArgsConstructor
@Slf4j
public class FixtureService {

    private final KontaktPersonSchuleService kontaktPersonSchuleService;
    private final KontaktPersonKindService kontaktPersonKindService;
    private final QuartierService quartierService;
    private final KindService kindService;
    private final DossierService dossierService;
    private final AnlassConfigRepo anlassConfigRepo;
    private final AnlassService anlassService;
    private final EinladungService einladungService;
    private final BenutzerService benutzerService;
    private final NotizService notizService;
    private final UntersuchungService untersuchungService;
    private final UntersuchungDurchfuehrungService untersuchungDurchfuehrungService;
    private final UntersuchungTriageService untersuchungTriageService;
    private final UntersuchungLaeusekontrolleService untersuchungLaeusekontrolleService;
    private final UntersuchungImpfungenService untersuchungImpfungenService;
    private final ImpfstoffService impfstoffService;
    private final ImpfstoffChargeService impfstoffChargeService;
    private final SchuleService schuleService;
    private final KlasseService klasseService;
    private final AufgabeService aufgabeService;
    private final LehrpersonService lehrpersonService;
    private final LabelService labelService;
    private final AppClock appClock;
    private final UntersuchungSehenBeurteilungService untersuchungSehenBeurteilungService;
    private final UntersuchungSehenBemerkungFragebogenService untersuchungSehenBemerkungFragebogenService;
    private final UntersuchungHoerenAudiogrammService untersuchungHoerenAudiogrammService;
    private final UntersuchungHoerenOtoskopieService untersuchungHoerenOtoskopieService;
    private final UntersuchungRepo untersuchungRepo;
    private final UntersuchungImpfungInternService untersuchungImpfungInternService;
    private final UntersuchungImpfstatusKrankheitService untersuchungImpfstatusKrankheitService;
    private final UntersuchungMassnahmenService untersuchungMassnahmenService;
    private final UntersuchungGroesseGewichtService untersuchungGroesseGewichtService;
    private final UntersuchungArztBeurteilungService untersuchungArztBeurteilungService;
    private final UntersuchungArztBemerkungService untersuchungArztBemerkungService;
    private final NachkontrolleService nachkontrolleService;
    private final BriefvorlageService briefvorlageService;
    private final UntersuchungKindDatenService untersuchungKindDatenService;
    private final UntersuchungImpfungenMassnahmenService untersuchungImpfungenMassnahmenService;

    @NonNull
    public KontaktPersonSchule createKontaktPersonSchule(@NonNull Schule... schulen) {
        KontaktPersonSchule lehrperson = KontaktPersonSchuleBuilder.create(
            builder -> builder
                .forSchule(schulen)
                .withFunktion(KontaktPersonSchuleFunktion.SEKRETARIAT));
        return kontaktPersonSchuleService.persist(lehrperson);
    }

    @NonNull
    public Lehrperson createLehrperson(@NonNull Klasse... klassen) {
        Lehrperson lehrperson = LehrpersonBuilder.create(
            builder -> builder
                .forKlasse(klassen));
        Arrays.stream(klassen).forEach(k -> {
            var kToAddL = klasseService.getById(k.getId());
            kToAddL.disableOptimisticLockingCheck();
            kToAddL.addLehrperson(lehrperson);
        });
        return lehrpersonService.persist(lehrperson);
    }

    @NonNull
    public KontaktPersonKind createKontaktPersonKind(@NonNull Kind... kinder) {
        KontaktPersonKind elternteil = KontaktPersonKindBuilder.create(
            builder -> builder
                .forKind(kinder));
        return kontaktPersonKindService.persist(elternteil);
    }

    @NonNull
    public Quartier createQuartier(@NonNull String quartierName) {
        Quartier quartier = QuartierBuilder.create(
            builder -> builder
                .withQuartier(quartierName));
        return quartierService.persist(quartier);
    }

    @NonNull
    public Anlass createAnlass() {
        return createAnlass(AnlassStatus.IN_PLANUNG);
    }

    @NonNull
    public Anlass createAnlass(@NonNull AnlassStatus status) {
        AnlassConfig config = createAnlassConfigAllOptional();
        return anlassService.persist(
            AnlassBuilder.create(builder -> builder
                .withTitle("Test getAnlass")
                .withAnlassStatus(status)
                .withDatumSetSchuljahr(LocalDate.of(2023, 5, 5))
                .withAnlassConfig(config))
        );
    }

    @NonNull
    public Anlass createAnlassWithSameRequirementSettings(@NonNull RequirementSetting requirementSetting) {
        LocalDate anlassDatum = LocalDate.of(2023, 5, 5);
        AnlassConfig config = anlassConfigRepo.getByAnlassTypAndStichtag(AnlassTyp.KLASSE_4, anlassDatum);//createAnlassConfigAllSameRequirementSetting(requirementSetting);
        updateAnlassConfigWithRequirementSettings(config, requirementSetting);
        return anlassService.persist(
            AnlassBuilder.create(builder -> builder
                .withTitle("Test getAnlass")
                .withAnlassStatus(AnlassStatus.IN_PLANUNG)
                .withDatumSetSchuljahr(anlassDatum)
                .withAnlassConfig(config))
        );
    }

    public void updateAnlassConfigWithRequirementSettings(@NonNull AnlassConfig existingConfig, @NonNull RequirementSetting requirementSetting) {
        boolean show = requirementSetting != RequirementSetting.NOT_VISIBLE;
        // Daten
        existingConfig.setShowDatenPersonendaten(show);
        existingConfig.setDatenUntersuchung(requirementSetting);
        existingConfig.setShowDatenSchule(show);
        existingConfig.setDatenBemerkung(requirementSetting);
        // Impfungen
        existingConfig.setImpfungenUebersicht(requirementSetting);
        existingConfig.setImpfungenListe(requirementSetting);
        existingConfig.setImpfungenMassnahmeGruppenFolgetermin(show);
        existingConfig.setImpfungenMassnahmeEinzelFolgetermin(show);
        existingConfig.setImpfungenMassnahmeInfoKinderarzt(show);
        existingConfig.setImpfungenMassnahmeBeratungMPA(show);
        existingConfig.setImpfungenMassnahmeInfomaterialAbgegeben(show);
        existingConfig.setImpfungenMassnahmeInfoEltern(show);
        existingConfig.setImpfungenMassnahmeRueckfrageKinderarzt(show);
        existingConfig.setCheckEinwilligungVorhanden(requirementSetting);
        existingConfig.setCheckKontraindikationenAbgeklaert(requirementSetting);
        existingConfig.setCheckDoppelkontrolleImpfentscheidDurchgefuehrt(requirementSetting);
        existingConfig.setUntersuchungImpfungEmpfehlung(requirementSetting);
        existingConfig.setUntersuchungImpfungAchtung(requirementSetting);
        existingConfig.setUntersuchungImpfungBemerkungen(requirementSetting);
        // Sehen
        existingConfig.setSehenBrilleLinsen(requirementSetting);
        existingConfig.setSehenFarbsehen(requirementSetting);
        existingConfig.setSehenTafel(requirementSetting);
        existingConfig.setSehenFernvisus(requirementSetting);
        existingConfig.setSehenStereoTest(requirementSetting);
        existingConfig.setSehenStrabismus(requirementSetting);
        existingConfig.setSehenInspektion(requirementSetting);
        existingConfig.setSehenKonvergenz(requirementSetting);
        existingConfig.setSehenMotilitaet(requirementSetting);
        existingConfig.setSehenNahvisus(requirementSetting);
        existingConfig.setSehenBemerkungen(requirementSetting);
        existingConfig.setSehenBemerkungenFragebogen(requirementSetting);
        existingConfig.setSehenMassnahmen(requirementSetting);
        existingConfig.setSehenEmpfehlung(requirementSetting);
        // Hoeren - Audiogramm
        existingConfig.setHoerenAudiogramm(requirementSetting);
        existingConfig.setHoerenAudiogramm250Hz(requirementSetting);
        existingConfig.setHoerenAudiogramm8000Hz(requirementSetting);
        existingConfig.setHoerenAudiogrammBeurteilung(requirementSetting);
        existingConfig.setHoerenAudiogrammZusaetzlicheAngaben(requirementSetting);
        existingConfig.setHoerenAudiogrammEmpfehlung(requirementSetting);
        existingConfig.setHoerenAudiogrammBemerkungen(requirementSetting);
        // Hoeren - Otoskopie
        existingConfig.setHoerenOtoskopie(requirementSetting);
        existingConfig.setHoerenOtoskopieBemerkungen(requirementSetting);
        // Groesse & Gewicht
        existingConfig.setGroesseGewichtGroesse(requirementSetting);
        existingConfig.setGroesseGewichtGewicht(requirementSetting);
        existingConfig.setGroesseGewichtBmi(requirementSetting);
        existingConfig.setGroesseGewichtGroessePerzentile(requirementSetting);
        existingConfig.setGroesseGewichtGewichtPerzentile(requirementSetting);
        existingConfig.setGroesseGewichtBmiPerzentile(requirementSetting);
        existingConfig.setGroesseGewichtBmiPerzentileValues(EnumSet.of(PerzentileBmi.PERZENTILE_HOCH));
        existingConfig.setGroesseGewichtBemerkungen(requirementSetting);
        // Triage
        existingConfig.setShowTriageTriage(show);
        existingConfig.setShowTriageKindWeiterleiten(show);
        // Aerztliche Untersuchung
        existingConfig.setUntersuchungBewegungsapparat(requirementSetting);
        existingConfig.setUntersuchungEinbeinstand(requirementSetting);
        existingConfig.setUntersuchungEinbeinstandRechts(requirementSetting);
        existingConfig.setUntersuchungEinbeinstandLinks(requirementSetting);
        existingConfig.setUntersuchungEinbeinhuepfen(requirementSetting);
        existingConfig.setUntersuchungEinbeinhuepfenRechts(requirementSetting);
        existingConfig.setUntersuchungEinbeinhuepfenLinks(requirementSetting);
        existingConfig.setUntersuchungFingeropposition(requirementSetting);
        existingConfig.setUntersuchungMotorik(requirementSetting);
        existingConfig.setUntersuchungSprache(requirementSetting);
        existingConfig.setUntersuchungMundZaehne(requirementSetting);
        existingConfig.setUntersuchungHaut(requirementSetting);
        existingConfig.setUntersuchungHerz(requirementSetting);
        existingConfig.setUntersuchungBlutdruck(requirementSetting);
        existingConfig.setUntersuchungVerhalten(requirementSetting);
        existingConfig.setUntersuchungEmpfehlung(requirementSetting);
        existingConfig.setUntersuchungBemerkungen(requirementSetting);
        // Massnahmen
        existingConfig.setShowMassnahmenPathologischeBefunde(show);
        existingConfig.setShowMassnahmenKeine(show);
        existingConfig.setShowMassnahmenBeratungMpa(show);
        existingConfig.setShowMassnahmenBeratungSchulAerztin(show);
        existingConfig.setShowMassnahmenBereitsBehandelt(show);
        existingConfig.setShowMassnahmenInfomaterial(show);
        existingConfig.setShowMassnahmenInfoEltern(show);
        existingConfig.setShowMassnahmenUeberweisungHausarzt(show);
        existingConfig.setShowMassnahmenUeberweisungAugenarzt(show);
        existingConfig.setShowMassnahmenNachkontrolleSaed(show);
        existingConfig.setShowMassnahmenAnderes(show);

        anlassConfigRepo.persist(existingConfig);
    }

    @NonNull
    public Anlass createAnlass(@NonNull LocalDate datum) {
        AnlassConfig config = createAnlassConfigAllOptional(datum.getYear(), AnlassTyp.KLASSE_4);
        return anlassService.persist(
            AnlassBuilder.create(builder -> builder
                .withTitle("Test getAnlass")
                .withDatumSetSchuljahr(datum)
                .withAnlassConfig(config))
        );
    }

    @NonNull
    public Anlass createAnlass(@NonNull AnlassTyp anlassTyp) {
        AnlassConfig config = createAnlassConfigAllOptional(2023, anlassTyp);
        return anlassService.persist(
            AnlassBuilder.create(builder -> builder
                .withTitle("Test getAnlass")
                .withDatumSetSchuljahr(LocalDate.of(2023, 5, 5))
                .withAnlassConfig(config))
        );
    }

    @NonNull
    public Anlass createAnlass(@NotNull Benutzer mpa) {
        AnlassConfig config = createAnlassConfigAllOptional(2023, AnlassTyp.KLASSE_8);

        return anlassService.persist(AnlassBuilder.create(builder -> builder
            .withTitle("Test getAnlass")
            .withDatumSetSchuljahr(LocalDate.of(2023, 5, 5))
            .withMPA(mpa)
            .withAnlassConfig(config))
        );
    }

    @NonNull
    public AnlassConfig createAnlassConfigAllOptional() {
        return createAnlassConfigAllOptional(AnlassTyp.KLASSE_4); // default Typ
    }

    @NonNull
    public AnlassConfig createAnlassConfigAllOptional(AnlassTyp anlassTyp) {
        return createAnlassConfigAllOptional(2023, anlassTyp); // default year
    }

    @NonNull
    public AnlassConfig createAnlassConfigAllOptional(int configYear, AnlassTyp anlassTyp) {
        AnlassConfig config = anlassConfigRepo.getByAnlassTypAndStichtag(anlassTyp, LocalDate.of(configYear, 1, 1));
        updateAnlassConfigWithRequirementSettings(config, RequirementSetting.OPTIONAL);
        return config;
    }

    @NonNull
    public Schule createSchule() {
        return schuleService.persist(SchuleBuilder.defaultSchule());
    }

    @NonNull
    public Schule createSchule(@NonNull String schulhaus) {
        Schule schule = SchuleBuilder.create(builder -> builder.withSchulhaus(schulhaus));
        return schuleService.persist(schule);
    }

    @NonNull
    public Schule createSchuleExternal(@NonNull String schulhaus, boolean aktivInExternalSystem) {
        Schule schule = SchuleBuilder.create(builder -> builder
            .withSchulhaus(schulhaus)
            .withExternalImportInfo(
                UUID.randomUUID().toString(),
                aktivInExternalSystem,
                appClock.offsetDateTime().now())
        );
        return schuleService.persist(schule);
    }

    @NonNull
    public Klasse createKlasse() {
        Klasse klasse = KlasseBuilder.create(builder -> builder
            .forSchule(createSchule()));
        return klasseService.persist(klasse);
    }

    @NonNull
    public Klasse createKlasse(@NonNull String klassenBezeichnung) {
        Klasse klasse = KlasseBuilder.create(builder -> builder
            .forSchule(createSchule())
            .withBezeichnung(klassenBezeichnung));
        return klasseService.persist(klasse);
    }

    @NonNull
    public Klasse createKlasseExternal(@NonNull String klassenBezeichnung, boolean aktivInExternalSystem) {
        Klasse klasse = KlasseBuilder.create(builder -> builder
            .forSchule(createSchule())
            .withBezeichnung(klassenBezeichnung)
            .withExternalImportInfo(
                UUID.randomUUID().toString(),
                aktivInExternalSystem,
                appClock.offsetDateTime().now()));
        return klasseService.persist(klasse);
    }

    @NonNull
    public Klasse createKlasseWithName(@NonNull Klassenstufe stufe, @NonNull String name) {
        Klasse klasse = KlasseBuilder.create(builder -> builder
            .forSchule(createSchule())
            .withBezeichnung(name)
            .withKlassenstufe(stufe));
        return klasseService.persist(klasse);
    }

    @NonNull
    public Klasse createKlasse(@NonNull Klassenstufe stufe) {
        Klasse klasse = KlasseBuilder.create(builder -> builder
            .forSchule(createSchule())
            .withKlassenstufe(stufe));
        return klasseService.persist(klasse);
    }

    @NonNull
    public Kind createKind() {
        Kind kind = KindBuilder.create(builder -> builder
            .withKlasse(createKlasse()));
        return kindService.persistWithoutDuplicateCheck(kind);
    }

    @NonNull
    public Kind createInactiveKind() {
        Kind kind = KindBuilder.create(builder -> builder
            .withKlasse(createKlasse())
            .withKindAktivStatus(EntityAktivStatus.INAKTIV));
        return kindService.persistWithoutDuplicateCheck(kind);
    }

    @NonNull
    public Kind createKind(
        @NonNull String vorname,
        @NonNull String name,
        @NonNull LocalDate geburtsdatum,
        @NonNull Geschlecht geschlecht
    ) {
        Kind kind = KindBuilder.create(builder -> builder
            .withName(name, vorname)
            .withGeburtsdatum(geburtsdatum)
            .withGeschlecht(geschlecht));
        return kindService.persistWithoutDuplicateCheck(kind);
    }

    @NonNull
    public Kind createKindEscada(
        @NonNull String vorname,
        @NonNull String name,
        @NonNull LocalDate geburtsdatum,
        @NonNull Geschlecht geschlecht,
        @Nullable SozialversicherungsNummer vnr,
        boolean aktivInExternalSystem
    ) {
        Kind kind = KindBuilder.create(builder -> builder
            .withName(name, vorname)
            .withGeburtsdatum(geburtsdatum)
            .withGeschlecht(geschlecht)
            .withSozialversicherungsNummer(vnr)
            .withExternalImportInfo(
                UUID.randomUUID().toString(),
                aktivInExternalSystem,
                appClock.offsetDateTime().now()));
        return kindService.persistWithoutDuplicateCheck(kind);
    }

    @NonNull
    public Kind createKind(@NonNull SozialversicherungsNummer sozNummer) {
        Kind kind = KindBuilder.create(builder -> builder
            .withSozialversicherungsNummer(sozNummer));
        return kindService.persistWithoutDuplicateCheck(kind);
    }

    @NonNull
    public Kind createKind(@NonNull Schule schule) {
        Kind kind = KindBuilder.create(builder -> builder
            .withSchule(schule));
        return kindService.persistWithoutDuplicateCheck(kind);
    }

    @NonNull
    public Kind createKind(@NonNull Klasse klasse) {
        Kind kind = KindBuilder.create(builder -> builder
            .withKlasse(klasse));
        return kindService.persistWithoutDuplicateCheck(kind);
    }

    @NonNull
    public Dossier createDossier() {
        return dossierService.persist(
            DossierBuilder.create(builder -> builder
                .forKind(createKind()))
        );
    }

    @NonNull
    public Dossier createDossier(@NonNull Kind kind) {
        return dossierService.persist(
            DossierBuilder.create(builder -> builder
                .forKind(kind))
        );
    }

    @NonNull
    public Einladung createEinladung() {
        final Anlass anlass = createAnlass();
        final Dossier dossier = createDossier();
        return einladungService.persist(
            EinladungBuilder.create(builder -> builder
                .withAnlass(anlass)
                .withDossier(dossier)));
    }

    @NonNull
    public Einladung createEinladungWithStatusAbgesagt() {
        final Anlass anlass = createAnlass();
        final Dossier dossier = createDossier();
        final Notiz notiz = createNotiz();
        return einladungService.persist(
            EinladungBuilder.create(builder -> builder
                .withAnlass(anlass)
                .withStatus(EinladungStatus.TEILNAHME_ABGESAGT)
                .withArtAbmeldung(ArtAbmeldung.INFO_LEHRPERSON)
                .withGrundAbmeldung(GrundAbmeldung.KONTROLLE_KINDER_PRIVATARZT)
                .withAbmeldungsNotiz(notiz)
                .withDossier(dossier)));
    }

    @NonNull
    public Einladung createEinladung(@NonNull Kind kind) {
        final Anlass anlass = createAnlass();
        final Dossier dossier = createDossier(kind);
        return einladungService.persist(
            EinladungBuilder.create(builder -> builder
                .withAnlass(anlass)
                .withDossier(dossier)));
    }

    @NonNull
    public Einladung createEinladung(@NonNull Anlass anlass) {
        final Dossier dossier = createDossier();
        return einladungService.persist(
            EinladungBuilder.create(builder -> builder
                .withAnlass(anlass)
                .withDossier(dossier)));
    }

    @NonNull
    public Einladung createEinladung(
        @NonNull Anlass anlass,
        @NonNull Dossier dossier) {
        return einladungService.persist(
            EinladungBuilder.create(builder -> builder
                .withAnlass(anlass)
                .withDossier(dossier)));
    }

    @Transactional(TxType.REQUIRES_NEW)
    public Einladung createEinladungInNewTransaction(
        @NonNull Anlass anlass,
        @NonNull Dossier dossier) {
        return einladungService.persist(
            EinladungBuilder.create(builder -> builder
                .withAnlass(anlass)
                .withDossier(dossier)));
    }

    @NonNull
    public Einladung createEinladung(
        @NonNull Anlass anlass,
        @NonNull Dossier dossier,
        @NonNull EinladungStatus status) {
        return einladungService.persist(
            EinladungBuilder.create(builder -> builder
                .withAnlass(anlass)
                .withDossier(dossier)
                .withStatus(status)));
    }

    @NonNull
    public Benutzer createBenutzer() {
        var createDto = BenutzerBuilder.createCreateDTO(
            builder -> builder
                .withRandomEmail("benno.benutzer@example.com")
                .withRandomVorname("Benno")
                .withRandomNachname("Benutzer")
                .withTelefon(NormalizedPhoneNumber.parse("+41 76 765 43 21"))
                .withKeycloakId(UUID.randomUUID().toString())
        );
        return benutzerService.getById(benutzerService.createBenutzer(createDto));
    }

    @NonNull
    public Notiz createNotiz() {
        return createNotiz(Collections.emptySet());
    }

    @NonNull
    public Notiz createNotiz(@NonNull Set<ID<Label>> labelIDs) {
        // Create a test dossier for all tests
        Kind kind = kindService.persistWithoutDuplicateCheck(KindBuilder.defaultKind());
        Dossier dossier = dossierService.persist(
            DossierBuilder.create(builder -> builder.forKind(kind)));
        return createNotiz(dossier, labelIDs, NotizTyp.DEFAULT);
    }

    @NonNull
    public Notiz createNotiz(@NonNull Dossier dossier, @NonNull Set<ID<Label>> labelIDs, @NonNull NotizTyp notizTyp) {
        var dossierId = dossier.getId();

        var createDto = BenutzerBuilder.createCreateDTO(
            builder -> builder
                .withRandomEmail("viktor.verantworltich@example.com")
                .withRandomVorname("Viktor")
                .withRandomNachname("Verantwortlich")
                .withKeycloakId(UUID.randomUUID().toString())
        );
        var benId = benutzerService.createBenutzer(createDto);
        var verantwortlicher = benutzerService.getById(benId);

        Set<Label> labels = labelIDs.stream()
            .map(labelService::getById)
            .collect(Collectors.toSet());

        Notiz notiz = NotizBuilder.create(
            builder -> builder
                .withID(ID.random(Notiz.class))
                .withTitel("Base Notiz")
                .withLabels(labels)
                .withNotizTyp(notizTyp)
                .withVerantwortlicher(verantwortlicher)
                .withDossier(dossierService.getById(dossierId)));

        return notizService.persist(notiz);
    }

    @Transactional(TxType.REQUIRES_NEW)
    public List<Notiz> findAllForKindWithLoadedFiles(@NonNull ID<Kind> kindID) {
        List<Notiz> notizen = notizService.findAllForKind(kindID);
        for (Notiz notiz : notizen) {
            notiz.getFiles().size();
        }
        return notizen;
    }

    public void createImpfausweisNotiz(@NonNull ID<Einladung> einladungID) {
        Einladung einladung = einladungService.getById(einladungID);
        einladung.disableOptimisticLockingCheck();
        Notiz notiz = createNotiz(einladung.getDossier(), Set.of(Label.ID_LABEL_IMPFAUSWEIS), NotizTyp.IMPFAUSWEIS);
        einladung.getNotizen().add(notiz);
    }

    public void createFragebogenNotiz(@NonNull ID<Einladung> einladungID) {
        Einladung einladung = einladungService.getById(einladungID);
        einladung.disableOptimisticLockingCheck();
        Notiz notiz = createNotiz(einladung.getDossier(), Set.of(Label.ID_LABEL_FRAGEBOGEN), NotizTyp.FRAGEBOGEN);
        einladung.getNotizen().add(notiz);
    }

    public void createAnmeldungBestaetigungNotiz(@NonNull ID<Einladung> einladungID) {
        Einladung einladung = einladungService.getById(einladungID);
        einladung.disableOptimisticLockingCheck();
        Notiz notiz = createNotiz(einladung.getDossier(), Set.of(Label.ID_LABEL_ANMELDUNG_BESTAETIGUNG), NotizTyp.ANMELDUNG_BESTAETIGUNG);
        einladung.getNotizen().add(notiz);
    }

    public void createAbmeldungsgrundNotiz(@NonNull ID<Einladung> einladungID) {
        Einladung einladung = einladungService.getById(einladungID);
        einladung.disableOptimisticLockingCheck();
        Notiz notiz = createNotiz(einladung.getDossier(), Set.of(Label.ID_LABEL_ABMELDUNGSGRUND), NotizTyp.ABMELDUNG);
        einladung.setAbmeldungsNotiz(notiz);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @NonNull
    @Transactional(TxType.REQUIRED)
    public Untersuchung getUntersuchungWithLoadedData(@NonNull ID<Einladung> einladungID) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungID, true);
        UntersuchungMassnahmen untersuchungMassnahmen = untersuchung.getUntersuchungMassnahmen();
        if (untersuchungMassnahmen != null) {
            if (untersuchungMassnahmen.getAufgabeInformationEltern() != null) {
                untersuchungMassnahmen.getAufgabeInformationEltern().getAufgabeStatus();
            }
            if (untersuchungMassnahmen.getAufgabeNachkontrolle() != null) {
                untersuchungMassnahmen.getAufgabeNachkontrolle().getAufgabeStatus();
            }
            if (untersuchungMassnahmen.getAufgabeUeberweisungHausarzt() != null) {
                untersuchungMassnahmen.getAufgabeUeberweisungHausarzt().getAufgabeStatus();
            }
            if (untersuchungMassnahmen.getAufgabeUeberweisungAugenarzt() != null) {
                untersuchungMassnahmen.getAufgabeUeberweisungAugenarzt().getAufgabeStatus();
            }
            untersuchungMassnahmen.getAbgegebenesInfomaterial().size();
        }
        UntersuchungImpfungen impfungen = untersuchung.getUntersuchungImpfungen();
        if (impfungen != null) {
            UntersuchungImpfungenMassnahmen impfMassnahmen = impfungen.getUntersuchungImpfungenMassnahmen();
            if (impfMassnahmen != null) {
                if (impfMassnahmen.getAufgabeInformationEltern() != null) {
                    impfMassnahmen.getAufgabeInformationEltern().getAufgabeStatus();
                }
                if (impfMassnahmen.getAufgabeInformationHausarzt() != null) {
                    impfMassnahmen.getAufgabeInformationHausarzt().getAufgabeStatus();
                }
                if (impfMassnahmen.getAufgabeRueckfrageHausarzt() != null) {
                    impfMassnahmen.getAufgabeRueckfrageHausarzt().getAufgabeStatus();
                }
            }
        }
        UntersuchungDurchfuehrung durchfuehrung = untersuchung.getUntersuchungDurchfuehrung();
        if (durchfuehrung != null) {
            if (durchfuehrung.getArzt() != null) {
                durchfuehrung.getArzt().getEmail();
            }
            if (durchfuehrung.getMpa() != null) {
                durchfuehrung.getMpa().getEmail();
            }
        }
        return untersuchung;
    }

    @NonNull
    public Untersuchung createUntersuchungWithEveryTab() {
        Einladung einladung = createEinladung();
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladung.getId());
        return createUntersuchungWithEveryTab(untersuchung);
    }

    @NonNull
    public Untersuchung createUntersuchungWithEveryTab(@NonNull Untersuchung untersuchung) {
        // KindDaten
        UntersuchungKindDaten untersuchungKindDaten =
            untersuchungKindDatenService.persist(UntersuchungKindDaten.ofKind(untersuchung.getEinladung()
                .getDossier()
                .getKind()));
        UntersuchungDurchfuehrung untersuchungDurchfuehrung =
            untersuchungDurchfuehrungService.persist(UntersuchungDurchfuehrung.forUntersuchung(untersuchung));
        // Impfungen
        Benutzer benutzer = createBenutzer();
        UntersuchungImpfungen existingImpfungen = untersuchungImpfungenService.persist(
            UntersuchungImpfungenBuilder.create(builder -> builder
                .forUntersuchung(untersuchung)
                .withBemerkungen("Bemerkungen zu den Impfungen")
                .withImpfausweisUnvollstaendig(true)
                .withVisum1(benutzer, appClock.offsetDateTime().now().toLocalDate())));
        // Sehen
        UntersuchungSehenBeurteilung existingSehen = untersuchungSehenBeurteilungService.persist(
            UntersuchungSehenBeurteilungBuilder.create(builder -> builder
                .forUntersuchung(untersuchung)
                .withBemerkung("Untersuchung Sehen Bemerkung")));
        // Hoeren - Audiogramm
        UntersuchungHoerenAudiogramm existingHoerenAudiogramm = untersuchungHoerenAudiogrammService.persist(
            UntersuchungHoerenAudiogrammBuilder.create(builder -> builder
                .forUntersuchung(untersuchung)
                .withBemerkung("Untersuchung Hoeren Audiogramm Bemerkung")));
        // Hoeren - Otoskopie
        UntersuchungHoerenOtoskopie existingHoerenOtoskopie = untersuchungHoerenOtoskopieService.persist(
            UntersuchungHoerenOtoskopieBuilder.create(builder -> builder
                .forUntersuchung(untersuchung)
                .withBemerkung("Untersuchung Hoeren Otoskopie Bemerkung"))
        );
        // Groesse & Gewicht
        UntersuchungGroesseGewicht existingGroesseGewicht = untersuchungGroesseGewichtService.persist(
            UntersuchungGroesseGewichtBuilder.create(builder -> builder
                .forUntersuchung(untersuchung)
                .withGroesse(180)
                .withGewicht(BigDecimal.valueOf(90))
                .withBemerkung("Untersuchung GroesseGewicht Bemerkung")));
        // Triage
        UntersuchungTriage existingTriage = untersuchungTriageService.persist(
            UntersuchungTriageBuilder.create(builder -> builder
                .forUntersuchung(untersuchung)
                .withTriageSu(true, "Triage Bemerkungen SU")));
        // Aertzliche Untersuchung
        UntersuchungArztBeurteilung existingArzt = untersuchungArztBeurteilungService.persist(
            UntersuchungArztBeurteilungBuilder.create(builder -> builder
                .forUntersuchung(untersuchung)
                .withBlutdruck(Blutdruck.AUFFAELLIG)
                .withBewegungsapparat(List.of(Bewegungsapparat.SKOLIOSE, Bewegungsapparat.FUSSFEHLSTELLUNG))
                .withEinbeinhuepfen(Einbeinhuepfen.SICHER)
                .withEinbeinstand(Einbeinstand.NICHT_MOEGLICH)
                .withMotorik(Motorik.NORMAL)));
        untersuchung.setUntersuchungArztBeurteilung(existingArzt);
        // Massnahmen
        UntersuchungMassnahmen existingMassnahmen = untersuchungMassnahmenService.persist(
            UntersuchungMassnahmenBuilder.create(builder -> builder
                .forUntersuchung(untersuchung)
                .withBemerkung("Untersuchung Massnahmen Bemerkung")));

        untersuchung.setUntersuchungKindDaten(untersuchungKindDaten);
        untersuchung.setUntersuchungDurchfuehrung(untersuchungDurchfuehrung);
        untersuchung.setUntersuchungImpfungen(existingImpfungen);
        untersuchung.setUntersuchungSehenBeurteilung(existingSehen);
        untersuchung.setUntersuchungHoerenAudiogramm(existingHoerenAudiogramm);
        untersuchung.setUntersuchungHoerenOtoskopie(existingHoerenOtoskopie);
        untersuchung.setUntersuchungGroesseGewicht(existingGroesseGewicht);
        untersuchung.setUntersuchungTriage(existingTriage);
        untersuchung.setUntersuchungMassnahmen(existingMassnahmen);

        untersuchungRepo.persist(untersuchung);
        return untersuchung;
    }

    @NonNull
    public Untersuchung createUntersuchungWithDurchfuehrung() {
        Einladung einladung = createEinladung();
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladung.getId());
        return createUntersuchungWithDurchfuehrung(untersuchung);
    }

    @NonNull
    public Untersuchung createUntersuchungWithDurchfuehrung(@NonNull Untersuchung untersuchung) {
        return createUntersuchungWithDurchfuehrung(untersuchung, null);
    }

    @NonNull
    public Untersuchung createUntersuchungWithDurchfuehrung(
        @NonNull Untersuchung untersuchung,
        @Nullable Consumer<UntersuchungDurchfuehrungBuilder> buildBlock
    ) {

        Untersuchung u = untersuchungRepo.getById(untersuchung.getId());

        Benutzer arztMpa = createBenutzer();
        UntersuchungDurchfuehrung existingDurchfuehrung;
        if (buildBlock == null) {
            existingDurchfuehrung =
                UntersuchungDurchfuehrungBuilder.create(builder -> builder
                    .forUntersuchung(u)
                    .withArzt(arztMpa)
                    .withMpa(arztMpa));
        } else {
            existingDurchfuehrung = UntersuchungDurchfuehrungBuilder.create(buildBlock);
        }
        existingDurchfuehrung = untersuchungDurchfuehrungService.persist(existingDurchfuehrung);

        u.setUntersuchungDurchfuehrung(existingDurchfuehrung);
        untersuchungRepo.persist(u);
        return u;
    }

    @NonNull
    public Untersuchung createUntersuchungWithTriage() {
        Einladung einladung = createEinladung();
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladung.getId());
        return createUntersuchungWithTriage(untersuchung);
    }

    @NonNull
    public Untersuchung createUntersuchungWithTriage(@NonNull Untersuchung untersuchung) {
        UntersuchungTriage existingTriage = untersuchungTriageService.persist(
            UntersuchungTriageBuilder.create(builder -> builder
                .forUntersuchung(untersuchung)
                .withTriageSu(true, "Triage Bemerkungen SU")));
        untersuchung.setUntersuchungTriage(existingTriage);
        untersuchungRepo.persist(untersuchung);
        return untersuchung;
    }

    @NonNull
    public Untersuchung createUntersuchungWithLaeusekontrolle() {
        Einladung einladung = createEinladung();
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladung.getId());
        return createUntersuchungWithLaeusekontrolle(untersuchung);
    }

    @NonNull
    public Untersuchung createUntersuchungWithLaeusekontrolle(@NonNull Untersuchung untersuchung) {
        UntersuchungLaeusekontrolle laeusekontrolle = untersuchungLaeusekontrolleService.persist(
            UntersuchungLaeusekontrolleBuilder.create(builder -> builder
                .forUntersuchung(untersuchung)
                .withBeurteilung(List.of(LaeusekontrolleBeurteilung.LAEUSE_VORHANDEN))
                .withBemerkung("Läuse Bemerkungen SU")));
        untersuchung.setUntersuchungLaeusekontrolle(laeusekontrolle);
        untersuchungRepo.persist(untersuchung);
        return untersuchung;
    }

    @NonNull
    public Untersuchung createUntersuchungWithSehen() {
        Einladung einladung = createEinladung();
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladung.getId());
        return createUntersuchungWithSehen(untersuchung);
    }

    @NonNull
    public Untersuchung createUntersuchungWithSehen(@NonNull Untersuchung untersuchung) {
        UntersuchungSehenBeurteilung existingSehen = untersuchungSehenBeurteilungService.persist(
            UntersuchungSehenBeurteilungBuilder.create(builder -> builder
                .forUntersuchung(untersuchung)
                .withBrilleLinsen(BrilleLinsenResultat.OHNE_KEINE)
                .withStereotest(StereotestResultat.NORMAL)
                .withFarbsehen(FarbsehenResultat.NORMAL)
                .withInspektion(InspektionKonvergenzResultat.UNAUFFAELLIG)
                .withKonvergenz(InspektionKonvergenzResultat.UNAUFFAELLIG)
                .withMotilitaet(MotilitaetResultat.NORMAL)
                .withStrabismus(List.of(StrabismusResultat.KEIN_STRABISMUS))
                .withStrabismusAuftreten(StrabismusAuftretenResultat.KEIN_STRABISMUS)
                .withStrabismusAuspraegung(StrabismusAuspraegungResultat.KEIN_STRABISMUS)
                .withBemerkung("Untersuchung Sehen Bemerkung")));
        untersuchung.setUntersuchungSehenBeurteilung(existingSehen);
        UntersuchungSehenBemerkungFragebogen existingSehenFragebogen =
            untersuchungSehenBemerkungFragebogenService.persist(
                UntersuchungSehenBemerkungFragebogenBuilder.create(builder -> builder
                    .forUntersuchung(untersuchung)
                    .withBemerkungFragebogen("Untersuchung Sehen Fragebogen Bemerkung")));
        untersuchung.setUntersuchungSehenBemerkungFragebogen(existingSehenFragebogen);
        untersuchungRepo.persist(untersuchung);
        return untersuchung;
    }

    @NonNull
    public Untersuchung createUntersuchungWithSehenAndMassnahmen() {
        Einladung einladung = createEinladung();
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladung.getId());
        return createUntersuchungWithSehenAndMassnahme(untersuchung);
    }

    @NonNull
    public Untersuchung createUntersuchungWithSehenAndMassnahme(@NonNull Untersuchung untersuchung) {
        UntersuchungSehenBeurteilung existingSehen = untersuchungSehenBeurteilungService.persist(
            UntersuchungSehenBeurteilungBuilder.create(builder -> builder
                .forUntersuchung(untersuchung)
                .withBemerkung("Untersuchung Sehen Bemerkung")));
        untersuchung.setUntersuchungSehenBeurteilung(existingSehen);

        UntersuchungMassnahmen existingMassnahme = untersuchungMassnahmenService.persist(
            UntersuchungMassnahmenBuilder.create(builder -> builder.forUntersuchung(untersuchung)
                .withKeineMassnahmen(false).withBemerkung("Untersuchung Massnahmen Bemerkungen")));
        untersuchung.setUntersuchungMassnahmen(existingMassnahme);
        untersuchungRepo.persist(untersuchung);
        return untersuchung;
    }

    @NonNull
    public Untersuchung createUntersuchungWithHoerenAudiogramm() {
        Einladung einladung = createEinladung();
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladung.getId());
        return createUntersuchungWithHoerenAudiogramm(untersuchung);
    }

    @NonNull
    public Untersuchung createUntersuchungWithHoerenAudiogramm(@NonNull Untersuchung untersuchung) {
        UntersuchungHoerenAudiogramm existingHoeren = untersuchungHoerenAudiogrammService.persist(
            UntersuchungHoerenAudiogrammBuilder.create(builder -> builder
                .forUntersuchung(untersuchung)
                .withAudiogrammLinks(Dezibel.DB20)
                .withAudiogrammRechts(Dezibel.DB20)
                .withAudiogrammBeurteilung(List.of(AudiogrammBeurteilung.NORMAL))
                .withZusaetzlicheAngaben(HoerenZusaetzlicheAngaben.KEINE)
                .withBemerkung("Untersuchung Hoeren Audiogramm Bemerkung")));
        untersuchung.setUntersuchungHoerenAudiogramm(existingHoeren);
        untersuchungRepo.persist(untersuchung);
        return untersuchung;
    }

    @NonNull
    public Untersuchung createUntersuchungWithHoerenOtoskopie() {
        Einladung einladung = createEinladung();
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladung.getId());
        return createUntersuchungWithHoerenOtoskopie(untersuchung);
    }

    @NonNull
    public Untersuchung createUntersuchungWithHoerenOtoskopie(@NonNull Untersuchung untersuchung) {
        UntersuchungHoerenOtoskopie existingHoerenOtoskopie = untersuchungHoerenOtoskopieService.persist(
            UntersuchungHoerenOtoskopieBuilder.create(builder -> builder
                .forUntersuchung(untersuchung)
                .withOtoskopieLinks(List.of(OtoskopieResultat.NICHT_EINSEHBAR))
                .withOtoskopieRechts(List.of(OtoskopieResultat.NORMAL))
                .withBemerkung("Untersuchung Hoeren Otoskopie Bemerkung")));
        untersuchung.setUntersuchungHoerenOtoskopie(existingHoerenOtoskopie);
        untersuchungRepo.persist(untersuchung);
        return untersuchung;
    }

    @NonNull
    public Untersuchung createUntersuchungWithGroesseGewicht() {
        Einladung einladung = createEinladung();
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladung.getId());
        return createUntersuchungWithGroesseGewicht(untersuchung);
    }

    @NonNull
    public Untersuchung createUntersuchungWithGroesseGewicht(@NonNull Untersuchung untersuchung) {
        UntersuchungGroesseGewicht existingGroesseGewicht = untersuchungGroesseGewichtService.persist(
            UntersuchungGroesseGewichtBuilder.create(builder -> builder
                .forUntersuchung(untersuchung)
                .withGroesse(180)
                .withGewicht(BigDecimal.valueOf(90))
                .withGroessePerzentile(PerzentileGroesse.NORMAL)
                .withGewichtPerzentile(PerzentileGewicht.NORMAL)
                .withBmiPerzentile(PerzentileBmi.NORMAL)
                .withBemerkung("Untersuchung GroesseGewicht Bemerkung")));
        untersuchung.setUntersuchungGroesseGewicht(existingGroesseGewicht);
        return untersuchung;
    }

    @NonNull
    public Untersuchung createUntersuchungWithArztuntersuchung() {
        Einladung einladung = createEinladung();
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladung.getId());
        return createUntersuchungWithArztuntersuchung(untersuchung);
    }

    @NonNull
    public Untersuchung createUntersuchungWithArztuntersuchung(@NonNull Untersuchung untersuchung) {
        UntersuchungArztBeurteilung existingArzt = untersuchungArztBeurteilungService.persist(
            UntersuchungArztBeurteilungBuilder.create(builder -> builder
                .forUntersuchung(untersuchung)
                .withBlutdruck(Blutdruck.AUFFAELLIG)
                .withBewegungsapparat(List.of(Bewegungsapparat.SKOLIOSE, Bewegungsapparat.FUSSFEHLSTELLUNG))
                .withEinbeinhuepfen(Einbeinhuepfen.SICHER)
                .withEinbeinstand(Einbeinstand.NICHT_MOEGLICH)
                .withMotorik(Motorik.NORMAL)));
        untersuchung.setUntersuchungArztBeurteilung(existingArzt);
        untersuchungRepo.persist(untersuchung);
        return untersuchung;
    }

    @NonNull
    public Untersuchung createUntersuchungWithArztuntersuchungBemerkung() {
        Einladung einladung = createEinladung();
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladung.getId());
        return createUntersuchungWithArztuntersuchungBemerkung(untersuchung);
    }

    @NonNull
    public Untersuchung createUntersuchungWithArztuntersuchungBemerkung(@NonNull Untersuchung untersuchung) {
        UntersuchungArztBemerkung existingArzt = untersuchungArztBemerkungService.persist(
            UntersuchungArztBemerkungBuilder.create(builder -> builder
                .forUntersuchung(untersuchung)
                .withBemerkungen("Untersuchung Arzt Bemerkung")));
        untersuchung.setUntersuchungArztBemerkung(existingArzt);
        untersuchungRepo.persist(untersuchung);
        return untersuchung;
    }

    @NonNull
    public Untersuchung createUntersuchungWithMassnahmen() {
        Einladung einladung = createEinladung();
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladung.getId());
        return createUntersuchungWithMassnahmen(untersuchung);
    }

    @NonNull
    public Untersuchung createUntersuchungWithMassnahmen(@NonNull Untersuchung untersuchung) {
        UntersuchungMassnahmen existingMassnahmen = untersuchungMassnahmenService.persist(
            UntersuchungMassnahmenBuilder.create(builder -> builder
                .forUntersuchung(untersuchung)
                .withBemerkung("Untersuchung Massnahmen Bemerkung")));
        untersuchung.setUntersuchungMassnahmen(existingMassnahmen);
        untersuchungRepo.persist(untersuchung);
        return existingMassnahmen.getUntersuchung();
    }

    @NonNull
    public Untersuchung createUntersuchungWithMassnahmen(@NonNull LocalDate anlassDatum, @NonNull Dossier dossier) {
        final Anlass anlass = createAnlass(anlassDatum);
        Einladung einladung = createEinladung(anlass, dossier);
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladung.getId());
        return createUntersuchungWithMassnahmen(untersuchung);
    }

    @NonNull
    public Untersuchung createUntersuchungWithImpfungen() {
        Einladung einladung = createEinladung();
        return createUntersuchungWithImpfungen(einladung);
    }

    @NonNull
    public Untersuchung createUntersuchungWithImpfungen(@NonNull Einladung einladung) {
        einladung.disableOptimisticLockingCheck();
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladung.getId());
        return createUntersuchungWithImpfungen(untersuchung);
    }

    @NonNull
    public Untersuchung createUntersuchungWithImpfungen(@NonNull Untersuchung untersuchung) {
        Benutzer benutzer = createBenutzer();
        UntersuchungImpfungen existingImpfungen = untersuchungImpfungenService.persist(
            UntersuchungImpfungenBuilder.create(builder -> builder
                .forUntersuchung(untersuchung)
                .withBemerkungen("Bemerkungen zu den Impfungen")
                .withImpfausweisUnvollstaendig(true)
                .withVisum1(benutzer, appClock.offsetDateTime().now().toLocalDate())));
        existingImpfungen.disableOptimisticLockingCheck();
        untersuchung.setUntersuchungImpfungen(existingImpfungen);
        untersuchungRepo.persist(untersuchung);
        return untersuchung;
    }

    @NonNull
    @Transactional(TxType.REQUIRED)
    public Untersuchung createUntersuchungWithImpfungenMassnahmen() {
        Untersuchung untersuchung = createUntersuchungWithImpfungen();
        return createUntersuchungWithImpfungenMassnahmen(untersuchung);
    }

    @NonNull
    @Transactional(TxType.REQUIRED)
    public Untersuchung createUntersuchungWithImpfungenMassnahmen(@NonNull Untersuchung untersuchung) {
        final UntersuchungImpfungen impfungen = untersuchung.getUntersuchungImpfungen();
        Objects.requireNonNull(impfungen);
        UntersuchungImpfungenMassnahmen massnahmen = untersuchungImpfungenMassnahmenService.persist(
            UntersuchungImpfungenMassnahmenBuilder.create(builder -> builder
                .forUntersuchungImpfungen(impfungen)
                .withInformationEltern(InformationEltern.BRIEF)
                .withInformationHausarzt(true)
                .withRueckfrageHausarzt(true)
                .withVormerkenFuerGruppenFolgetermin(false)
                .withBemerkungen("Meine Impfungs-Massnahmen"))
        );
        impfungen.setUntersuchungImpfungenMassnahmen(massnahmen);
        untersuchungImpfungenService.persist(impfungen);
        return impfungen.getUntersuchung();
    }

    @NonNull
    public UntersuchungImpfungIntern createUntersuchungImpfungIntern(
        @NonNull UntersuchungImpfungen untersuchungImpfungen) {
        Impfstoff impfstoff = createImpfstoff();
        return untersuchungImpfungInternService
            .persist(UntersuchungImpfungInternBuilder.create(builder -> builder
                .forUntersuchungImpfungen(untersuchungImpfungen)
                .withImmunitaetForKrankheit(Krankheit.HEPATITIS_B, true)
                .withUserErfasst(createBenutzer())
                .withImpfstoff(impfstoff)));
    }

    @NonNull
    public UntersuchungImpfstatusKrankheit createUntersuchungImpfstatusKrankheit(
        @NonNull UntersuchungImpfungen untersuchungImpfungen) {
        return untersuchungImpfstatusKrankheitService
            .persist(UntersuchungImpfstatusKrankheitBuilder.create(builder -> builder
                .forUntersuchungImpfungen(untersuchungImpfungen)
                .forKrankheit(Krankheit.HEPATITIS_B)));
    }

    @NonNull
    public Untersuchung createUntersuchungWithCompleteData() {
        final Anlass anlass = createAnlass();
        final Dossier dossier = createDossier();
        return createUntersuchungWithCompleteData(anlass, dossier);
    }

    @NonNull
    public Untersuchung createUntersuchungWithCompleteData(@NonNull Anlass anlass, @NonNull Dossier dossier) {
        Einladung einladung = createEinladung(anlass, dossier);
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladung.getId());
        untersuchung = createUntersuchungWithDurchfuehrung(untersuchung);
        untersuchung = createUntersuchungWithTriage(untersuchung);
        untersuchung = createUntersuchungWithSehen(untersuchung);
        untersuchung = createUntersuchungWithHoerenAudiogramm(untersuchung);
        untersuchung = createUntersuchungWithHoerenOtoskopie(untersuchung);
        untersuchung = createUntersuchungWithImpfungen(untersuchung);
        untersuchung = createUntersuchungWithImpfungenMassnahmen(untersuchung);
        untersuchung = createUntersuchungWithMassnahmen(untersuchung);
        untersuchung = createUntersuchungWithGroesseGewicht(untersuchung);
        untersuchung = createUntersuchungWithArztuntersuchung(untersuchung);
        untersuchung = createUntersuchungWithLaeusekontrolle(untersuchung);
        anlassService.changeAnlassStatus(einladung.getAnlass().getId(), AnlassStatus.ABGESCHLOSSEN);
        return untersuchung;
    }

    @NonNull
    public Untersuchung createUntersuchungWithCompleteDataInNewTransaction(
        @NonNull Anlass anlass,
        @NonNull Dossier dossier) {
        Einladung einladung = createEinladungInNewTransaction(anlass, dossier);
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladung.getId());
        untersuchung = createUntersuchungWithDurchfuehrung(untersuchung);
        untersuchung = createUntersuchungWithTriage(untersuchung);
        untersuchung = createUntersuchungWithSehen(untersuchung);
        untersuchung = createUntersuchungWithHoerenAudiogramm(untersuchung);
        untersuchung = createUntersuchungWithImpfungen(untersuchung);
        untersuchung = createUntersuchungWithImpfungenMassnahmen(untersuchung);
        untersuchung = createUntersuchungWithMassnahmen(untersuchung);
        untersuchung = createUntersuchungWithGroesseGewicht(untersuchung);
        untersuchung = createUntersuchungWithArztuntersuchung(untersuchung);
        anlassService.changeAnlassStatus(einladung.getAnlass().getId(), AnlassStatus.ABGESCHLOSSEN);
        return untersuchung;
    }

    @NonNull
    public Untersuchung createUntersuchungWithKindDaten() {
        Einladung einladung = createEinladung();
        return createUntersuchungWithKindDaten(einladung);
    }

    @NonNull
    public Untersuchung createUntersuchungWithKindDaten(@NonNull Einladung einladung) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladung.getId());
        return createUntersuchungWithKindDaten(untersuchung);
    }

    @NonNull
    public Untersuchung createUntersuchungWithKindDaten(@NonNull Untersuchung untersuchung) {
        UntersuchungKindDaten untersuchungKindDaten =
            untersuchungKindDatenService.persist(UntersuchungKindDaten.ofKind(untersuchung.getEinladung().getDossier()
                .getKind()));
        untersuchung.setUntersuchungKindDaten(untersuchungKindDaten);
        untersuchungRepo.persist(untersuchung);
        return untersuchung;
    }

    public UntersuchungImpfungIntern createInterneImpfungForUntersuchung(
        @NonNull UntersuchungImpfungen untersuchungImpfungen
    ) {
        Impfstoff impfstoff = createImpfstoff();
        return untersuchungImpfungInternService
            .persist(UntersuchungImpfungInternBuilder.create(builder -> builder
                .forUntersuchungImpfungen(untersuchungImpfungen)
                .withUserErfasst(createBenutzer())
                .withImpfstoff(impfstoff)));
    }

    @NonNull
    public Impfstoff createImpfstoff() {
        return impfstoffService.persist(ImpfstoffBuilder.defaultImpfstoff());
    }

    @NonNull
    public Impfstoff createImpfstoff(Krankheit... krankheiten) {
        Impfstoff impfstoff = ImpfstoffBuilder.create(i -> i.forKrankheiten(krankheiten));
        return impfstoffService.persist(impfstoff);
    }

    @NonNull
    public ImpfstoffCharge createImpfstoffCharge() {
        return impfstoffChargeService.persist(ImpfstoffChargeBuilder.defaultImpfstoffCharge());
    }

    @NonNull
    public ImpfstoffCharge createImpfstoffCharge(@NonNull Impfstoff impfstoff, @NonNull String chargeName) {
        var charge = ImpfstoffChargeBuilder.create(i -> i.forImpfstoff(impfstoff)
            .withName(TestUtil.getRandomString(chargeName)));
        return impfstoffChargeService.persist(charge);
    }

    @NonNull
    @Transactional(TxType.REQUIRED)
    public Kind changeNameOfKind(@NonNull ID<Kind> kindID, @NonNull String newName) {
        Kind kind = kindService.getById(kindID);
        kind.disableOptimisticLockingCheck();
        kind.setName(newName);
        return kindService.persistWithoutDuplicateCheck(kind);
    }

    @NonNull
    @Transactional(TxType.REQUIRED)
    public Aufgabe changeAufgabeEmpfaenger(@NonNull ID<Aufgabe> aufgabeID, @NonNull Benutzer empfaenger) {
        Aufgabe aufgabe = aufgabeService.getById(aufgabeID);
        aufgabe.setEmpfaenger(empfaenger);
        aufgabe.disableOptimisticLockingCheck();
        return aufgabeService.persist(aufgabe);
    }

    @NonNull
    @Transactional(TxType.REQUIRED)
    public Kind changeEmailOfKind(@NonNull ID<Kind> kindID, @NonNull EmailAddress emailAddress) {
        Kind kind = kindService.getById(kindID);
        kind.setEmail(emailAddress);
        kind.disableOptimisticLockingCheck();
        return kindService.persistWithoutDuplicateCheck(kind);
    }

    @NonNull
    @Transactional(TxType.REQUIRED)
    @CanIgnoreReturnValue
    public Schule setTimestampFirstImportOfSchule(@NonNull ID<Schule> schuleID, @Nullable OffsetDateTime timestamp) {
        Schule schule = schuleService.getById(schuleID);
        Objects.requireNonNull(schule.getExternalImportInfo()).setTimestampFirstImport(timestamp);
        schule.disableOptimisticLockingCheck();
        return schuleService.persist(schule);
    }

    @NonNull
    @Transactional(TxType.REQUIRED)
    @CanIgnoreReturnValue
    public Schule setSchuleAktivStatus(@NonNull ID<Schule> schuleID, @NonNull EntityAktivStatus aktivStatus) {
        Schule schule = schuleService.getById(schuleID);
        schule.setSchuleAktivStatus(aktivStatus);
        schule.disableOptimisticLockingCheck();
        return schuleService.persist(schule);
    }

    @NonNull
    public Aufgabe createAufgabe() {
        return createAufgabe(null);
    }

    @NonNull
    public Aufgabe createAufgabe(@Nullable Kind kind) {
        // Create a Aufgabe
        Benutzer currentBenutzer = benutzerService.getCurrentBenutzerOrThrowException();
        Benutzer benutzer = createBenutzer();
        Aufgabe aufgabe = AufgabeBuilder.create(
            builder -> builder
                .withID(ID.random(Aufgabe.class))
                .withKind(kind)
                .withTitle("Basis Aufgabe")
                .withFaelligDatum(LocalDate.of(2025, 1, 1))
                .withUserErfasst(currentBenutzer)
                .withEmpfaenger(benutzer));
        return aufgabeService.persist(aufgabe);
    }

    @NonNull
    @Transactional(TxType.REQUIRES_NEW)
    public Set<Schule> findSchulenOfKontaktPerson(@NonNull ID<KontaktPersonSchule> id) {
        Set<Schule> schulen = kontaktPersonSchuleService.getById(id).getSchulen();
        schulen.size(); // to prevent lazy loading exception
        return schulen;
    }

    @NonNull
    @Transactional(TxType.REQUIRES_NEW)
    public Set<Klasse> findKlassenOfLehrperson(@NonNull ID<Lehrperson> id) {
        Set<Klasse> klassen = lehrpersonService.getById(id).getKlassen();
        klassen.size(); // to prevent lazy loading exception
        return klassen;
    }

    @NonNull
    @Transactional(TxType.REQUIRES_NEW)
    public Set<Kind> findKinderOfElternperson(@NonNull ID<KontaktPersonKind> id) {
        var elternperson = kontaktPersonKindService.getById(id);
        Set<Kind> kinder = elternperson.getKinder();
        kinder.size(); // to prevent lazy loading exception
        return kinder;
    }

    @NotNull
    public Nachkontrolle createNachkontrolle(@NonNull Dossier dossier, boolean nachKontrolleNeeded) {
        return nachkontrolleService.persist(NachkontrolleBuilder.create(builder ->
            builder.forDossier(dossier)
                .withNachkontrolleSehen(nachKontrolleNeeded)
        ));
    }

    @NonNull
    @Transactional(TxType.REQUIRES_NEW)
    public Briefvorlage createBriefvorlage(@NonNull BriefvorlageType type) {
        File briefvorlageFile = TestUtil.loadBriefvorlageFile(type);
        FileUpload fileUpload = FileUploadWrapper.fromFile(briefvorlageFile);
        FileData fileData = briefvorlageService.saveBriefvorlageAttachmentInFilestorage(fileUpload);
        Briefvorlage aufgabe = BriefvorlageBuilder.create(
            builder -> builder
                .withId(ID.random(Briefvorlage.class))
                .withName(TestUtil.getRandomString("Vorlage"))
                .withFile(fileData)
                .withType(type));
        return briefvorlageService.persist(aufgabe);
    }

    @NonNull
    @Transactional(TxType.REQUIRES_NEW)
    public Benutzer readBenutzerWithBerechtigungen(@NonNull ID<Benutzer> benutzerID) {
        Benutzer systemAdmin = benutzerService.getById(benutzerID);
        systemAdmin.getBerechtigungen().size();
        return systemAdmin;
    }
}
