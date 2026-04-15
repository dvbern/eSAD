package ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.briefgenerierung;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import ch.dvbern.dvbstarter.infrastructure.fileio.FileDownloadUtil;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.cleanfilename.CleanFileName;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.arzt.Arzt;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.Briefvorlage;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.briefgenerierung.docmerger.MergeSourceCreatorService;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.briefgenerierung.docmerger.RootMergeSource;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.docmerger.DocMergerUtil;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfo;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreService;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintResultDTO;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import ch.dvbern.oss.hemed.esc.api.shared.validator.MergeSourceDummyDataCreator;
import lombok.RequiredArgsConstructor;
import org.apache.tika.mime.MediaType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static ch.dvbern.dvbstarter.infrastructure.fileio.FileNameUtil.DATE_FORMATTER_FOR_FILENAME;

@Service
@RequiredArgsConstructor
public class BriefgenerierungService {

    private final MergeSourceCreatorService mergeSourceCreatorService;
    private final FileStoreService fileStoreService;
    private final BenutzerService benutzerService;
    private final AppClock appClock;

    @NonNull
    public PrintResultDTO generateBriefForKind(
        @NonNull Briefvorlage briefvorlage,
        @NonNull Kind kind,
        @Nullable Arzt arzt
    ) {
        Benutzer currentBenutzer = benutzerService.getCurrentBenutzerOrThrowException();
        return generateBriefForKind(briefvorlage, kind, arzt, currentBenutzer);
    }

    @NonNull
    public PrintResultDTO generateBriefForKind(
        @NonNull Briefvorlage briefvorlage,
        @NonNull Kind kind,
        @Nullable Arzt arzt,
        @NonNull Benutzer benutzer
    ) {
        RootMergeSource mergeSource = mergeSourceCreatorService.fromKind(kind, arzt, benutzer);
        return printBrief(briefvorlage, kind, mergeSource);
    }

    @NonNull
    public PrintResultDTO generateBriefForAnlass(
        @NonNull Briefvorlage briefvorlage,
        @NonNull Einladung einladung,
        @Nullable Arzt arzt
    ) {
        RootMergeSource mergeSource = mergeSourceCreatorService.fromAnlass(einladung, arzt);
        return printBrief(briefvorlage, einladung.getDossier().getKind(), mergeSource);
    }

    @NonNull
    public PrintResultDTO generateTestBrief(@NonNull Briefvorlage briefvorlage) {
        Kind kind = MergeSourceDummyDataCreator.getTestKind(appClock.offsetDateTime().now());
        RootMergeSource mergeSource = switch (briefvorlage.getType()) {
            case ANLASS -> mergeSourceCreatorService.testMergeSourceForAnlass(kind);
            case KIND -> mergeSourceCreatorService.testMergeSourceForKind(kind);
        };
        return printBrief(briefvorlage, kind, mergeSource);
    }

    @NonNull
    private PrintResultDTO printBrief(
        @NonNull Briefvorlage briefvorlage,
        @NonNull Kind kind,
        @NonNull RootMergeSource mergeSource
    ) {
        byte[] briefvorlageBytes =
            fileStoreService.getFileBytesSync(briefvorlage.getFile().getFileInfoBucket().getFileIdInBucket());

        InputStream inputStream = new ByteArrayInputStream(briefvorlageBytes);

        // Generate the docx
        byte[] result = DocMergerUtil.merge(inputStream, mergeSource);
        return new PrintResultDTO(result, createFileInfo(briefvorlage, kind, result.length));
    }

    @NonNull
    private FileInfo createFileInfo(
        @NonNull Briefvorlage briefvorlage,
        @NonNull Kind kind,
        long size
    ) {
        String fileName = String.format(
            "%s_%s_%s.docx",
            PrintUtil.WHITESPACE.matcher(briefvorlage.getName()).replaceAll("-"),
            PrintUtil.WHITESPACE.matcher(kind.getNameVorname()).replaceAll("-"),
            appClock.getToday().format(DATE_FORMATTER_FOR_FILENAME));

        return FileInfo.of(
            CleanFileName.parse(fileName),
            MediaType.application(FileDownloadUtil.MIME_TYPE_DOCX),
            size
        );
    }
}
