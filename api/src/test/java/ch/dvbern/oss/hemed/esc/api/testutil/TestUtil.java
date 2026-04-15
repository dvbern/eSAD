package ch.dvbern.oss.hemed.esc.api.testutil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.function.Supplier;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.fileio.FileUtil;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.Util;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.BriefvorlageType;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.internal.mapping.Jackson2Mapper;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.apache.commons.text.RandomStringGenerator.Builder;
import org.assertj.core.api.Assertions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@UtilityClass
public class TestUtil {

    @NonNull
    public static <T extends AbstractEntity<T>> ID<T> parseIdFromLocation(
        @NonNull String location,
        @NonNull Class<T> entityClass
    ) {
        var id = location.substring(location.lastIndexOf('/') + 1);
        return ID.parse(id, entityClass);
    }

    public static io.restassured.mapper.ObjectMapper restAssuredObjectMapper(
        @NonNull ObjectMapper jacksonMapper
    ) {
        return new Jackson2Mapper((aClass, s) -> jacksonMapper);
    }

    @NonNull
    public static Path createTempFileFor(@NonNull Path path, @NonNull String testFixture) {
        try {
            var bytes = Util.readResource(path.toString());
            var baseName = FilenameUtils.getBaseName(testFixture);
            var suffix = '.' + FilenameUtils.getExtension(testFixture);
            var tempFile = File.createTempFile(baseName, suffix);

            tempFile.deleteOnExit();

            FileUtils.writeByteArrayToFile(tempFile, bytes);

            return tempFile.toPath();
        } catch (IOException e) {
            throw new RuntimeException("Problem reading/writing file: " + testFixture, e);
        }
    }

    @NonNull
    public static String getRandomString(@NonNull String prefix) {
        char[][] pairs = { { 'a', 'z' }, { 'A', 'Z' } };
        RandomStringGenerator generator = new Builder()
            .withinRange(pairs)
            .get();
        return prefix + '_' + generator.generate(10);
    }

    public static void setEntityInfo(@NonNull AbstractEntity<?> entity) {
        entity.setTimestampErstellt(OffsetDateTime.now().minusMonths(1));
        entity.setTimestampMutiert(OffsetDateTime.now().minusMonths(1));
        entity.setUserErstellt("Test");
        entity.setUserMutiert("Test");
    }

    @NonNull
    public static File loadTestSimpleEscadaFile() {
        String filePath = "/EscadaTest/testdata.csv";
        return loadTestfile(filePath);
    }

    @NonNull
    public static File loadComplexEscadaFile() {
        String filePath = "/EscadaTest/csv_pseudonymisiert.csv";
        return loadTestfile(filePath);
    }

    @NonNull
    public static File loadComplexPupilFile() {
        String filePath = "/PupilTest/csv_pseudonymisiert.csv";
        return loadTestfile(filePath);
    }

    @NonNull
    public static File loadTestPDF() {
        return loadTestfile("/FileUtilTest/pdfFileExample.pdf");
    }

    @NonNull
    public static File loadTestWord() {
        return loadTestfile("/DocMergerTest/all-properties.docx");
    }

    @NonNull
    public static File loadTestImage() {
        return loadTestfile("/FileUtilTest/logo.png");
    }

    @NonNull
    public static File loadBriefvorlageFile(@NonNull BriefvorlageType type) {
        if (type == BriefvorlageType.KIND) {
            return loadTestfile("/DocMergerTest/kind-properties.docx");
        }
        if (type == BriefvorlageType.ANLASS) {
            return loadTestfile("/DocMergerTest/all-properties.docx");
        }
        throw new IllegalArgumentException("Unknown type: " + type);
    }

    @NonNull
    public static File loadTestProblematicFilename() {
        return loadTestfile("/FileUtilTest/pro ble, mätic filéname.txt.jpg");
    }

    @NonNull
    public static File load15mbImgFile() {
        String portraitPath = "/FileUtilTest/15mb.jpg";
        return loadTestfile(portraitPath);
    }

    @NonNull
    public static File loadTextFile() {
        return loadTestfile("/FileUtilTest/sampletext.txt");
    }

    @NonNull
    public static File loadEmptyFile() {
        return loadTestfile("/FileUtilTest/Empty.csv");
    }

    @NonNull
    public static File loadTestfile(@NonNull String filePath) {
        try (InputStream inputStream = TestUtil.class.getResourceAsStream(filePath)) {
            Objects.requireNonNull(inputStream, filePath + " was not found");

            File tempFile =
                File.createTempFile(FilenameUtils.getBaseName(filePath), '.' + FilenameUtils.getExtension(filePath));
            tempFile.deleteOnExit();
            FileUtils.copyInputStreamToFile(inputStream, tempFile);
            return tempFile;
        } catch (IOException e) {
            throw new AppFailureException(AppFailureMessage.internalError("Could not load test file"), e);
        }
    }

    @NonNull
    public static File loadTestfileWithFilename(@NonNull String filePath, @NonNull String desiredFilename) {

        try (InputStream inputStream = TestUtil.class.getResourceAsStream(filePath)) {
            Objects.requireNonNull(inputStream, filePath + " was not found");

            File tempFile =
                File.createTempFile(desiredFilename, '.' + FilenameUtils.getExtension(filePath));
            tempFile.deleteOnExit();

            File f = new File(tempFile.getParent(), desiredFilename);
            f.deleteOnExit();

            FileUtils.copyInputStreamToFile(inputStream, f);
            return f;
        } catch (IOException e) {
            throw new AppFailureException(AppFailureMessage.internalError("Could not load test file"), e);
        }
    }

    public static FileUpload fileToFileUpload(@NonNull File file, @Nullable String originalFilename) {
        return new FileUpload() {
            @Override
            public MultivaluedMap<String, String> getHeaders() {
                return new MultivaluedHashMap<>();
            }

            @Override
            public String name() {
                return null == originalFilename ? file.getName() : originalFilename;
            }

            @Override
            public Path filePath() {
                return Path.of(file.getPath());
            }

            @Override
            public String fileName() {
                return file.getAbsoluteFile().getName();
            }

            @Override
            public long size() {
                return file.length();
            }

            @Override
            public String contentType() {
                return FileUtil.parseMediaTypeFromContent(Path.of(file.getPath())).toString();
            }

            @Override
            public String charSet() {
                return Charset.defaultCharset().name();
            }
        };
    }

    @SuppressWarnings("java:S2925" /* we could use Awaitability here but the gain seems marginal
    when considered as a tradeoff against an additional dependency*/)
    public static void waitForConditionOrAssertFails(
        @NonNull Supplier<Boolean> condition,
        int maxRetries,
        int waitInMillis,
        @NonNull String messageToAssertFailure
    ) throws InterruptedException {
        boolean success = false;
        for (int i = 0; i < maxRetries; i++) {
            if (condition.get()) {
                success = true;
                break;
            }
            Thread.sleep(waitInMillis);
        }

        Assertions.assertThat(success)
            .as(messageToAssertFailure)
            .isTrue();
    }
}
