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

package ch.dvbern.oss.hemed.esc.api.shared.printer.anlass;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.dvbstarter.infrastructure.fileio.FileDownloadUtil;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguageProvider;
import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungInternService;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.Impfstoff;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Nachkontrolle;
import ch.dvbern.oss.hemed.esc.api.features.kinder.NachkontrolleService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.CurrentUserService;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfo;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreService;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintResultDTO;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.checkerframework.checker.nullness.qual.NonNull;

import static ch.dvbern.dvbstarter.infrastructure.fileio.FileNameUtil.DATE_FORMATTER_FOR_FILENAME;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnlassPrintService {

    private final CurrentUserService currentUserService;
    private final AnlassService anlassService;
    private final EinladungService einladungService;
    private final NachkontrolleService nachkontrolleService;
    private final AppClock appClock;
    private final AppLanguageProvider appLanguageProvider;
    private final FileStoreService fileStoreService;
    private final TenantContext tenantContext;
    private final TL tl;
    private final TenantSettingsService tenantSettingService;
    private final ConfigService configService;
    private final UntersuchungImpfungInternService untersuchungImpfungInternService;

    @NonNull
    public PrintResultDTO printAnlass(@NonNull ID<Anlass> anlassId, @NonNull AnlassPrintVariant variant) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Anlass anlass = anlassService.getById(anlassId);
            requirePrintVariantEnabled(anlass, variant);
            List<Nachkontrolle> nachkontrollen = new ArrayList<>();
            List<Einladung> einladungen = einladungService.findByAnlass(anlassId);
            einladungen.stream()
                .map(einladung -> nachkontrolleService.findByDossier(einladung.getDossier().getId()))
                .forEach(nachkontrolleOptional -> nachkontrolleOptional.ifPresent(nachkontrollen::add));

            Map<Einladung, List<Impfstoff>> geplanteImpfstoffeByEinladung = variant == AnlassPrintVariant.IMPFEN
                ? buildGeplanteImpfstoffeByEinladung(einladungen)
                : null;

            AnlassPlanungPrintDTO dto = new AnlassPlanungPrintDTO(
                anlass,
                einladungen,
                appClock.getToday(),
                tenantContext.get(),
                (AppLanguages) appLanguageProvider.current(),
                nachkontrollen,
                variant,
                geplanteImpfstoffeByEinladung
            );

            logPrintOfAnlass(dto, currentUserService.currentUserName());
            AnlassPlanungPrinter.print(dto, baos, fileStoreService, tenantSettingService, configService);

            byte[] byteArray = baos.toByteArray();
            return new PrintResultDTO(byteArray, createFileInfo(anlass, byteArray.length, "pdf"));
        } catch (IOException e) {
            throw new AppFailureException(AppFailureMessage.internalError("Error when creating Anlass PDF"), e);
        }
    }

    private void requirePrintVariantEnabled(@NonNull Anlass anlass, @NonNull AnlassPrintVariant variant) {
        AnlassConfig anlassConfig = anlass.getAnlassConfig();
        if (variant == AnlassPrintVariant.IMPFEN && !anlassConfig.isShowAnlassPrintImpfen()) {
            LOG.warn("There was an attempt to print an 'Anlass Print for geplante Impfungen' but the setting is disabled, aborting.");
            throw new AppValidationException(AppValidationMessage.anlassPrintVariantDisabled(variant));
        }
    }

    @NonNull
    private Map<Einladung, List<Impfstoff>> buildGeplanteImpfstoffeByEinladung(@NonNull List<Einladung> einladungen) {
        Map<Einladung, List<Impfstoff>> result = new HashMap<>();
        for (Einladung einladung : einladungen) {
            result.put(einladung, AnlassImpfenUtil.getGeplanteImpfstoffe(einladung, untersuchungImpfungInternService));
        }
        return result;
    }

    private void logPrintOfAnlass(
        @NonNull AnlassPlanungPrintDTO dto,
        @NonNull EmailAddress emailAddress
    ) {
        String anlassTitle = dto.getAnlass().getFullAnlassTitel(tl);
        String template = "ESC-PDF-Print: Printing of Anlass with title=${anlassTitle} "
            + "for anlassId=${anlassId} with ${einladungenCount} invitations for username=${emailAddress}";
        final Map<String, String> params = Map.of(
            "anlassTitle", anlassTitle,
            "anlassId", dto.getAnlass().getId().getUUID().toString(),
            "einladungenCount", String.valueOf(dto.getEinladungen().size()),
            "emailAddress", emailAddress.toString()
        );

        String result = StringSubstitutor.replace(template, params, "${", "}");
        LOG.info(result);
    }

    @NonNull
    private FileInfo createFileInfo(@NonNull Anlass anlass, long size, @NonNull String fileExtension) {
        String fileName = tl.translate(
            "printer.anlass.filename",
            "titel",
            PrintUtil.FILENAME_UNSAFE_CHARS.matcher(anlass.getFullAnlassTitel(tl)).replaceAll("-"),
            "now",
            appClock.getToday().format(DATE_FORMATTER_FOR_FILENAME),
            "fileExtension",
            fileExtension);

        return FileDownloadUtil.buildFileInfo(fileName, fileExtension, size);
    }

}
