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
import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungImpfstatusKrankheit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungImpfstatusKrankheitService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungImpfungInternService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.DossierService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKindService;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizService;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.CurrentUserService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfo;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreService;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintResultDTO;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintZipUtil;
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
    private final AppClock appClock;
    private final AppLanguageProvider appLanguageProvider;
    private final FileStoreService fileStoreService;
    private final TL tl;

    @NonNull
    public PrintResultDTO printKinderdossier(
        @NonNull KinderdossierPrintVariant variation,
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

            List<KinderdossierAnlassPrintDTO> anlassDTOList = new ArrayList<>();
            for (Einladung einladung : einladungList) {
                Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladung.getId());
                List<UntersuchungImpfstatusKrankheit> impfstatusList =
                    untersuchungImpfstatusKrankheitService.findByUntersuchung(untersuchung.getId());
                List<UntersuchungImpfungIntern> impfungInternList =
                    untersuchungImpfungInternService.findByUntersuchung(untersuchung.getId());

                anlassDTOList.add(new KinderdossierAnlassPrintDTO(einladung, impfungInternList, impfstatusList));
            }

            KinderdossierPrintDTO dto = new KinderdossierPrintDTO(
                kind,
                kontaktPersonKindList,
                anlassDTOList,
                notizList,
                appClock.localDateTime().now().toLocalDate(),
                TenantEnum.BS, // Use tenant resolver once we found a way around SonarCube's parameter limitation //
                (AppLanguages) appLanguageProvider.current(),
                variation
            );

            logPrintOfKinderdossier(dto, variation, currentUserService.currentUserName());
            KinderdossierPrinter.print(dto, baos, fileStoreService);

            byte[] byteArray = baos.toByteArray();
            PrintResultDTO pdfPrintResult =
                new PrintResultDTO(byteArray, createFileInfo(kind, byteArray.length, "pdf"));

            // If there are any attachments, return a zip instead
            if (variation != KinderdossierPrintVariant.ANLAESSE
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
        @NonNull KinderdossierPrintVariant variation,
        @NonNull EmailAddress emailAddress
    ) {

        String template = "ESC-PDF-Print: Generation of Kinderdossier variation=${variation} "
            + "for kindId=${kindId} (${kindName}) and Anlaesse=[${anlassnames}] for username=${emailAddress}";
        Map<String, String> params = new HashMap<>();
        params.put("variation", variation.name());
        params.put("kindId", dto.getKind().getId().getUUID().toString());
        params.put("kindName", dto.getKind().getNameVorname());
        params.put(
            "anlassnames", dto.getAnlassDTOList().stream()
                .map(KinderdossierAnlassPrintDTO::getEinladung)
                .map(Einladung::getAnlass)
                .map(Anlass::getTitel)
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
            appClock.localDateTime().now().toLocalDate().format(DATE_FORMATTER_FOR_FILENAME),
            "fileExtension",
            fileExtension);

        return FileDownloadUtil.buildFileInfo(fileName, fileExtension, size);
    }
}
