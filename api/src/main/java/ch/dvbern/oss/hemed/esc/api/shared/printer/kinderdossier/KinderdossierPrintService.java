package ch.dvbern.oss.hemed.esc.api.shared.printer.kinderdossier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
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
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheitService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungInternService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.AnlassWithDateTimeJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.DossierService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKindService;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizService;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.CurrentUserService;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfo;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreService;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintResultDTO;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintZipUtil;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static ch.dvbern.dvbstarter.infrastructure.fileio.FileNameUtil.DATE_FORMATTER_FOR_FILENAME;

@Service
@RequiredArgsConstructor
@Slf4j
public class KinderdossierPrintService {

    private final CurrentUserService currentUserService;
    private final KindService kindService;
    private final KontaktPersonKindService kontaktPersonKindService;
    private final NotizService notizService;
    private final DossierService dossierService;
    private final EinladungService einladungService;
    private final UntersuchungService untersuchungService;
    private final UntersuchungImpfstatusKrankheitService untersuchungImpfstatusKrankheitService;
    private final UntersuchungImpfungInternService untersuchungImpfungInternService;
    private final AnlassService anlassService;
    private final AppClock appClock;
    private final AppLanguageProvider appLanguageProvider;
    private final FileStoreService fileStoreService;
    private final TenantSettingsService tenantSettingsService;
    private final ConfigService configService;
    private final TenantContext tenantContext;
    private final TL tl;

    @NonNull
    public PrintResultDTO printKinderdossier(
        @NonNull KinderdossierPrintVariant variant,
        @NonNull ID<Kind> kindId,
        @Nullable ID<Anlass> singleAnlassId
    ) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Kind kind = kindService.getById(kindId);
            List<KontaktPersonKind> kontaktPersonKindList = kontaktPersonKindService.findAktivByKind(kindId);
            List<Notiz> notizList = notizService.findAllForKind(kindId)
                .stream()
                .sorted(Comparator.comparing(AbstractEntity::getTimestampErstellt))
                .toList()
                .reversed();

            List<Einladung> einladungList = new ArrayList<>();
            ID<Dossier> dossierIdByKind = dossierService.getDossierIdByKind(kindId);
            if (singleAnlassId == null) {
                einladungList = einladungService.findAllByDossier(dossierIdByKind);
            } else {
                Optional<Einladung> singleEinladung = einladungService.findByAnlassUndKind(singleAnlassId, kindId);
                if (singleEinladung.isPresent()) {
                    einladungList.add(singleEinladung.get());
                }
            }

            List<AnlassUntersuchungPrintDTO> anlassDTOList = new ArrayList<>();
            for (Einladung einladung : einladungList) {
                Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladung.getId());
                List<UntersuchungImpfstatusKrankheit> impfstatusList =
                    untersuchungImpfstatusKrankheitService.findByUntersuchung(untersuchung.getId());
                List<UntersuchungImpfungIntern> impfungInternList =
                    untersuchungImpfungInternService.findByUntersuchung(untersuchung.getId());
                List<AnlassWithDateTimeJson> folgeImpfAnlassList = anlassService.findScheduledAnlaesseOfTypeForKind(
                    List.of(AnlassTyp.IMPFSPRECHSTUNDE, AnlassTyp.IMPFAKTION, AnlassTyp.IMPFTERMIN),
                    kindId,
                    List.of(einladung.getAnlass().getId())
                );

                anlassDTOList.add(AnlassUntersuchungPrintDTO.ofEinladung(
                    einladung,
                    impfungInternList,
                    impfstatusList,
                    folgeImpfAnlassList));
            }

            KinderdossierPrintDTO dto = new KinderdossierPrintDTO(
                kind,
                kontaktPersonKindList,
                anlassDTOList,
                notizList,
                appClock.getToday(),
                tenantContext.get(),
                (AppLanguages) appLanguageProvider.current(),
                variant
            );

            logPrintOfKinderdossier(dto, variant, currentUserService.currentUserName());
            KinderdossierPrinter.print(dto, baos, fileStoreService, tenantSettingsService, configService);

            byte[] byteArray = baos.toByteArray();
            PrintResultDTO pdfPrintResult =
                new PrintResultDTO(byteArray, createFileInfo(kind, byteArray.length, "pdf"));

            // If there are any attachments, return a zip instead
            if (variant != KinderdossierPrintVariant.ANLAESSE
                && notizList.stream().anyMatch(notiz -> !notiz.getFiles().isEmpty())
            ) {
                List<Notiz> notizWithFiles = notizList.stream().filter(notiz -> !notiz.getFiles().isEmpty()).toList();
                byte[] zipContent = PrintZipUtil.zipPrintAndAttachments(
                    pdfPrintResult,
                    fileStoreService,
                    tl.translate("printer.attachments-prefix"),
                    notizWithFiles);
                return new PrintResultDTO(zipContent, createFileInfo(kind, zipContent.length, "zip"));
            }

            return pdfPrintResult;
        } catch (IOException e) {
            throw new AppFailureException(AppFailureMessage.internalError("Error when creating Kinderdossier PDF"), e);
        }
    }

    private void logPrintOfKinderdossier(
        @NonNull KinderdossierPrintDTO dto,
        @NonNull KinderdossierPrintVariant variant,
        @NonNull EmailAddress emailAddress
    ) {

        String template = "ESC-PDF-Print: Generation of Kinderdossier variant=${variant} "
            + "for kindId=${kindId} (${kindName}) and Anlaesse=[${anlassnames}] for username=${emailAddress}";
        Map<String, String> params = new HashMap<>();
        params.put("variant", variant.name());
        params.put("kindId", dto.getKind().getId().getUUID().toString());
        params.put("kindName", dto.getKind().getNameVorname());
        params.put(
            "anlassnames", dto.getAnlassDTOList().stream()
                .map(AnlassUntersuchungPrintDTO::getAnlass)
                .map(anlass -> anlass.getFullAnlassTitel(tl))
                .collect(Collectors.joining(",")));
        params.put("emailAddress", emailAddress.toString());

        String result = StringSubstitutor.replace(template, params, "${", "}");
        LOG.info(result);

    }

    @NonNull
    private FileInfo createFileInfo(@NonNull Kind kind, long size, String fileExtension) {
        String fileName = tl.translate(
            "printer.kinderdossier.filename",
            "nameVorname",
            PrintUtil.WHITESPACE.matcher(kind.getNameVorname()).replaceAll("-"),
            "now",
            appClock.getToday().format(DATE_FORMATTER_FOR_FILENAME),
            "fileExtension",
            fileExtension);

        return FileDownloadUtil.buildFileInfo(fileName, fileExtension, size);
    }
}
