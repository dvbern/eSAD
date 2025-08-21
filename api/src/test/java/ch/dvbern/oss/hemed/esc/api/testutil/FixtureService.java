package ch.dvbern.oss.hemed.esc.api.testutil;

import java.io.File;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
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
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassConfigRepo;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungDurchfuehrung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungDurchfuehrungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungImpfstatusKrankheit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungImpfstatusKrankheitService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungImpfungInternService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungImpfungenService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungKindDaten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungKindDatenService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungRepo;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungSehen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungSehenService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungTriage;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungTriageService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArzt;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Bewegungsapparat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Blutdruck;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinhuepfen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinstand;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Motorik;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.PerzentileBmi;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.PerzentileGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.PerzentileGroesse;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.UntersuchungGroesseGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.UntersuchungGroesseGewichtService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.AudiogrammBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.Dezibel;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.HoerenZusaetzlicheAngaben;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.OtoskopieResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogrammService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenOtoskopie;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenOtoskopieService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.InformationEltern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmenService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmenService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.ArtAbmeldung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.BrilleLinsenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.EinladungStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.FarbsehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.GrundAbmeldung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.InspektionKonvergenzResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.MotilitaetResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.StereotestResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.StrabismusAuftretenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.StrabismusAuspraegungResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.StrabismusResultat;
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
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassConfigBuilder;
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
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungArztBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungDurchfuehrungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungGroesseGewichtBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungHoerenAudiogrammBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungHoerenOtoskopieBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfstatusKrankheitBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungInternBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungenBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungenMassnahmenBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungMassnahmenBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungSehenBuilder;
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
    private final UntersuchungImpfungenService untersuchungImpfungenService;
    private final ImpfstoffService impfstoffService;
    private final ImpfstoffChargeService impfstoffChargeService;
    private final SchuleService schuleService;
    private final KlasseService klasseService;
    private final AufgabeService aufgabeService;
    private final LehrpersonService lehrpersonService;
    private final LabelService labelService;
    private final AppClock appClock;
    private final UntersuchungSehenService untersuchungSehenService;
    private final UntersuchungHoerenAudiogrammService untersuchungHoerenAudiogrammService;
    private final UntersuchungHoerenOtoskopieService untersuchungHoerenOtoskopieService;
    private final UntersuchungRepo untersuchungRepo;
    private final UntersuchungImpfungInternService untersuchungImpfungInternService;
    private final UntersuchungImpfstatusKrankheitService untersuchungImpfstatusKrankheitService;
    private final UntersuchungMassnahmenService untersuchungMassnahmenService;
    private final UntersuchungGroesseGewichtService untersuchungGroesseGewichtService;
    private final UntersuchungArztService untersuchungArztService;
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
    public Anlass persistAnlass(@NonNull Anlass anlass) {
        anlassConfigRepo.persist(anlass.getAnlassConfig());
        return anlassService.persist(anlass);
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
        AnlassConfig config = createAnlassConfigAllSameRequirementSetting(requirementSetting);
        return anlassService.persist(
            AnlassBuilder.create(builder -> builder
                .withTitle("Test getAnlass")
                .withAnlassStatus(AnlassStatus.IN_PLANUNG)
                .withDatumSetSchuljahr(LocalDate.of(2023, 5, 5))
                .withAnlassConfig(config))
        );
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
        AnlassConfig configAllOptional = AnlassConfigBuilder.create(builder -> builder
            .withID(ID.random(AnlassConfig.class))
            .withGueltigkeitsZeitraum(
                LocalDate.of(configYear, 1, 1),
                LocalDate.of(configYear, 12, 31))
            .withAnlassTyp(anlassTyp));
        anlassConfigRepo.persist(configAllOptional);
        return configAllOptional;
    }

    @NonNull
    public AnlassConfig createAnlassConfigAllSameRequirementSetting(@NonNull RequirementSetting requirementSetting) {
        AnlassConfig configAllSame = AnlassConfigBuilder.allSameRequirementSetting(requirementSetting);
        anlassConfigRepo.persist(configAllSame);
        return configAllSame;
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
        return createNotiz(dossier, labelIDs);
    }

    @NonNull
    public Notiz createNotiz(@NonNull Dossier dossier, @NonNull Set<ID<Label>> labelIDs) {
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
        Notiz notiz = createNotiz(einladung.getDossier(), Set.of(Label.ID_LABEL_IMPFAUSWEIS));
        einladung.setImpfausweis(notiz);
    }

    public void createFragebogenNotiz(@NonNull ID<Einladung> einladungID) {
        Einladung einladung = einladungService.getById(einladungID);
        Notiz notiz = createNotiz(einladung.getDossier(), Set.of(Label.ID_LABEL_FRAGEBOGEN));
        einladung.setFragebogen(notiz);
    }

    public void createAbmeldungsgrundNotiz(@NonNull ID<Einladung> einladungID) {
        Einladung einladung = einladungService.getById(einladungID);
        Notiz notiz = createNotiz(einladung.getDossier(), Set.of(Label.ID_LABEL_ABMELDUNGSGRUND));
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
        UntersuchungSehen existingSehen = untersuchungSehenService.persist(
            UntersuchungSehenBuilder.create(builder -> builder
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
                .withGewicht(90)
                .withBemerkung("Untersuchung GroesseGewicht Bemerkung")));
        // Triage
        UntersuchungTriage existingTriage = untersuchungTriageService.persist(
            UntersuchungTriageBuilder.create(builder -> builder
                .forUntersuchung(untersuchung)
                .withTriageSu(true, "Triage Bemerkungen SU")));
        // Aertzliche Untersuchung
        UntersuchungArzt existingArzt = untersuchungArztService.persist(
            UntersuchungArztBuilder.create(builder -> builder
                .forUntersuchung(untersuchung)
                .withBemerkungen("Untersuchung Arzt Bemerkung")
                .withBlutdruck(Blutdruck.AUFFAELLIG)
                .withBewegungsapparat(List.of(Bewegungsapparat.SKOLIOSE, Bewegungsapparat.FUSSFEHLSTELLUNG))
                .withEinbeinhuepfen(Einbeinhuepfen.SICHER)
                .withEinbeinstand(Einbeinstand.NICHT_MOEGLICH)
                .withMotorik(Motorik.NORMAL)));
        untersuchung.setUntersuchungArzt(existingArzt);
        // Massnahmen
        UntersuchungMassnahmen existingMassnahmen = untersuchungMassnahmenService.persist(
            UntersuchungMassnahmenBuilder.create(builder -> builder
                .forUntersuchung(untersuchung)
                .withBemerkung("Untersuchung Massnahmen Bemerkung")));

        untersuchung.setUntersuchungKindDaten(untersuchungKindDaten);
        untersuchung.setUntersuchungDurchfuehrung(untersuchungDurchfuehrung);
        untersuchung.setUntersuchungImpfungen(existingImpfungen);
        untersuchung.setUntersuchungSehen(existingSehen);
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
    public Untersuchung createUntersuchungWithSehen() {
        Einladung einladung = createEinladung();
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladung.getId());
        return createUntersuchungWithSehen(untersuchung);
    }

    @NonNull
    public Untersuchung createUntersuchungWithSehen(@NonNull Untersuchung untersuchung) {
        UntersuchungSehen existingSehen = untersuchungSehenService.persist(
            UntersuchungSehenBuilder.create(builder -> builder
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
        untersuchung.setUntersuchungSehen(existingSehen);
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
        UntersuchungSehen existingSehen = untersuchungSehenService.persist(
            UntersuchungSehenBuilder.create(builder -> builder
                .forUntersuchung(untersuchung)
                .withBemerkung("Untersuchung Sehen Bemerkung")));
        untersuchung.setUntersuchungSehen(existingSehen);

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
                .withGewicht(90)
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
        UntersuchungArzt existingArzt = untersuchungArztService.persist(
            UntersuchungArztBuilder.create(builder -> builder
                .forUntersuchung(untersuchung)
                .withBemerkungen("Untersuchung Arzt Bemerkung")
                .withBlutdruck(Blutdruck.AUFFAELLIG)
                .withBewegungsapparat(List.of(Bewegungsapparat.SKOLIOSE, Bewegungsapparat.FUSSFEHLSTELLUNG))
                .withEinbeinhuepfen(Einbeinhuepfen.SICHER)
                .withEinbeinstand(Einbeinstand.NICHT_MOEGLICH)
                .withMotorik(Motorik.NORMAL)));
        untersuchung.setUntersuchungArzt(existingArzt);
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
        kind.setName(newName);
        return kindService.persistWithoutDuplicateCheck(kind);
    }

    @NonNull
    @Transactional(TxType.REQUIRED)
    public Kind changeEmailOfKind(@NonNull ID<Kind> kindID, @NonNull EmailAddress emailAddress) {
        Kind kind = kindService.getById(kindID);
        kind.setEmail(emailAddress);
        return kindService.persistWithoutDuplicateCheck(kind);
    }

    @NonNull
    @Transactional(TxType.REQUIRED)
    @CanIgnoreReturnValue
    public Schule setTimestampFirstImportOfSchule(@NonNull ID<Schule> schuleID, @Nullable OffsetDateTime timestamp) {
        Schule schule = schuleService.getById(schuleID);
        Objects.requireNonNull(schule.getExternalImportInfo()).setTimestampFirstImport(timestamp);
        return schuleService.persist(schule);
    }

    @NonNull
    public Aufgabe createAufgabe() {
        return createAufgabe(null);
    }

    @NonNull
    public Aufgabe createAufgabe(@Nullable Kind kind) {
        // Create a Aufgabe
        Benutzer benutzer = createBenutzer();
        Aufgabe aufgabe = AufgabeBuilder.create(
            builder -> builder
                .withID(ID.random(Aufgabe.class))
                .withKind(kind)
                .withTitle("Basis Aufgabe")
                .withFaelligDatum(LocalDate.of(2025, 1, 1))
                .withUserErfasst(benutzer)
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
}
