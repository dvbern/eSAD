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

package ch.dvbern.oss.hemed.esc.api.shared.docmerger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.dvbstarter.shared.i18n.translations.TLProducer;
import ch.dvbern.lib.doctemplate.common.DocTemplateException;
import ch.dvbern.lib.doctemplate.common.MergeContext;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.Dezibel;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenZusaetzlicheAngaben;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.OtoskopieResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenOtoskopie;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.BrilleLinsenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.DistanzSehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.FarbsehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.InspektionKonvergenzResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.MotilitaetResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StereotestResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusResultat;
import ch.dvbern.oss.hemed.esc.api.features.arzt.Arzt;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.briefgenerierung.docmerger.MergeSourceCreatorService;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.briefgenerierung.docmerger.RootMergeSource;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Nachkontrolle;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.printer.BasePrinter;
import ch.dvbern.oss.hemed.esc.api.shared.validator.MergeSourceDummyDataCreator;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassConfigBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.ArztBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.DossierBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.EinladungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KindBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KlasseBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KontaktPersonKindBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.NachkontrolleBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.SchuleBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungHoerenAudiogrammBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungHoerenOtoskopieBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfstatusKrankheitBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungInternBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungMassnahmenBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungSehenBeurteilungBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class DocMergerTest {

    private static final String OUTPUT_PATH = Paths.get(FileUtils.getTempDirectoryPath(), "esc", "docmerge").toString();

    private final TL tl = new TLProducer(() -> AppLanguages.DE).forAppLanguage(AppLanguages.DE);

    @BeforeAll
    static void setUp() throws IOException {
        FileUtils.forceMkdir(new File(OUTPUT_PATH));
        for (TenantEnum tenant : TenantEnum.values()) {
            FileUtils.forceMkdir(new File(OUTPUT_PATH + '/' + tenant));
        }
    }

    @ParameterizedTest
    @EnumSource(TenantEnum.class)
    void allProperties(TenantEnum tenant) {
        // ARRANGE
        Kind kind = KindBuilder.create(builder -> builder.withSchule(SchuleBuilder.defaultSchule())
            .withKlasse(KlasseBuilder.defaultKlasse()));
        Dossier dossier = DossierBuilder.create(builder -> builder.forKind(kind)
            .withZustaendigArzt(BenutzerBuilder.defaultBenutzer()));
        Nachkontrolle nachkontrolle =
            NachkontrolleBuilder.create(builder -> builder.forDossier(dossier)
                .withNachkontrolleSehen(true)
                .withNachkontrolleHoeren(true));

        List<UntersuchungImpfungIntern> untersuchungImpfungInterns = List.of(
            UntersuchungImpfungInternBuilder.create(builder -> builder.withDatumImpfung(LocalDate.now())),
            UntersuchungImpfungInternBuilder.create(builder -> builder.withDatumImpfung(LocalDate.now().minusWeeks(2)))
        );

        List<UntersuchungImpfstatusKrankheit> untersuchungImpfstatus = Arrays.stream(Krankheit.values())
            .map(krankheit -> UntersuchungImpfstatusKrankheitBuilder.create(builder -> builder.forKrankheit(krankheit)
                .withAnzahlImpfungen(5)
                .withDatumLetzteImpfung(LocalDate.now())))
            .toList();

        Benutzer currentBenutzer = BenutzerBuilder.create(builder -> builder.withVorname("Current"));

        Arzt arzt = ArztBuilder.create(builder -> builder.withAnrede("Dr. Med.")
            .withName("Arzt")
            .withVorame("Armin"));

        Einladung einladung = EinladungBuilder.create(build -> build.withDossier(dossier)
            .withUntersuchung(MergeSourceDummyDataCreator.getTestUntersuchung()));

        RootMergeSource mergeSource = MergeSourceCreatorService.toRootMergeSourceWithAnlass(
            einladung,
            nachkontrolle,
            untersuchungImpfstatus,
            untersuchungImpfungInterns,
            currentBenutzer,
            OffsetDateTime.now(),
            tl,
            arzt,
            KontaktPersonKindBuilder.defaultKontaktPersonKind()
        );

        // ACT
        try (InputStream inputStream = BasePrinter.class.getResourceAsStream("/DocMergerTest/all-properties.docx");
            FileOutputStream outputStream = getFileOutputStream("all-properties", tenant)) {

            if (inputStream != null) {
                byte[] result = DocMergerUtil.merge(inputStream, mergeSource);
                outputStream.write(result);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @ParameterizedTest
    @EnumSource(TenantEnum.class)
    void kindProperties(TenantEnum tenant) {
        // ARRANGE
        Kind kind = KindBuilder.create(builder -> builder.withSchule(SchuleBuilder.defaultSchule())
            .withKlasse(KlasseBuilder.defaultKlasse()));
        Dossier dossier = DossierBuilder.create(builder -> builder.forKind(kind)
            .withZustaendigArzt(BenutzerBuilder.defaultBenutzer()));
        Nachkontrolle nachkontrolle =
            NachkontrolleBuilder.create(builder -> builder.forDossier(dossier)
                .withNachkontrolleSehen(true)
                .withNachkontrolleHoeren(true));
        Benutzer currentBenutzer = BenutzerBuilder.create(builder -> builder.withVorname("Current"));
        Arzt arzt = ArztBuilder.create(builder -> builder.withAnrede("Dr. Med.")
            .withName("Arzt")
            .withVorame("Armin"));

        RootMergeSource mergeSource = MergeSourceCreatorService.toRootMergeSource(
            dossier,
            nachkontrolle,
            currentBenutzer,
            OffsetDateTime.now(),
            tl,
            arzt,
            KontaktPersonKindBuilder.defaultKontaktPersonKind()
        );

        // ACT
        try (InputStream inputStream = BasePrinter.class.getResourceAsStream("/DocMergerTest/kind-properties.docx");
            FileOutputStream outputStream = getFileOutputStream("kind-properties", tenant)) {

            if (inputStream != null) {
                byte[] result = DocMergerUtil.merge(inputStream, mergeSource);
                outputStream.write(result);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void missmatch_between_keys_and_type() {
        // ARRANGE
        Kind kind = KindBuilder.create(builder -> builder.withSchule(SchuleBuilder.defaultSchule())
            .withKlasse(KlasseBuilder.defaultKlasse()));
        Dossier dossier = DossierBuilder.create(builder -> builder.forKind(kind)
            .withZustaendigArzt(BenutzerBuilder.defaultBenutzer()));
        Nachkontrolle nachkontrolle =
            NachkontrolleBuilder.create(builder -> builder.forDossier(dossier)
                .withNachkontrolleSehen(true)
                .withNachkontrolleHoeren(true));
        Benutzer currentBenutzer = BenutzerBuilder.create(builder -> builder.withVorname("Current"));
        Arzt arzt = ArztBuilder.create(builder -> builder.withAnrede("Dr. Med.")
            .withName("Arzt")
            .withVorame("Armin"));

        RootMergeSource mergeSource = MergeSourceCreatorService.toRootMergeSource(
            dossier,
            nachkontrolle,
            currentBenutzer,
            OffsetDateTime.now(),
            tl,
            arzt,
            KontaktPersonKindBuilder.defaultKontaktPersonKind()
        );

        // ACT & ASSERT
        try (InputStream inputStream = BasePrinter.class.getResourceAsStream("/DocMergerTest/all-properties.docx")) {
            Objects.requireNonNull(inputStream);

            Assertions.assertThatThrownBy(() -> DocMergerUtil.merge(inputStream, mergeSource))
                .isInstanceOf(AppValidationException.class)
                .hasMessageContaining("AppValidation.DOCMERGE_KEY_NOT_SUPPORTED_BY_BRIEFVORLAGE_TYPE");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @ParameterizedTest
    @MethodSource("beurteilungSehenResults")
    void beurteilungSehen(
        @NonNull String expectedValue,
        @NonNull UntersuchungSehenBeurteilung untersuchungSehenBeurteilung,
        @NonNull UntersuchungMassnahmen untersuchungMassnahmen,
        @NonNull AnlassConfig anlassConfig
    ) throws DocTemplateException {
        testRootMergeSource(
            "anlass.sehen.beurteilung",
            expectedValue,
            untersuchungMassnahmen,
            untersuchungSehenBeurteilung,
            null,
            null,
            anlassConfig);
    }

    @ParameterizedTest
    @MethodSource("farbsehenResults")
    void farbsehen(
        @NonNull String expectedValue,
        @NonNull UntersuchungSehenBeurteilung untersuchungSehenBeurteilung,
        @Nullable UntersuchungMassnahmen untersuchungMassnahmen
    ) throws DocTemplateException {
        testRootMergeSource(
            "anlass.sehen.farbsehen",
            expectedValue,
            untersuchungMassnahmen,
            untersuchungSehenBeurteilung,
            null,
            null,
            AnlassConfigBuilder.allSameRequirementSetting(RequirementSetting.OPTIONAL));
    }

    @ParameterizedTest
    @MethodSource("zusammenfassungOhrenResultsAudiogramm")
    void zusammenfassunOhrenAudiogramm(
        @NonNull String expectedValue,
        @NonNull UntersuchungHoerenAudiogramm untersuchungHoerenAudiogramm,
        @NonNull UntersuchungMassnahmen untersuchungMassnahmen,
        @NonNull AnlassConfig anlassConfig
    ) throws DocTemplateException {
        testRootMergeSource(
            "anlass.hoeren.audiogramm.zusammenfassungOhren",
            expectedValue,
            untersuchungMassnahmen,
            null,
            untersuchungHoerenAudiogramm,
            null,
            anlassConfig);
    }

    @ParameterizedTest
    @MethodSource("beurteilungAudiogramResults")
    void beurteilungAudiogram(
        @NonNull String expectedValue,
        @NonNull UntersuchungHoerenAudiogramm untersuchungHoerenAudiogramm
    ) throws DocTemplateException {
        testRootMergeSource(
            "anlass.hoeren.audiogramm.beurteilungAudiogramm",
            expectedValue,
            UntersuchungMassnahmenBuilder.defaultUntersuchungMassnahmen(LocalDate.now()),
            null,
            untersuchungHoerenAudiogramm,
            null,
            AnlassConfigBuilder.allSameRequirementSetting(RequirementSetting.OPTIONAL)
        );
    }

    @ParameterizedTest
    @MethodSource("zusammenfassungOhrenResultsOtoskopie")
    void zusammenfassunOhrenOtoskopie(
        @NonNull String expectedValue,
        @NonNull UntersuchungHoerenOtoskopie untersuchungHoerenOtoskopie,
        @NonNull UntersuchungMassnahmen untersuchungMassnahmen,
        @NonNull AnlassConfig anlassConfig
    ) throws DocTemplateException {
        testRootMergeSource(
            "anlass.hoeren.otoskopie.zusammenfassungOhren",
            expectedValue,
            untersuchungMassnahmen,
            null,
            null,
            untersuchungHoerenOtoskopie,
            anlassConfig);
    }

    @ParameterizedTest
    @MethodSource("beurteilungOhrenResultsOtoskopie")
    void resultatOhrenOtoskopie(
        @NonNull String expectedLinks,
        @NonNull String expectedRechts,
        @NonNull UntersuchungHoerenOtoskopie untersuchungHoerenOtoskopie
    ) throws DocTemplateException {
        testRootMergeSource(
            "anlass.hoeren.otoskopie.otoskopieLinks",
            expectedLinks,
            UntersuchungMassnahmenBuilder.defaultUntersuchungMassnahmen(LocalDate.now()),
            null,
            null,
            untersuchungHoerenOtoskopie,
            AnlassConfigBuilder.allSameRequirementSetting(RequirementSetting.OPTIONAL));

        testRootMergeSource(
            "anlass.hoeren.otoskopie.otoskopieRechts",
            expectedRechts,
            UntersuchungMassnahmenBuilder.defaultUntersuchungMassnahmen(LocalDate.now()),
            null,
            null,
            untersuchungHoerenOtoskopie,
            AnlassConfigBuilder.allSameRequirementSetting(RequirementSetting.OPTIONAL));
    }

    private void testRootMergeSource(
        @NonNull String key,
        @NotNull String expectedValue,
        @Nullable UntersuchungMassnahmen untersuchungMassnahmen,
        @Nullable UntersuchungSehenBeurteilung untersuchungSehenBeurteilung,
        @Nullable UntersuchungHoerenAudiogramm untersuchungHoerenAudiogramm,
        @Nullable UntersuchungHoerenOtoskopie untersuchungHoerenOtoskopie,
        @NonNull AnlassConfig anlassConfig
    ) throws DocTemplateException {
        // ARRANGE
        Dossier dossier = DossierBuilder.defaultDossier();
        Nachkontrolle nachkontrolle = NachkontrolleBuilder.defaultNachkontrolle();
        Benutzer currentBenutzer = BenutzerBuilder.defaultBenutzer();

        Untersuchung untersuchung = MergeSourceDummyDataCreator.getTestUntersuchung();
        untersuchung.setUntersuchungSehenBeurteilung(untersuchungSehenBeurteilung);
        untersuchung.setUntersuchungHoerenAudiogramm(untersuchungHoerenAudiogramm);
        untersuchung.setUntersuchungHoerenOtoskopie(untersuchungHoerenOtoskopie);
        untersuchung.setUntersuchungMassnahmen(untersuchungMassnahmen);

        Anlass anlass =
            AnlassBuilder.create(builder -> builder.withAnlassConfig(anlassConfig));

        Einladung einladung =
            EinladungBuilder.create(build -> build.withDossier(dossier)
                .withUntersuchung(untersuchung)
                .withAnlass(anlass));

        RootMergeSource mergeSource = MergeSourceCreatorService.toRootMergeSourceWithAnlass(
            einladung,
            nachkontrolle,
            List.of(),
            List.of(),
            currentBenutzer,
            OffsetDateTime.now(),
            tl,
            ArztBuilder.defaultArzt(),
            KontaktPersonKindBuilder.defaultKontaktPersonKind()
        );

        MergeContext ctx = new MergeContext(mergeSource);
        assertThat(mergeSource.getData(ctx, key)).isEqualTo(expectedValue);
    }

    private FileOutputStream getFileOutputStream(@NonNull String filename, @NonNull TenantEnum tenant)
        throws IOException {
        return new FileOutputStream(getTempFilenamePath(filename, tenant));
    }

    private static String getTempFilenamePath(@NonNull String filename, @NonNull TenantEnum tenant) {
        String path = OUTPUT_PATH + '/' + tenant.name() + '/' + filename + ".docx";
        LOG.info("Writing to {}", path);
        return path;
    }

    @NonNull
    private static Stream<Arguments> beurteilungSehenResults() {
        return Stream.of(
            // Everything good
            Arguments.of(
                "normal",
                UntersuchungSehenBeurteilungBuilder.create(builder -> builder.withFarbsehen(FarbsehenResultat.NORMAL)
                    .withStereotest(StereotestResultat.NORMAL)
                    .withBrilleLinsen(BrilleLinsenResultat.OHNE_KEINE)
                    .withFernvisus(List.of(DistanzSehenResultat.NORMAL))
                    .withNahvisus(List.of(DistanzSehenResultat.NORMAL))
                    .withStrabismus(List.of(StrabismusResultat.KEIN_STRABISMUS))
                    .withInspektion(InspektionKonvergenzResultat.UNAUFFAELLIG)
                    .withKonvergenz(InspektionKonvergenzResultat.UNAUFFAELLIG)
                    .withMotilitaet(MotilitaetResultat.NORMAL)),
                UntersuchungMassnahmenBuilder.defaultUntersuchungMassnahmen(LocalDate.now()),
                AnlassConfigBuilder.allSameRequirementSetting(RequirementSetting.OPTIONAL)
            ),
            // Stuff's always normal if nothing has to be filled out. ¯\_(ツ)_/¯
            Arguments.of(
                "normal",
                UntersuchungSehenBeurteilungBuilder.defaultUntersuchungSehen(LocalDate.now()),
                UntersuchungMassnahmenBuilder.defaultUntersuchungMassnahmen(LocalDate.now()),
                AnlassConfigBuilder.allSameRequirementSetting(RequirementSetting.NOT_VISIBLE)
            ),
            // Anything thats off -> "siehe begleitbrief"
            Arguments.of(
                "siehe Begleitbrief",
                UntersuchungSehenBeurteilungBuilder.create(builder -> builder.withBrilleLinsen(BrilleLinsenResultat.OHNE_VERGESSEN)),
                UntersuchungMassnahmenBuilder.defaultUntersuchungMassnahmen(LocalDate.now()),
                AnlassConfigBuilder.allSameRequirementSetting(RequirementSetting.OPTIONAL)
            ),
            // Nothing matters if bereits in Untersuchung for Sehen
            Arguments.of(
                "bereits in Behandlung",
                UntersuchungSehenBeurteilungBuilder.defaultUntersuchungSehen(LocalDate.now()),
                UntersuchungMassnahmenBuilder.create(builder -> builder.withBereitsBehandeltSehen(true)),
                AnlassConfigBuilder.allSameRequirementSetting(RequirementSetting.OPTIONAL)
            ),
            // Nothing has been checked
            Arguments.arguments(
                "nicht geprüft worden",
                UntersuchungSehenBeurteilungBuilder.create(builder -> builder.withFarbsehen(FarbsehenResultat.NICHT_GEPRUEFT)
                    .withStereotest(StereotestResultat.NICHT_GEPRUEFT)
                    .withBrilleLinsen(BrilleLinsenResultat.NICHT_GEPRUEFT)
                    .withFernvisus(List.of(DistanzSehenResultat.NICHT_GEPRUEFT))
                    .withNahvisus(List.of(DistanzSehenResultat.NICHT_GEPRUEFT))
                    .withStrabismus(List.of(StrabismusResultat.NICHT_GEPRUEFT))
                    .withInspektion(InspektionKonvergenzResultat.NICHT_GEPRUEFT)
                    .withKonvergenz(InspektionKonvergenzResultat.NICHT_GEPRUEFT)
                    .withMotilitaet(MotilitaetResultat.NICHT_GEPRUEFT)),
                UntersuchungMassnahmenBuilder.defaultUntersuchungMassnahmen(LocalDate.now()),
                AnlassConfigBuilder.allSameRequirementSetting(RequirementSetting.OPTIONAL)
            )
        );
    }

    private static Stream<Arguments> farbsehenResults() {
        return Stream.of(
            Arguments.of(
                "unklar und muss daher von einem Augenarzt/ einer Augenärztin überprüft werden.",
                UntersuchungSehenBeurteilungBuilder.create(builder -> builder.withFarbsehen(FarbsehenResultat.UNSICHER)),
                UntersuchungMassnahmenBuilder.defaultUntersuchungMassnahmen(LocalDate.now())
            ),
            Arguments.of(
                "unklar und muss daher von einem Augenarzt/ einer Augenärztin überprüft werden.",
                UntersuchungSehenBeurteilungBuilder.create(builder -> builder.withFarbsehen(FarbsehenResultat.UNSICHER)),
                null
            ),
            Arguments.of(
                "unklar und muss daher von einem Augenarzt/ einer Augenärztin überprüft werden.",
                UntersuchungSehenBeurteilungBuilder.create(builder -> builder.withFarbsehen(FarbsehenResultat.UNSICHER)),
                UntersuchungMassnahmenBuilder.defaultUntersuchungMassnahmen(LocalDate.now())
            ),
            Arguments.of(
                "unklar und wird in der nächsten schulärztlichen Untersuchung nochmals kontrolliert",
                UntersuchungSehenBeurteilungBuilder.create(builder -> builder.withFarbsehen(FarbsehenResultat.UNSICHER)),
                UntersuchungMassnahmenBuilder.create(builder -> builder.withNachkontrolle(
                    true,
                    false,
                    false,
                    false,
                    false,
                    false

                ))
            ));
    }

    @NonNull
    private static Stream<Arguments> zusammenfassungOhrenResultsAudiogramm() {
        return Stream.of(
            // Everything good
            Arguments.of(
                "normal",
                UntersuchungHoerenAudiogrammBuilder.create(builder -> builder
                    .withZusaetzlicheAngaben(List.of(HoerenZusaetzlicheAngaben.KEINE))
                    .withAudiogrammBeurteilung(List.of(AudiogrammBeurteilung.NORMAL))
                    .withAudiogrammLinks(Dezibel.DB20)
                    .withAudiogrammRechts(Dezibel.DB20)),
                UntersuchungMassnahmenBuilder.defaultUntersuchungMassnahmen(LocalDate.now()),
                AnlassConfigBuilder.allSameRequirementSetting(RequirementSetting.OPTIONAL)
            ),
            // DB's off
            Arguments.of(
                "siehe Begleitbrief",
                UntersuchungHoerenAudiogrammBuilder.create(builder -> builder
                    .withAudiogrammRechts(Dezibel.DB10)),
                UntersuchungMassnahmenBuilder.defaultUntersuchungMassnahmen(LocalDate.now()),
                AnlassConfigBuilder.allSameRequirementSetting(RequirementSetting.OPTIONAL)
            ),
            // Some be-urteilung not NORMAL or KEINE
            Arguments.of(
                "siehe Begleitbrief",
                UntersuchungHoerenAudiogrammBuilder.create(builder -> builder
                    .withAudiogrammRechts(Dezibel.DB20)
                    .withAudiogrammLinks(Dezibel.DB10)),
                UntersuchungMassnahmenBuilder.defaultUntersuchungMassnahmen(LocalDate.now()),
                AnlassConfigBuilder.allSameRequirementSetting(RequirementSetting.OPTIONAL)
            ),
            // Nothing matters if bereits in Untersuchung for Hoeren
            Arguments.of(
                "bereits in Behandlung",
                UntersuchungHoerenAudiogrammBuilder.create(builder -> builder
                    .withAudiogrammRechts(Dezibel.DB10)
                    .withAudiogrammLinks(Dezibel.DB40)),
                UntersuchungMassnahmenBuilder.create(builder -> builder.withBereitsBehandeltHoeren(true)),
                AnlassConfigBuilder.allSameRequirementSetting(RequirementSetting.OPTIONAL)
            )
        );
    }

    private static Stream<Arguments> beurteilungAudiogramResults() {
        return Stream.of(
            Arguments.of(
                "beidseitig vermindert",
                UntersuchungHoerenAudiogrammBuilder.create(builder -> builder.withAudiogrammBeurteilung(List.of(
                    AudiogrammBeurteilung.LINKS_REDUZIERT,
                    AudiogrammBeurteilung.RECHTS_REDUZIERT))
                )),
            Arguments.of(
                "links vermindert",
                UntersuchungHoerenAudiogrammBuilder.create(builder -> builder.withAudiogrammBeurteilung(List.of(
                    AudiogrammBeurteilung.LINKS_REDUZIERT)))),
            Arguments.of(
                "rechts vermindert",
                UntersuchungHoerenAudiogrammBuilder.create(builder -> builder.withAudiogrammBeurteilung(List.of(
                    AudiogrammBeurteilung.RECHTS_REDUZIERT))
                )),
            Arguments.of(
                "unaufällig",
                UntersuchungHoerenAudiogrammBuilder.create(builder -> builder.withAudiogrammBeurteilung(List.of(
                    AudiogrammBeurteilung.NORMAL)))),
            Arguments.of(
                "nicht geprüft",
                UntersuchungHoerenAudiogrammBuilder.create(builder -> builder.withAudiogrammBeurteilung(List.of(
                    AudiogrammBeurteilung.NICHT_GEPRUEFT)))
            ),
            Arguments.of(
                "",
                UntersuchungHoerenAudiogrammBuilder.create(builder -> builder.withAudiogrammBeurteilung(List.of()))
            ),
            Arguments.of(
                "unaufällig, nicht geprüft",
                UntersuchungHoerenAudiogrammBuilder.create(builder -> builder.withAudiogrammBeurteilung(List.of(
                    AudiogrammBeurteilung.NICHT_GEPRUEFT, AudiogrammBeurteilung.NORMAL)))
            ),
            Arguments.of(
                "beidseitig vermindert, unaufällig",
                UntersuchungHoerenAudiogrammBuilder.create(builder -> builder.withAudiogrammBeurteilung(List.of(
                    AudiogrammBeurteilung.NORMAL,
                    AudiogrammBeurteilung.RECHTS_REDUZIERT,
                    AudiogrammBeurteilung.LINKS_REDUZIERT)))
            ),
            Arguments.of(
                "unaufällig, rechts vermindert",
                UntersuchungHoerenAudiogrammBuilder.create(builder -> builder.withAudiogrammBeurteilung(List.of(
                    AudiogrammBeurteilung.NORMAL,
                    AudiogrammBeurteilung.RECHTS_REDUZIERT)))
            )
        );
    }

    @NonNull
    private static Stream<Arguments> zusammenfassungOhrenResultsOtoskopie() {
        return Stream.of(
            // Everything good
            Arguments.of(
                "normal",
                UntersuchungHoerenOtoskopieBuilder.create(builder -> builder
                    .withOtoskopieLinks(List.of(OtoskopieResultat.NORMAL))
                    .withOtoskopieRechts(List.of(OtoskopieResultat.NORMAL))),
                UntersuchungMassnahmenBuilder.defaultUntersuchungMassnahmen(LocalDate.now()),
                AnlassConfigBuilder.allSameRequirementSetting(RequirementSetting.OPTIONAL)
            ),
            // DB's off
            Arguments.of(
                "normal",
                UntersuchungHoerenOtoskopieBuilder.defaultUntersuchungHoerenOtoskopie(LocalDate.now()),
                UntersuchungMassnahmenBuilder.defaultUntersuchungMassnahmen(LocalDate.now()),
                AnlassConfigBuilder.allSameRequirementSetting(RequirementSetting.OPTIONAL)
            ),
            // Some be-urteilung not NORMAL or KEINE
            Arguments.of(
                "siehe Begleitbrief",
                UntersuchungHoerenOtoskopieBuilder.create(builder -> builder
                    .withOtoskopieLinks(List.of(OtoskopieResultat.NICHT_EINSEHBAR))),
                UntersuchungMassnahmenBuilder.defaultUntersuchungMassnahmen(LocalDate.now()),
                AnlassConfigBuilder.allSameRequirementSetting(RequirementSetting.OPTIONAL)
            ),
            // Nothing matters if bereits in Untersuchung for Hoeren
            Arguments.of(
                "bereits in Behandlung",
                UntersuchungHoerenOtoskopieBuilder.create(builder -> builder
                    .withOtoskopieLinks(List.of(OtoskopieResultat.NICHT_EINSEHBAR))),
                UntersuchungMassnahmenBuilder.create(builder -> builder.withBereitsBehandeltHoeren(true)),
                AnlassConfigBuilder.allSameRequirementSetting(RequirementSetting.OPTIONAL)
            )
        );
    }

    @NonNull
    private static Stream<Arguments> beurteilungOhrenResultsOtoskopie() {
        Stream<Arguments> testsDefinedResults = Map.of(
            OtoskopieResultat.NORMAL,
            "hat keine Auffälligkeiten gezeigt",
            OtoskopieResultat.TROMMELFELL_CERUMEN,
            "hat ergeben, dass das Trommelfell aufgrund von Ohrenschmalz nicht einsehbar war.",
            OtoskopieResultat.NICHT_EINSEHBAR,
            "hat ergeben, dass das Trommelfell nicht einsehbar war.",
            OtoskopieResultat.TROMMELFELL_PERFORATION,
            "hat eine Trommelfellperforation gezeigt.",
            OtoskopieResultat.SEROMUKOTYMPANON,
            "hat ein Seromukotympanon gezeigt.",
            OtoskopieResultat.NICHT_GEPRUEFT,
            "wurde nicht durchgeführt."
        ).entrySet().stream().flatMap(
            entry ->
                Stream.of(
                    Arguments.of(
                        entry.getValue(),
                        entry.getValue(),
                        UntersuchungHoerenOtoskopieBuilder.create(builder -> builder
                            .withOtoskopieLinks(List.of(entry.getKey()))
                            .withOtoskopieLinksResultatAndere("Bemerkung Links")
                            .withOtoskopieRechts(List.of(entry.getKey()))
                            .withOtoskopieRechtsResultatAndere("Bemerkung Rechts")
                        )
                    ),
                    Arguments.of(
                        entry.getValue(),
                        entry.getValue(),
                        UntersuchungHoerenOtoskopieBuilder.create(builder -> builder
                            .withOtoskopieLinks(List.of(entry.getKey()))
                            .withOtoskopieRechts(List.of(entry.getKey()))
                        )
                    ))

        );

        Stream<Arguments> testsAnderes = Stream.of(
            Arguments.of(
                "hat ergeben, dass es Abweichungen gibt: Bemerkung Links Andere",
                "hat ergeben, dass es Abweichungen gibt: Bemerkung Rechts Andere",
                UntersuchungHoerenOtoskopieBuilder.create(builder -> builder
                    .withOtoskopieLinks(List.of(OtoskopieResultat.ANDERES))
                    .withOtoskopieLinksResultatAndere("Bemerkung Links Andere")
                    .withOtoskopieRechts(List.of(OtoskopieResultat.ANDERES))
                    .withOtoskopieRechtsResultatAndere("Bemerkung Rechts Andere")
                )
            ),
            Arguments.of(
                "hat ergeben, dass es Abweichungen gibt",
                "hat ergeben, dass es Abweichungen gibt",
                UntersuchungHoerenOtoskopieBuilder.create(builder -> builder
                    .withOtoskopieLinks(List.of(OtoskopieResultat.ANDERES))
                    .withOtoskopieRechts(List.of(OtoskopieResultat.ANDERES))
                )
            )
        );

        return Stream.concat(testsDefinedResults, testsAnderes);
    }
}
