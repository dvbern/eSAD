package ch.dvbern.oss.hemed.esc.api.features.hellodata;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.TreeSet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This Test helps us to create openapi.yaml that we give HelloData.
 * Derives the contract handed to HelloData from the generated OpenAPI document and writes it to
 * {@value #HELLODATA_SPEC_PATH}.
 * <p>
 * HelloData needs a machine-readable contract to generate their ingestion models from, but the
 * full document describes several hundred internal schemas that are none of their business.
 * Deriving the reduced document instead of maintaining it by hand is what keeps the two from
 * drifting apart.
 * <p>
 * <b>This test writes into the source tree on purpose.</b> Regenerating is then just running the
 * test, and reviewing is just {@code git diff} - no copying from {@code target/}. It still fails
 * whenever it had to change something, so a stale contract cannot pass CI unnoticed: run it once
 * more after committing and it goes green.
 * <p>
 * <b>Refreshing the input:</b> {@value #FULL_SPEC_PATH} is itself generated, by
 * {@code ./mvnw compile quarkus:build}. Note the {@code compile} - {@code quarkus:build} alone
 * augments whatever is already in {@code target/classes} and will silently produce a spec
 * without your change.
 * <p>
 * <b>Limit of this guard:</b> it compares the reduced document against the checked-in full
 * document, not against the code. Change a DTO without refreshing the full spec and this test
 * still passes - it is the second half of the chain, not the whole of it.
 */
@DisplayNameGeneration(ReplaceUnderscores.class)
class HelloDataOpenApiContractTest {

    static final String FULL_SPEC_PATH = "openapi-schema/openapi.yaml";
    static final String HELLODATA_SPEC_PATH = "openapi-schema/hellodata-openapi.yaml";

    private static final Path FULL_SPEC = Path.of(FULL_SPEC_PATH);
    private static final Path HELLODATA_SPEC = Path.of(HELLODATA_SPEC_PATH);

    private static final String HELLA_DATA_PATH_PREFIX = "/api/hellodata";
    private static final String SCHEMA_REF_PREFIX = "#/components/schemas/";

    private final ObjectMapper yaml = new ObjectMapper(
        new YAMLFactory()
            .enable(Feature.MINIMIZE_QUOTES)
            .disable(Feature.WRITE_DOC_START_MARKER));

    @Test
    void hellodata_contract_is_up_to_date() throws IOException {
        String previous = Files.exists(HELLODATA_SPEC)
            ? Files.readString(HELLODATA_SPEC, StandardCharsets.UTF_8)
            : null;

        String current = yaml.writerWithDefaultPrettyPrinter()
            .writeValueAsString(reduceToHelloData(yaml.readTree(FULL_SPEC.toFile())));

        if (!current.equals(previous)) {
            Files.writeString(HELLODATA_SPEC, current, StandardCharsets.UTF_8);
        }

        assertThat(current)
            .as("""
                The HelloData contract changed and %s has been rewritten in place.
                Review it with 'git diff %s' and commit it - this file is what HelloData \
                generates their ingestion models from, so a change here is a change to an \
                external contract.
                If the diff is not what you expected, refresh the input first: \
                './mvnw compile quarkus:build'.""",
                HELLODATA_SPEC_PATH,
                HELLODATA_SPEC_PATH)
            .isEqualTo(previous);
    }

    @Test
    void hellodata_contract_exposes_only_hellodata_paths() throws IOException {
        ObjectNode reduced = reduceToHelloData(yaml.readTree(FULL_SPEC.toFile()));

        assertThat(reduced.get("paths").properties())
            .as("no internal endpoint may leak into the contract handed to HelloData")
            .allSatisfy(entry -> assertThat(entry.getKey()).startsWith(HELLA_DATA_PATH_PREFIX));

        assertThat(reduced.get("paths").size())
            .as("the export endpoints must be present")
            .isPositive();
    }

    /**
     * Keeps the HelloData paths plus the transitive closure of the schemas they reference.
     */
    private ObjectNode reduceToHelloData(JsonNode full) {
        ObjectNode reduced = yaml.createObjectNode();
        reduced.set("openapi", full.get("openapi"));

        // Drop info.version on purpose: HelloData only cares about actual business changes to
        // the contract, not about our SNAPSHOT version bumps. Keeping the version would add a
        // noisy diff to this externally-reviewed file on every release.
        ObjectNode info = ((ObjectNode) full.get("info")).deepCopy();
        info.remove("version");
        reduced.set("info", info);

        ObjectNode paths = reduced.putObject("paths");
        Iterator<String> pathNames = full.get("paths").fieldNames();
        while (pathNames.hasNext()) {
            String pathName = pathNames.next();
            if (pathName.startsWith(HELLA_DATA_PATH_PREFIX)) {
                paths.set(pathName, full.get("paths").get(pathName));
            }
        }

        JsonNode allSchemas = full.get("components").get("schemas");
        ObjectNode components = reduced.putObject("components");
        ObjectNode schemas = components.putObject("schemas");
        // TreeSet: stable ordering, so the generated file diffs cleanly
        for (String schemaName : collectReferencedSchemas(paths, allSchemas)) {
            schemas.set(schemaName, allSchemas.get(schemaName));
        }
        components.set("securitySchemes", full.get("components").get("securitySchemes"));

        return reduced;
    }

    private TreeSet<String> collectReferencedSchemas(JsonNode paths, JsonNode allSchemas) {
        TreeSet<String> found = new TreeSet<>();
        Deque<JsonNode> pending = new ArrayDeque<>();
        pending.push(paths);

        while (!pending.isEmpty()) {
            for (JsonNode ref : pending.pop().findValues("$ref")) {
                String value = ref.asText();
                if (value.startsWith(SCHEMA_REF_PREFIX)) {
                    String name = value.substring(SCHEMA_REF_PREFIX.length());
                    // only follow a schema the first time it is seen, otherwise a cyclic
                    // reference would loop forever
                    if (found.add(name) && allSchemas.has(name)) {
                        pending.push(allSchemas.get(name));
                    }
                }
            }
        }
        return found;
    }
}
