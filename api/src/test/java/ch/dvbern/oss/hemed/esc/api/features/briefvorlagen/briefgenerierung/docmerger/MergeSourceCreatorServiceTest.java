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

package ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.briefgenerierung.docmerger;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.dvbstarter.shared.i18n.translations.TLProducer;
import ch.dvbern.lib.doctemplate.common.DocTemplateException;
import ch.dvbern.lib.doctemplate.common.MergeContext;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Blutdruck;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfschutzGegeben;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.types.ImpfungenMassnahmenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.person.Adresse;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MergeSourceCreatorServiceTest {

    private static final String BULLET_PREFIX = "\u2003•\u2002";

    private final TL tl = createTl(AppLanguages.DE);
    private final TL tlEn = createTl(AppLanguages.EN);
    private final TL tlFr = createTl(AppLanguages.FR);
    private final TL tlIt = createTl(AppLanguages.IT);

    private static TL createTl(AppLanguages language) {
        return new TLProducer(
            () -> language,
            TenantContext.forTenant(TenantEnum.DEFAULT)
        ).forAppLanguage(language);
    }

    @Test
    void formatAddress_returns_empty_when_address_is_null() {
        // ARRANGE
        Adresse address = null;

        // ACT
        String result = MergeSourceCreatorService.formatAddress(address);

        // ASSERT
        assertThat(result)
            .isEmpty();
    }

    @Test
    void formatAddress_returns_formatted_address_when_all_fields_are_present() {
        // ARRANGE
        Adresse address = new Adresse();
        address.setStrasseUndNr("Teststrasse 123");
        address.setAdresseZusatz("4. Stock");
        address.setPlz("3000");
        address.setOrt("Bern");

        // ACT
        String result = MergeSourceCreatorService.formatAddress(address);

        // ASSERT
        assertThat(result)
            .isEqualTo("Teststrasse 123\n4. Stock\n3000 Bern");
    }

    @Test
    void formatAddress_returns_empty_when_all_fields_are_empty() {
        // ARRANGE
        Adresse address = new Adresse();
        address.setStrasseUndNr("");
        address.setAdresseZusatz("");
        address.setPlz("");
        address.setOrt("");

        // ACT
        String result = MergeSourceCreatorService.formatAddress(address);

        // ASSERT
        assertThat(result)
            .isEmpty();
    }

    @ParameterizedTest
    @MethodSource("providePartialAddresses")
    void formatAddress_correctly_formats_partial_addresses(
        String strasseUndNr,
        String adresseZusatz,
        String plz,
        String ort,
        String expected
    ) {
        // ARRANGE
        Adresse address = new Adresse();
        address.setStrasseUndNr(strasseUndNr);
        address.setAdresseZusatz(adresseZusatz);
        address.setPlz(plz);
        address.setOrt(ort);

        // ACT
        String result = MergeSourceCreatorService.formatAddress(address);

        // ASSERT
        assertThat(result)
            .isEqualTo(expected);
    }

    private static Stream<Arguments> providePartialAddresses() {
        return Stream.of(
            // Street only
            Arguments.of("Teststrasse 123", "", "", "", "Teststrasse 123"),
            // Street and city
            Arguments.of("Teststrasse 123", "", "", "Bern", "Teststrasse 123\nBern"),
            // Street and ZIP
            Arguments.of("Teststrasse 123", "", "3000", "", "Teststrasse 123\n3000"),
            // ZIP and city only
            Arguments.of("", "", "3000", "Bern", "3000 Bern"),
            // Adresszusatz only
            Arguments.of("", "4. Stock", "", "", "4. Stock"),
            // Various combinations
            Arguments.of("Teststrasse 123", "4. Stock", "", "", "Teststrasse 123\n4. Stock"),
            Arguments.of("", "", "3000", "Bern", "3000 Bern")
        );
    }

    @Test
    void getFilteredImpfstatusList_returns_Keine_when_list_is_empty_or_no_match() {
        // ACT & ASSERT
        assertThat(MergeSourceCreatorService.getFilteredImpfstatusList(
            List.of(),
            tl,
            impfstatus -> true
        ))
            .isEqualTo("Keine");

        UntersuchungImpfstatusKrankheit impfstatus = new UntersuchungImpfstatusKrankheit();
        impfstatus.setKrankheit(Krankheit.DIPHTERIE);
        assertThat(MergeSourceCreatorService.getFilteredImpfstatusList(
            List.of(impfstatus),
            tl,
            item -> false
        ))
            .isEqualTo("Keine");
    }

    @Test
    void getFilteredImpfstatusList_returns_localized_Keine_for_other_languages() {
        // ACT & ASSERT
        assertThat(MergeSourceCreatorService.getFilteredImpfstatusList(
            List.of(),
            tlEn,
            impfstatus -> true
        ))
            .isEqualTo("None");

        assertThat(MergeSourceCreatorService.getFilteredImpfstatusList(
            List.of(),
            tlFr,
            impfstatus -> true
        ))
            .isEqualTo("Aucun");

        assertThat(MergeSourceCreatorService.getFilteredImpfstatusList(
            List.of(),
            tlIt,
            impfstatus -> true
        ))
            .isEqualTo("Nessuno");
    }

    @Test
    void getFilteredImpfstatusList_returns_sorted_distinct_newline_separated_list_when_matches_exist() {
        // ARRANGE
        UntersuchungImpfstatusKrankheit impfstatus1 = new UntersuchungImpfstatusKrankheit();
        impfstatus1.setKrankheit(Krankheit.TETANUS);
        impfstatus1.setImpfschutzGegeben(ImpfschutzGegeben.JA);

        UntersuchungImpfstatusKrankheit impfstatus2 = new UntersuchungImpfstatusKrankheit();
        impfstatus2.setKrankheit(Krankheit.DIPHTERIE);
        impfstatus2.setImpfschutzGegeben(ImpfschutzGegeben.JA);

        UntersuchungImpfstatusKrankheit impfstatusDuplicate = new UntersuchungImpfstatusKrankheit();
        impfstatusDuplicate.setKrankheit(Krankheit.DIPHTERIE);
        impfstatusDuplicate.setImpfschutzGegeben(ImpfschutzGegeben.JA);

        UntersuchungImpfstatusKrankheit impfstatusNonMatching = new UntersuchungImpfstatusKrankheit();
        impfstatusNonMatching.setKrankheit(Krankheit.PERTUSSIS);
        impfstatusNonMatching.setImpfschutzGegeben(ImpfschutzGegeben.NEIN);

        List<UntersuchungImpfstatusKrankheit> list = List.of(
            impfstatus1,
            impfstatus2,
            impfstatusDuplicate,
            impfstatusNonMatching
        );

        // ACT
        String result = MergeSourceCreatorService.getFilteredImpfstatusList(
            list,
            tl,
            impfstatus -> impfstatus.getImpfschutzGegeben() == ImpfschutzGegeben.JA
        );

        // ASSERT
        assertThat(result)
            .isEqualTo(BULLET_PREFIX + "Diphtherie\n" + BULLET_PREFIX + "Tetanus");
    }

    @Test
    void anlassMergeSource_resolves_all_new_variables() throws DocTemplateException {
        // ARRANGE
        ImpfungDocMergerDTO impfungDto = new ImpfungDocMergerDTO("Boostrix");
        ImpfstatusListSummaryDocMergerDTO impfstatusListSummaryDto = new ImpfstatusListSummaryDocMergerDTO(
            BULLET_PREFIX + "Diphtherie\n" + BULLET_PREFIX + "Tetanus",
            BULLET_PREFIX + "FSME",
            "Keine"
        );
        AnlassMergeSource mergeSource = new AnlassMergeSource(
            new AnlassMetadatenDocMergerDTO(null, null, null, null),
            List.of(),
            impfstatusListSummaryDto,
            impfungDto,
            null,
            null,
            null,
            null,
            null
        );
        MergeContext ctx = new MergeContext(mergeSource);

        // ACT & ASSERT - impfung prefix (existing overview)
        assertThat(mergeSource.getData(ctx, "impfung.impfungList"))
            .isEqualTo("Boostrix");

        // ACT & ASSERT - impfstatusListe prefix (new filtered variables)
        assertThat(mergeSource.getData(ctx, "impfstatusListe.impfschutzGenuegend"))
            .isEqualTo(BULLET_PREFIX + "Diphtherie\n" + BULLET_PREFIX + "Tetanus");
        assertThat(mergeSource.getData(ctx, "impfstatusListe.impfempfehlung"))
            .isEqualTo(BULLET_PREFIX + "FSME");
        assertThat(mergeSource.getData(ctx, "impfstatusListe.impfentscheid"))
            .isEqualTo("Keine");

        // ACT & ASSERT - case-insensitive under impfstatusListe prefix via BeanMergeSource
        assertThat(mergeSource.getData(ctx, "impfstatusListe.impfschutzgenuegend"))
            .isEqualTo(BULLET_PREFIX + "Diphtherie\n" + BULLET_PREFIX + "Tetanus");
        assertThat(mergeSource.getData(ctx, "impfstatusListe.Impfempfehlung"))
            .isEqualTo(BULLET_PREFIX + "FSME");
        assertThat(mergeSource.getData(ctx, "impfstatusListe.Impfentscheid"))
            .isEqualTo("Keine");

        // ACT & ASSERT - unsupported keys or prefixes return null (BeanMergeSource) or bracketed key
        assertThat(mergeSource.getData(ctx, "impfstatusListe.unknown"))
            .isNull();
        assertThat(mergeSource.getData(ctx, "impfschutzGenuegend"))
            .isEqualTo("[impfschutzGenuegend]");
        assertThat(mergeSource.getData(ctx, "krankheit.impfschutzGenuegend"))
            .isEqualTo("[krankheit.impfschutzGenuegend]");
    }

    @Test
    void getImpfausweisNichtVorhandenText_returns_empty_when_null_or_false() {
        // ACT & ASSERT
        assertThat(MergeSourceCreatorService.getImpfausweisNichtVorhandenText(null, tl))
            .isEmpty();

        UntersuchungImpfungen impfungen = new UntersuchungImpfungen();
        impfungen.setImpfausweisNichtVorhanden(false);
        assertThat(MergeSourceCreatorService.getImpfausweisNichtVorhandenText(impfungen, tl))
            .isEmpty();
    }

    @Test
    void getImpfausweisNichtVorhandenText_returns_text_in_all_languages() {
        // ARRANGE
        UntersuchungImpfungen impfungen = new UntersuchungImpfungen();
        impfungen.setImpfausweisNichtVorhanden(true);

        // ACT & ASSERT
        assertThat(MergeSourceCreatorService.getImpfausweisNichtVorhandenText(impfungen, tl))
            .isEqualTo("Ihr Kind hat keinen Impfausweis abgegeben oder es gibt Unklarheiten.");
        assertThat(MergeSourceCreatorService.getImpfausweisNichtVorhandenText(impfungen, tlEn))
            .isEqualTo("EN: Ihr Kind hat keinen Impfausweis abgegeben oder es gibt Unklarheiten.");
        assertThat(MergeSourceCreatorService.getImpfausweisNichtVorhandenText(impfungen, tlFr))
            .isEqualTo("FR: Ihr Kind hat keinen Impfausweis abgegeben oder es gibt Unklarheiten.");
        assertThat(MergeSourceCreatorService.getImpfausweisNichtVorhandenText(impfungen, tlIt))
            .isEqualTo("IT: Ihr Kind hat keinen Impfausweis abgegeben oder es gibt Unklarheiten.");
    }

    @Test
    void getMassnahmeImpfungBeimHausarztText_returns_empty_when_null_or_not_selected() {
        // ACT & ASSERT
        assertThat(MergeSourceCreatorService.getMassnahmeImpfungBeimHausarztText(null, tl))
            .isEmpty();

        UntersuchungImpfungen impfungen = new UntersuchungImpfungen();
        assertThat(MergeSourceCreatorService.getMassnahmeImpfungBeimHausarztText(impfungen, tl))
            .isEmpty();

        UntersuchungImpfungenMassnahmen massnahmen = new UntersuchungImpfungenMassnahmen();
        massnahmen.setEmpfehlung(EnumSet.noneOf(ImpfungenMassnahmenEmpfehlung.class));
        impfungen.setUntersuchungImpfungenMassnahmen(massnahmen);
        assertThat(MergeSourceCreatorService.getMassnahmeImpfungBeimHausarztText(impfungen, tl))
            .isEmpty();
    }

    @Test
    void getMassnahmeImpfungBeimHausarztText_returns_text_in_all_languages() {
        // ARRANGE
        UntersuchungImpfungen impfungen = new UntersuchungImpfungen();
        UntersuchungImpfungenMassnahmen massnahmen = new UntersuchungImpfungenMassnahmen();
        massnahmen.setEmpfehlung(EnumSet.of(ImpfungenMassnahmenEmpfehlung.IMPFUNG_BEIM_HAUSARZT));
        impfungen.setUntersuchungImpfungenMassnahmen(massnahmen);

        // ACT & ASSERT
        assertThat(MergeSourceCreatorService.getMassnahmeImpfungBeimHausarztText(impfungen, tl))
            .isEqualTo(
                "Wir empfehlen: Für noch fehlende Impfungen vereinbaren Sie einen Termin bei Ihrem Hausarzt / Ihrer"
                    + " Hausärztin.");
        assertThat(MergeSourceCreatorService.getMassnahmeImpfungBeimHausarztText(impfungen, tlEn))
            .isEqualTo(
                "EN: Wir empfehlen: Für noch fehlende Impfungen vereinbaren Sie einen Termin bei Ihrem Hausarzt /"
                    + " Ihrer Hausärztin.");
        assertThat(MergeSourceCreatorService.getMassnahmeImpfungBeimHausarztText(impfungen, tlFr))
            .isEqualTo(
                "FR: Wir empfehlen: Für noch fehlende Impfungen vereinbaren Sie einen Termin bei Ihrem Hausarzt /"
                    + " Ihrer Hausärztin.");
        assertThat(MergeSourceCreatorService.getMassnahmeImpfungBeimHausarztText(impfungen, tlIt))
            .isEqualTo(
                "IT: Wir empfehlen: Für noch fehlende Impfungen vereinbaren Sie einen Termin bei Ihrem Hausarzt /"
                    + " Ihrer Hausärztin.");
    }

    @Test
    void getBlutdruckAuffaelligText_returns_empty_when_null_or_not_auffaellig() {
        // ACT & ASSERT
        assertThat(MergeSourceCreatorService.getBlutdruckAuffaelligText(null, tl))
            .isEmpty();

        UntersuchungArztBeurteilung arzt = new UntersuchungArztBeurteilung();
        arzt.setBlutdruck(Blutdruck.NORMAL);
        assertThat(MergeSourceCreatorService.getBlutdruckAuffaelligText(arzt, tl))
            .isEmpty();

        arzt.setBlutdruck(Blutdruck.NICHT_GEPRUEFT);
        assertThat(MergeSourceCreatorService.getBlutdruckAuffaelligText(arzt, tl))
            .isEmpty();
    }

    @Test
    void getBlutdruckAuffaelligText_returns_text_in_all_languages() {
        // ARRANGE
        UntersuchungArztBeurteilung arzt = new UntersuchungArztBeurteilung();
        arzt.setBlutdruck(Blutdruck.AUFFAELLIG);

        // ACT & ASSERT
        assertThat(MergeSourceCreatorService.getBlutdruckAuffaelligText(arzt, tl))
            .isEqualTo("Bitte kontrollieren Sie in nächster Zeit den Blutdruck bei Ihrem Hausarzt/Ihrer Hausärztin.");
        assertThat(MergeSourceCreatorService.getBlutdruckAuffaelligText(arzt, tlEn))
            .isEqualTo(
                "EN: Bitte kontrollieren Sie in nächster Zeit den Blutdruck bei Ihrem Hausarzt/Ihrer Hausärztin.");
        assertThat(MergeSourceCreatorService.getBlutdruckAuffaelligText(arzt, tlFr))
            .isEqualTo(
                "FR: Bitte kontrollieren Sie in nächster Zeit den Blutdruck bei Ihrem Hausarzt/Ihrer Hausärztin.");
        assertThat(MergeSourceCreatorService.getBlutdruckAuffaelligText(arzt, tlIt))
            .isEqualTo(
                "IT: Bitte kontrollieren Sie in nächster Zeit den Blutdruck bei Ihrem Hausarzt/Ihrer Hausärztin.");
    }

    @Test
    void toImpfstatusListSummaryDTO_maps_summary_correctly() {
        // ARRANGE
        UntersuchungImpfstatusKrankheit impfstatus1 = new UntersuchungImpfstatusKrankheit();
        impfstatus1.setKrankheit(Krankheit.DIPHTERIE);
        impfstatus1.setImpfschutzGegeben(ImpfschutzGegeben.JA);

        UntersuchungImpfstatusKrankheit impfstatus2 = new UntersuchungImpfstatusKrankheit();
        impfstatus2.setKrankheit(Krankheit.FSME);
        impfstatus2.setImpfempfehlung(true);
        impfstatus2.setImpfentscheid(true);

        List<UntersuchungImpfstatusKrankheit> list = List.of(impfstatus1, impfstatus2);

        // ACT
        ImpfstatusListSummaryDocMergerDTO result = MergeSourceCreatorService.toImpfstatusListSummaryDTO(
            list,
            tl
        );

        // ASSERT
        assertThat(result.getImpfschutzGenuegend())
            .isEqualTo(BULLET_PREFIX + "Diphtherie");
        assertThat(result.getImpfempfehlung())
            .isEqualTo(BULLET_PREFIX + "FSME");
        assertThat(result.getImpfentscheid())
            .isEqualTo(BULLET_PREFIX + "FSME");

        // ACT empty
        ImpfstatusListSummaryDocMergerDTO emptyResult = MergeSourceCreatorService.toImpfstatusListSummaryDTO(
            List.of(),
            tl
        );

        // ASSERT empty
        assertThat(emptyResult.getImpfschutzGenuegend())
            .isEqualTo("Keine");
        assertThat(emptyResult.getImpfempfehlung())
            .isEqualTo("Keine");
        assertThat(emptyResult.getImpfentscheid())
            .isEqualTo("Keine");
    }

    @Test
    void anlassMergeSource_resolves_canonical_variables_only() throws DocTemplateException {
        // ARRANGE
        String impfausweisText = "Impfausweis nicht vorhanden.";
        String massnahmeHausarztText = "Wir empfehlen einen Impftermin bei Ihrer Ärztin/Ihrem Arzt.";
        String blutdruckText = "Bitte kontrollieren Sie den Blutdruck bei Ihrem Hausarzt/Ihrer Hausärztin";

        ImpfungDocMergerDTO impfungDto = new ImpfungDocMergerDTO(
            "Boostrix",
            impfausweisText,
            massnahmeHausarztText
        );
        ImpfstatusListSummaryDocMergerDTO impfstatusListSummaryDto = new ImpfstatusListSummaryDocMergerDTO(
            "Genuegend",
            "Empfehlung",
            "Entscheid"
        );
        UntersuchungArztDocMergerDTO arztDto = new UntersuchungArztDocMergerDTO(
            "auffällig",
            "140",
            "90",
            blutdruckText
        );

        AnlassMergeSource mergeSource = new AnlassMergeSource(
            new AnlassMetadatenDocMergerDTO(null, null, null, null),
            List.of(),
            impfstatusListSummaryDto,
            impfungDto,
            null,
            null,
            null,
            null,
            arztDto
        );
        MergeContext ctx = new MergeContext(mergeSource);

        // ACT & ASSERT - Single canonical keys resolve correctly
        assertThat(mergeSource.getData(ctx, "impfung.impfausweisNichtVorhanden"))
            .isEqualTo(impfausweisText);
        assertThat(mergeSource.getData(ctx, "impfung.massnahmeImpfungBeimHausarzt"))
            .isEqualTo(massnahmeHausarztText);
        assertThat(mergeSource.getData(ctx, "impfstatusListe.impfschutzGenuegend"))
            .isEqualTo("Genuegend");
        assertThat(mergeSource.getData(ctx, "impfstatusListe.impfempfehlung"))
            .isEqualTo("Empfehlung");
        assertThat(mergeSource.getData(ctx, "impfstatusListe.impfentscheid"))
            .isEqualTo("Entscheid");
        assertThat(mergeSource.getData(ctx, "untersuchungArzt.blutdruckAuffaellig"))
            .isEqualTo(blutdruckText);

        // ACT & ASSERT - Disallowed aliases / duplicate prefixes are NOT resolved
        assertThat(mergeSource.getData(ctx, "impfung.keinImpfausweis"))
            .isNull();
        assertThat(mergeSource.getData(ctx, "impfung.impfungBeimHausarzt"))
            .isNull();
        assertThat(mergeSource.getData(ctx, "impfstatus.impfschutzGenuegend"))
            .isEqualTo("[impfstatus.impfschutzGenuegend]");
        assertThat(mergeSource.getData(ctx, "impfstatus.impfausweisNichtVorhanden"))
            .isEqualTo("[impfstatus.impfausweisNichtVorhanden]");
        assertThat(mergeSource.getData(ctx, "impfstatus.massnahmeImpfungBeimHausarzt"))
            .isEqualTo("[impfstatus.massnahmeImpfungBeimHausarzt]");
        assertThat(mergeSource.getData(ctx, "impfungen.impfausweisNichtVorhanden"))
            .isEqualTo("[impfungen.impfausweisNichtVorhanden]");
        assertThat(mergeSource.getData(ctx, "untersuchungArzt.blutdruckEmpfehlung"))
            .isNull();
        assertThat(mergeSource.getData(ctx, "untersuchungArzt.blutdruckKontrolle"))
            .isNull();
        assertThat(mergeSource.getData(ctx, "arzt.blutdruckAuffaellig"))
            .isEqualTo("[arzt.blutdruckAuffaellig]");
    }
}
