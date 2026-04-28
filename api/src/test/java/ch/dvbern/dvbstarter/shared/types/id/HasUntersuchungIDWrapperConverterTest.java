package ch.dvbern.dvbstarter.shared.types.id;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HasUntersuchungIDWrapperConverterTest {

    private static final String SAMPLE_UUID = "3c2d0ee3-c07e-4e1e-96ef-00c8fee6a9a3";

    @AllArgsConstructor
    @NoArgsConstructor
    @Setter
    @Getter
    public static class JsonWithWrapper {
        private HasUntersuchungIDWrapper wrappedId;
    }

    @Nested
    class JpaConverter {

        private HasUntersuchungIDWrapperConverter converter;

        @BeforeEach
        void setUp() {
            converter = new HasUntersuchungIDWrapperConverter();
        }

        @Test
        void convertToDatabaseColumn_null_returns_null() {
            assertThat(converter.convertToDatabaseColumn(null)).isNull();
        }

        @Test
        void convertToEntityAttribute_null_returns_null() {
            assertThat(converter.convertToEntityAttribute(null)).isNull();
        }

        @Test
        void convertToDatabaseColumn_einladung_stores_uuid_with_einladung_suffix() {
            HasUntersuchungIDWrapper wrapper = HasUntersuchungIDWrapper.fromString(SAMPLE_UUID + "-einladung");

            assertThat(converter.convertToDatabaseColumn(wrapper)).isEqualTo(SAMPLE_UUID + "-einladung");
        }

        @Test
        void convertToDatabaseColumn_anonym_stores_uuid_with_anonym_suffix() {
            HasUntersuchungIDWrapper wrapper = HasUntersuchungIDWrapper.fromString(SAMPLE_UUID + "-anonym");

            assertThat(converter.convertToDatabaseColumn(wrapper)).isEqualTo(SAMPLE_UUID + "-anonym");
        }

        @Test
        void convertToEntityAttribute_plain_uuid_throws_IllegalArgumentException() {
            assertThatThrownBy(() -> converter.convertToEntityAttribute(SAMPLE_UUID))
                .isInstanceOf(IllegalArgumentException.class)
                .rootCause()
                .hasMessageContaining("shortname");
        }

        @Test
        void convertToEntityAttribute_einladung_suffix_resolves_to_einladung() {
            HasUntersuchungIDWrapper wrapper = converter.convertToEntityAttribute(SAMPLE_UUID + "-einladung");

            assertThat(wrapper).isNotNull();
            assertThat(wrapper.isAnonymerEintrag()).isFalse();
            assertThat(wrapper.toExternalForm()).isEqualTo(SAMPLE_UUID + "-einladung");
        }

        @Test
        void convertToEntityAttribute_anonym_suffix_resolves_to_anonymerEintrag() {
            HasUntersuchungIDWrapper wrapper = converter.convertToEntityAttribute(SAMPLE_UUID + "-anonym");

            assertThat(wrapper).isNotNull();
            assertThat(wrapper.isAnonymerEintrag()).isTrue();
            assertThat(wrapper.toExternalForm()).isEqualTo(SAMPLE_UUID + "-anonym");
        }

        @Test
        void roundtrip_einladung() {
            HasUntersuchungIDWrapper original = HasUntersuchungIDWrapper.fromString(SAMPLE_UUID + "-einladung");
            String dbValue = converter.convertToDatabaseColumn(original);
            HasUntersuchungIDWrapper restored = converter.convertToEntityAttribute(dbValue);

            assertThat(restored).isNotNull();
            assertThat(restored.toExternalForm()).isEqualTo(original.toExternalForm());
            assertThat(restored.isAnonymerEintrag()).isEqualTo(original.isAnonymerEintrag());
        }

        @Test
        void roundtrip_anonym() {
            HasUntersuchungIDWrapper original = HasUntersuchungIDWrapper.fromString(SAMPLE_UUID + "-anonym");
            String dbValue = converter.convertToDatabaseColumn(original);
            HasUntersuchungIDWrapper restored = converter.convertToEntityAttribute(dbValue);

            assertThat(restored).isNotNull();
            assertThat(restored.toExternalForm()).isEqualTo(original.toExternalForm());
            assertThat(restored.isAnonymerEintrag()).isEqualTo(original.isAnonymerEintrag());
        }
    }

    @Nested
    class JacksonConverter {

        private ObjectMapper mapper;

        @BeforeEach
        void setUp() {
            mapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            HasUntersuchungIDWrapperConverter.registerJackson(module);
            mapper.registerModule(module);
        }

        @Test
        void deserialize_plain_uuid_throws_JsonMappingException() {
            String json = "{\"wrappedId\":\"" + SAMPLE_UUID + "\"}";

            assertThatThrownBy(() -> mapper.readValue(json, JsonWithWrapper.class))
                .isInstanceOf(JsonProcessingException.class);
        }

        @Test
        void deserialize_anonym_suffix_resolves_to_anonymerEintrag() throws JsonProcessingException {
            String json = "{\"wrappedId\":\"" + SAMPLE_UUID + "-anonym\"}";

            JsonWithWrapper result = mapper.readValue(json, JsonWithWrapper.class);

            assertThat(result.getWrappedId()).isNotNull();
            assertThat(result.getWrappedId().isAnonymerEintrag()).isTrue();
            assertThat(result.getWrappedId().toExternalForm()).isEqualTo(SAMPLE_UUID + "-anonym");
        }

        @Test
        void serialize_einladung_writes_uuid_with_einladung_suffix() throws JsonProcessingException {
            JsonWithWrapper wrapper = new JsonWithWrapper(HasUntersuchungIDWrapper.fromString(SAMPLE_UUID + "-einladung"));

            String json = mapper.writeValueAsString(wrapper);

            assertThat(json).isEqualTo("{\"wrappedId\":\"" + SAMPLE_UUID + "-einladung\"}");
        }

        @Test
        void serialize_anonym_writes_uuid_with_anonym_suffix() throws JsonProcessingException {
            JsonWithWrapper wrapper = new JsonWithWrapper(HasUntersuchungIDWrapper.fromString(SAMPLE_UUID + "-anonym"));

            String json = mapper.writeValueAsString(wrapper);

            assertThat(json).isEqualTo("{\"wrappedId\":\"" + SAMPLE_UUID + "-anonym\"}");
        }

        @Test
        void roundtrip_einladung() throws JsonProcessingException {
            UUID uuid = UUID.randomUUID();
            String original = "{\"wrappedId\":\"" + uuid + "-einladung\"}";

            JsonWithWrapper result = mapper.readValue(original, JsonWithWrapper.class);
            String serialized = mapper.writeValueAsString(result);

            assertThat(serialized).isEqualTo(original);
        }

        @Test
        void roundtrip_anonym() throws JsonProcessingException {
            UUID uuid = UUID.randomUUID();
            String original = "{\"wrappedId\":\"" + uuid + "-anonym\"}";

            JsonWithWrapper result = mapper.readValue(original, JsonWithWrapper.class);
            String serialized = mapper.writeValueAsString(result);

            assertThat(serialized).isEqualTo(original);
        }
    }
}
