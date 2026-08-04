package ch.dvbern.oss.hemed.esc.api.shared.printer.anonymer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.fileio.FileDownloadUtil;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguageProvider;
import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anonymereintrag.AnonymerEintrag;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anonymereintrag.AnonymerEintragService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheitService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungInternService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfo;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreService;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintResultDTO;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import ch.dvbern.oss.hemed.esc.api.shared.printer.kinderdossier.AnlassUntersuchungPrintDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

import static ch.dvbern.dvbstarter.infrastructure.fileio.FileNameUtil.DATE_FORMATTER_FOR_FILENAME;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnonymerEintragPrintService {

    private final AnonymerEintragService anonymerEintragService;
    private final UntersuchungImpfstatusKrankheitService untersuchungImpfstatusKrankheitService;
    private final UntersuchungImpfungInternService untersuchungImpfungInternService;
    private final AppClock appClock;
    private final AppLanguageProvider appLanguageProvider;
    private final TenantProducer tenantProducer;
    private final FileStoreService fileStoreService;
    private final TL tl;


    @NonNull
    public PrintResultDTO printAnonymerEintrag(@NonNull ID<AnonymerEintrag> anonymerEintragId) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            AnonymerEintrag anonymerEintrag = anonymerEintragService.getById(anonymerEintragId);
            Anlass anlass = anonymerEintrag.getAnlass();
            Untersuchung untersuchung = anonymerEintrag.getUntersuchung();

            List<UntersuchungImpfstatusKrankheit> impfstatusList = List.of();
            List<UntersuchungImpfungIntern> impfungInternList = List.of();
            if (untersuchung != null) {
                impfstatusList = untersuchungImpfstatusKrankheitService.findByUntersuchung(untersuchung.getId());
                impfungInternList = untersuchungImpfungInternService.findByUntersuchung(untersuchung.getId());
            }

            AnlassUntersuchungPrintDTO anlassDTO = AnlassUntersuchungPrintDTO.anonymous(
                anlass,
                untersuchung,
                anonymerEintrag.getHash(),
                impfungInternList,
                impfstatusList,
                List.of());

            AnonymerEintragPrintDTO dto = new AnonymerEintragPrintDTO(
                anonymerEintrag.getHash(),
                List.of(anlassDTO),
                appClock.getToday(),
                tenantProducer.produceTenant(),
                (AppLanguages) appLanguageProvider.current()
            );

            AnonymerEintragPrinter.print(dto, baos, fileStoreService);

            byte[] byteArray = baos.toByteArray();
            return new PrintResultDTO(byteArray, createFileInfo(anonymerEintrag.getHash(), byteArray.length, "pdf"));
        } catch (IOException e) {
            throw new AppFailureException(
                AppFailureMessage.internalError("Error when creating anonymer Eintrag PDF"), e);
        }
    }

    @NonNull
    private FileInfo createFileInfo(@NonNull String hash, long size, String fileExtension) {
        String fileName = tl.translate(
            "printer.anonymer-eintrag.filename",
            "hash",
            PrintUtil.WHITESPACE.matcher(hash).replaceAll("-"),
            "now",
            appClock.getToday().format(DATE_FORMATTER_FOR_FILENAME),
            "fileExtension",
            fileExtension);

        return FileDownloadUtil.buildFileInfo(fileName, fileExtension, size);
    }
}
