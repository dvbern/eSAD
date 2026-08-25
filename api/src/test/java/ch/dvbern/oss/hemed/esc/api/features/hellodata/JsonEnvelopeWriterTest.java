package ch.dvbern.oss.hemed.esc.api.features.hellodata;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataMetaJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataSentinelJson;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The envelope is framed by hand - braces, commas and the trailing {@code _meta} are written as
 * the rows come off the cursor, because that is what lets it stream. Hand-written JSON has
 * exactly one interesting failure mode: a row count where the separators come out wrong.
 * <p>
 * {@code HelloDataResourceITest} covers the endpoint against the real database, but the BE test
 * data always contains Schulen, so it can never reach the <b>empty</b> case - and an export of
 * zero rows is precisely where a stray comma or a missing bracket would produce invalid JSON.
 * Hence this plain unit test: no Quarkus, just the writer and a parser.
 */
@DisplayNameGeneration(ReplaceUnderscores.class)
class JsonEnvelopeWriterTest {

    private static final HelloDataSentinelJson SENTINEL = new HelloDataSentinelJson(
        new HelloDataMetaJson(
            HelloDataMetaJson.STATUS_COMPLETE,
            0,
            // parse instead of now(): the app's clock is irrelevant here, only the framing is
            // under test
            OffsetDateTime.parse("2026-01-01T02:15:00+01:00")));

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private final JsonEnvelopeWriter writer = new JsonEnvelopeWriter(objectMapper);

    @Test
    void an_export_without_rows_is_still_valid_json() {
        JsonNode envelope = parse(write(List.of()));

        assertThat(envelope.get(JsonEnvelopeWriter.DATA_KEY).isArray()).isTrue();
        assertThat(envelope.get(JsonEnvelopeWriter.DATA_KEY)).isEmpty();
        assertThat(envelope.get(HelloDataSentinelJson.META_KEY).get("status").asText())
            .as("even an empty export must be recognisable as complete rather than truncated")
            .isEqualTo(HelloDataMetaJson.STATUS_COMPLETE);
    }

    @Test
    void a_single_row_gets_no_separator() {
        JsonNode envelope = parse(write(List.of(new Row("a"))));

        assertThat(envelope.get(JsonEnvelopeWriter.DATA_KEY)).hasSize(1);
        assertThat(envelope.get(JsonEnvelopeWriter.DATA_KEY).get(0).get("value").asText()).isEqualTo("a");
    }

    @Test
    void rows_are_separated_and_keep_their_order() {
        JsonNode envelope = parse(write(List.of(new Row("a"), new Row("b"), new Row("c"))));

        JsonNode data = envelope.get(JsonEnvelopeWriter.DATA_KEY);
        assertThat(data).hasSize(3);
        assertThat(data.get(0).get("value").asText()).isEqualTo("a");
        assertThat(data.get(1).get("value").asText()).isEqualTo("b");
        assertThat(data.get(2).get("value").asText()).isEqualTo("c");
    }

    @Test
    void the_sentinel_is_the_last_thing_written() {
        String json = write(List.of(new Row("a")));

        assertThat(json)
            .as("rowCount is only known once the cursor is exhausted, so _meta cannot come first")
            .containsSubsequence(JsonEnvelopeWriter.DATA_KEY, HelloDataSentinelJson.META_KEY)
            .endsWith("}");
    }

    @SneakyThrows
    private String write(List<Row> rows) {
        var os = new ByteArrayOutputStream();

        writer.writeStart(os);
        long rowNumber = 0;
        for (Row row : rows) {
            rowNumber++;
            writer.writeRow(os, row, rowNumber);
        }
        writer.writeEnd(os, SENTINEL);

        return os.toString(StandardCharsets.UTF_8);
    }

    @SneakyThrows
    private JsonNode parse(String json) {
        return objectMapper.readTree(json);
    }

    /** Stand-in for a row DTO - the writer does not care what it serialises. */
    private record Row(String value) {
    }
}
