/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.shared.printer.anlass;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguageProvider;
import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungInternService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.features.kinder.NachkontrolleService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.CurrentUserService;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreService;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintResultDTO;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.DossierBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.EinladungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.ImpfstoffBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KindBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.TenantSettingBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungInternBuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AnlassPrintServiceTest {

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private AnlassService anlassService;

    @Mock
    private EinladungService einladungService;

    @Mock
    private NachkontrolleService nachkontrolleService;

    @Mock
    private AppClock appClock;

    @Mock
    private AppLanguageProvider appLanguageProvider;

    @Mock
    private FileStoreService fileStoreService;

    @Mock
    private TenantContext tenantContext;

    @Mock
    private TL tl;

    @Mock
    private TenantSettingsService tenantSettingService;

    @Mock
    private ConfigService configService;

    @Mock
    private UntersuchungImpfungInternService untersuchungImpfungInternService;

    @InjectMocks
    private AnlassPrintService anlassPrintService;

    @BeforeEach
    void setUp() {
        when(appClock.getToday()).thenReturn(LocalDate.of(2024, 1, 1));
        when(appLanguageProvider.current()).thenReturn(AppLanguages.DE);
        when(tenantContext.get()).thenReturn(TenantEnum.BE);
        when(currentUserService.currentUserName()).thenReturn(EmailAddress.fromString("test@example.com"));
        // only used by the ALLGEMEIN variant's "eingeladene Kinder" table (esadId feature check)
        lenient().when(tenantSettingService.getTenantSetting()).thenReturn(TenantSettingBuilder.defaultTenantSetting());
        when(tenantSettingService.isTenantHeaderInExportsEnabled()).thenReturn(true);
        // used by Anlass.getFullAnlassTitel(tl) when logging the print action
        when(tl.translate(anyString())).thenReturn("Anlasstyp");
        when(tl.translate(
            eq("printer.anlass.filename"),
            anyString(),
            any(Serializable.class),
            anyString(),
            any(Serializable.class),
            anyString(),
            any(Serializable.class)
        ))
            .thenReturn("Anlass.pdf");
    }

    @Test
    void printAnlass_IMPFEN_looks_up_geplante_Impfstoffe_and_renders_PDF() {
        // ARRANGE
        Anlass anlass = AnlassBuilder.defaultAnlass(LocalDate.of(2020, 1, 1));
        Untersuchung untersuchung = UntersuchungBuilder.create(builder -> {
        });
        Einladung einladung = EinladungBuilder.create(builder -> builder
            .withAnlass(anlass)
            .withUntersuchung(untersuchung)
            .withDossier(DossierBuilder.create(dossierBuilder -> dossierBuilder
                .forKind(KindBuilder.create(kindBuilder -> kindBuilder.withName("Mustermann", "Anna"))))));

        UntersuchungImpfungIntern impfungGeplant = UntersuchungImpfungInternBuilder.create(builder -> builder
            .withImpfStatus(ImpfStatus.GEPLANT)
            .withImpfstoff(ImpfstoffBuilder.create(
                impfstoffBuilder -> impfstoffBuilder.forKrankheiten(Krankheit.MASERN))));

        when(anlassService.getById(anlass.getId())).thenReturn(anlass);
        when(einladungService.findByAnlass(anlass.getId())).thenReturn(List.of(einladung));
        when(nachkontrolleService.findByDossier(any())).thenReturn(Optional.empty());
        when(untersuchungImpfungInternService.findByUntersuchung(untersuchung.getId()))
            .thenReturn(List.of(impfungGeplant));

        // ACT
        PrintResultDTO result = anlassPrintService.printAnlass(anlass.getId(), AnlassPrintVariant.IMPFEN);

        // ASSERT: the planned Impfstoffe are looked up (variant == IMPFEN branch) and the PDF is rendered
        assertThat(result.getContent())
            .isNotEmpty();
        verify(untersuchungImpfungInternService).findByUntersuchung(untersuchung.getId());
    }

    @Test
    void printAnlass_ALLGEMEIN_does_not_look_up_geplante_Impfstoffe() {
        // ARRANGE
        Anlass anlass = AnlassBuilder.defaultAnlass(LocalDate.of(2020, 1, 1));
        Einladung einladung = EinladungBuilder.create(builder -> builder.withAnlass(anlass));

        when(anlassService.getById(anlass.getId())).thenReturn(anlass);
        when(einladungService.findByAnlass(anlass.getId())).thenReturn(List.of(einladung));
        when(nachkontrolleService.findByDossier(any())).thenReturn(Optional.empty());

        // ACT
        PrintResultDTO result = anlassPrintService.printAnlass(anlass.getId(), AnlassPrintVariant.ALLGEMEIN);

        // ASSERT: for the non-IMPFEN variant, the planned Impfstoffe are never looked up
        assertThat(result.getContent())
            .isNotEmpty();
        verifyNoInteractions(untersuchungImpfungInternService);
    }
}
