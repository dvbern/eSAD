package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import ch.dvbern.dvbstarter.shared.types.cleanfilename.CleanFileName;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfo;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileTyp;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import lombok.RequiredArgsConstructor;
import org.apache.tika.mime.MediaType;
import org.assertj.core.util.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.OffsetDateTime;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class FileDataBuilder {

    private final FileData fileData;

    private FileDataBuilder() {
        this.fileData = defaultImgFileData();
        TestUtil.setEntityInfo(fileData);
    }

    @NonNull
    public static FileData defaultPdfFileData() {
        FileInfo fileInfo = FileInfo.of(new CleanFileName("pdf.pdf"), MediaType.application("pdf"), 100);
        fileInfo.setFileIdInBucket("pdf");
        FileData fileData = new FileData(
            FileTyp.NOTIZUPLOAD,
            new CleanFileName(TestUtil.getRandomString("FileName")),
            fileInfo
        );

        fileData.setTimestampErstellt(OffsetDateTime.now());
        return fileData;
    }

    @NonNull
    public static FileData defaultWordFileData() {
        FileInfo fileInfo = FileInfo.of(
            new CleanFileName("word.docx"),
            MediaType.application("vnd.openxmlformats-officedocument.wordprocessingml.document"),
            100);
        fileInfo.setFileIdInBucket("word");
        FileData fileData = new FileData(
            FileTyp.BRIEFVORLAGE,
            new CleanFileName(TestUtil.getRandomString("FileName")),
            fileInfo
        );

        fileData.setTimestampErstellt(OffsetDateTime.now());
        return fileData;
    }

    @NonNull
    public static FileData defaultImgFileData() {
        FileInfo fileInfo = FileInfo.of(new CleanFileName("picture.jpg"), MediaType.image("jpeg"), 100);
        fileInfo.setFileIdInBucket("picture");
        FileData fileData = new FileData(
            FileTyp.NOTIZUPLOAD,
            new CleanFileName(TestUtil.getRandomString("FileName")),
            fileInfo
        );

        fileData.setTimestampErstellt(OffsetDateTime.now());
        return fileData;
    }

    @NonNull
    @CanIgnoreReturnValue
    public FileDataBuilder withId(@NonNull ID<FileData> id) {
        this.fileData.setId(id);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public FileDataBuilder withFileName(@NonNull String fileName) {
        this.fileData.getFileInfoBucket().setFileName(new CleanFileName(fileName));
        this.fileData.setCalculatedFileName(new CleanFileName("calculatedfilename_" + fileName));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public FileDataBuilder withFileIdInBucket(@NonNull String fileIdInBucket) {
        this.fileData.getFileInfoBucket().setFileIdInBucket(fileIdInBucket);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public FileDataBuilder withMimeType(@NonNull MediaType mimeType) {
        this.fileData.getFileInfoBucket().setMediaType(mimeType);
        return this;
    }

    @NonNull
    public static FileData create(@NonNull Consumer<FileDataBuilder> block) {
        FileDataBuilder builder = new FileDataBuilder();
        block.accept(builder);
        return builder.fileData;
    }
}
