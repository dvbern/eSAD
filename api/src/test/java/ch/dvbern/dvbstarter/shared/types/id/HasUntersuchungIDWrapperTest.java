package ch.dvbern.dvbstarter.shared.types.id;

import ch.dvbern.oss.hemed.esc.api.features.anlass.anonymereintrag.AnonymerEintrag;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class HasUntersuchungIDWrapperTest {

    private static final String SAMPLE_UUID = "3c2d0ee3-c07e-4e1e-96ef-00c8fee6a9a3";

    @Nested
    class FromString {

        @Test
        void plain_uuid_without_suffix_throws_IllegalArgumentException() {
            assertThatThrownBy(() -> HasUntersuchungIDWrapper.fromString(SAMPLE_UUID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("shortname");
        }

        @Test
        void empty_string_throws_IllegalArgumentException() {
            assertThatThrownBy(() -> HasUntersuchungIDWrapper.fromString(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("shortname");
        }

        @Test
        void uuid_with_einladung_suffix_resolves_to_Einladung() {
            HasUntersuchungIDWrapper wrapper = HasUntersuchungIDWrapper.fromString(SAMPLE_UUID + "-einladung");

            assertThat(wrapper.isAnonymerEintrag()).isFalse();
            assertThat(wrapper.isOfClass(Einladung.class)).isTrue();
            assertThat(wrapper.toExternalForm()).isEqualTo(SAMPLE_UUID + "-einladung");
        }

        @Test
        void uuid_with_anonym_suffix_resolves_to_AnonymerEintrag() {
            HasUntersuchungIDWrapper wrapper = HasUntersuchungIDWrapper.fromString(SAMPLE_UUID + "-anonym");

            assertThat(wrapper.isAnonymerEintrag()).isTrue();
            assertThat(wrapper.isOfClass(AnonymerEintrag.class)).isTrue();
            assertThat(wrapper.toExternalForm()).isEqualTo(SAMPLE_UUID + "-anonym");
        }

        @Test
        void unknown_suffix_throws_IllegalArgumentException_listing_known_shortnames() {
            assertThatThrownBy(() -> HasUntersuchungIDWrapper.fromString(SAMPLE_UUID + "-unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unknown")
                .hasMessageContaining("anonym")
                .hasMessageContaining("einladung");
        }

        @Test
        void empty_suffix_after_dash_throws_IllegalArgumentException() {
            assertThatThrownBy(() -> HasUntersuchungIDWrapper.fromString(SAMPLE_UUID + "-"))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void malformed_uuid_part_with_known_suffix_throws_IllegalArgumentException() {
            // First 36 characters are not a valid UUID; suffix is known.
            String malformedUuidPart = "ZZZZZZZZ-ZZZZ-ZZZZ-ZZZZ-ZZZZZZZZZZZZ";

            assertThatThrownBy(() -> HasUntersuchungIDWrapper.fromString(malformedUuidPart + "-einladung"))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void case_sensitive_shortname_unknown_uppercase_throws() {
            // Shortname is matched case-sensitively; uppercase should not be accepted.
            assertThatThrownBy(() -> HasUntersuchungIDWrapper.fromString(SAMPLE_UUID + "-EINLADUNG"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("EINLADUNG");
        }
    }

    @Nested
    class FromEinladungId {

        @Test
        void builds_wrapper_with_uuid_and_einladung_class() {
            ID<Einladung> source = ID.parse(SAMPLE_UUID, Einladung.class);

            HasUntersuchungIDWrapper wrapper = HasUntersuchungIDWrapper.fromEinladungId(source);

            assertThat(wrapper.isAnonymerEintrag()).isFalse();
            assertThat(wrapper.isOfClass(Einladung.class)).isTrue();
            assertThat(wrapper.toEinladungId()).isEqualTo(source);
        }
    }

    @Nested
    class FromAnonymerEintragId {

        @Test
        void builds_wrapper_with_uuid_and_anonymerEintrag_class() {
            ID<AnonymerEintrag> source = ID.parse(SAMPLE_UUID, AnonymerEintrag.class);

            HasUntersuchungIDWrapper wrapper = HasUntersuchungIDWrapper.fromAnonymerEintragId(source);

            assertThat(wrapper.isAnonymerEintrag()).isTrue();
            assertThat(wrapper.isOfClass(AnonymerEintrag.class)).isTrue();
            assertThat(wrapper.toAnonymerEintragId()).isEqualTo(source);
        }
    }

    @Nested
    class FromEinladungUuid {

        @Test
        void builds_wrapper_with_uuid_and_einladung_class() {
            HasUntersuchungIDWrapper wrapper = HasUntersuchungIDWrapper.fromEinladungUuid(SAMPLE_UUID);

            assertThat(wrapper.isOfClass(Einladung.class)).isTrue();
            assertThat(wrapper.isAnonymerEintrag()).isFalse();
            assertThat(wrapper.toEinladungId().getUUID()).isEqualTo(UUID.fromString(SAMPLE_UUID));
        }

        @Test
        void invalid_uuid_string_throws_IllegalArgumentException() {
            assertThatThrownBy(() -> HasUntersuchungIDWrapper.fromEinladungUuid("not-a-uuid"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class FromAnonymerEintragUuid {

        @Test
        void builds_wrapper_with_uuid_and_anonymerEintrag_class() {
            HasUntersuchungIDWrapper wrapper = HasUntersuchungIDWrapper.fromAnonymerEintragUuid(SAMPLE_UUID);

            assertThat(wrapper.isOfClass(AnonymerEintrag.class)).isTrue();
            assertThat(wrapper.isAnonymerEintrag()).isTrue();
            assertThat(wrapper.toAnonymerEintragId().getUUID()).isEqualTo(UUID.fromString(SAMPLE_UUID));
        }

        @Test
        void invalid_uuid_string_throws_IllegalArgumentException() {
            assertThatThrownBy(() -> HasUntersuchungIDWrapper.fromAnonymerEintragUuid("not-a-uuid"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class ToExternalForm {

        @Test
        void einladung_class_renders_with_einladung_suffix() {
            HasUntersuchungIDWrapper wrapper = HasUntersuchungIDWrapper.fromEinladungUuid(SAMPLE_UUID);

            assertThat(wrapper.toExternalForm()).isEqualTo(SAMPLE_UUID + "-einladung");
        }

        @Test
        void anonym_class_renders_with_anonym_suffix() {
            HasUntersuchungIDWrapper wrapper = HasUntersuchungIDWrapper.fromAnonymerEintragUuid(SAMPLE_UUID);

            assertThat(wrapper.toExternalForm()).isEqualTo(SAMPLE_UUID + "-anonym");
        }
    }

    @Nested
    class ToEinladungId {

        @Test
        void returns_ID_with_same_uuid_and_class_for_Einladung_wrapper() {
            HasUntersuchungIDWrapper wrapper = HasUntersuchungIDWrapper.fromEinladungUuid(SAMPLE_UUID);

            ID<Einladung> id = wrapper.toEinladungId();

            assertThat(id.getUUID()).isEqualTo(UUID.fromString(SAMPLE_UUID));
            assertThat(id.getEntityClazz()).isEqualTo(Einladung.class);
        }

        @Test
        void throws_IllegalStateException_for_AnonymerEintrag_wrapper() {
            HasUntersuchungIDWrapper wrapper = HasUntersuchungIDWrapper.fromAnonymerEintragUuid(SAMPLE_UUID);

            assertThatThrownBy(wrapper::toEinladungId)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(AnonymerEintrag.class.getName())
                .hasMessageContaining(Einladung.class.getName());
        }
    }

    @Nested
    class ToAnonymerEintragId {

        @Test
        void returns_ID_with_same_uuid_and_class_for_AnonymerEintrag_wrapper() {
            HasUntersuchungIDWrapper wrapper = HasUntersuchungIDWrapper.fromAnonymerEintragUuid(SAMPLE_UUID);

            ID<AnonymerEintrag> id = wrapper.toAnonymerEintragId();

            assertThat(id.getUUID()).isEqualTo(UUID.fromString(SAMPLE_UUID));
            assertThat(id.getEntityClazz()).isEqualTo(AnonymerEintrag.class);
        }

        @Test
        void throws_IllegalStateException_for_Einladung_wrapper() {
            HasUntersuchungIDWrapper wrapper = HasUntersuchungIDWrapper.fromEinladungUuid(SAMPLE_UUID);

            assertThatThrownBy(wrapper::toAnonymerEintragId)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(Einladung.class.getName())
                .hasMessageContaining(AnonymerEintrag.class.getName());
        }
    }

    @Nested
    class IsAnonymerEintrag {

        @Test
        void returns_true_for_AnonymerEintrag_wrapper() {
            HasUntersuchungIDWrapper wrapper = HasUntersuchungIDWrapper.fromAnonymerEintragUuid(SAMPLE_UUID);

            assertThat(wrapper.isAnonymerEintrag()).isTrue();
        }

        @Test
        void returns_false_for_Einladung_wrapper() {
            HasUntersuchungIDWrapper wrapper = HasUntersuchungIDWrapper.fromEinladungUuid(SAMPLE_UUID);

            assertThat(wrapper.isAnonymerEintrag()).isFalse();
        }
    }

    @Nested
    class IsOfClass {

        @Test
        void returns_true_when_class_matches_Einladung() {
            HasUntersuchungIDWrapper wrapper = HasUntersuchungIDWrapper.fromEinladungUuid(SAMPLE_UUID);

            assertThat(wrapper.isOfClass(Einladung.class)).isTrue();
            assertThat(wrapper.isOfClass(AnonymerEintrag.class)).isFalse();
        }

        @Test
        void returns_true_when_class_matches_AnonymerEintrag() {
            HasUntersuchungIDWrapper wrapper = HasUntersuchungIDWrapper.fromAnonymerEintragUuid(SAMPLE_UUID);

            assertThat(wrapper.isOfClass(AnonymerEintrag.class)).isTrue();
            assertThat(wrapper.isOfClass(Einladung.class)).isFalse();
        }
    }

    @Nested
    class RoundTrip {

        @Test
        void fromString_then_toExternalForm_preserves_einladung_value() {
            String input = SAMPLE_UUID + "-einladung";

            String roundTripped = HasUntersuchungIDWrapper.fromString(input).toExternalForm();

            assertThat(roundTripped).isEqualTo(input);
        }

        @Test
        void fromString_then_toExternalForm_preserves_anonym_value() {
            String input = SAMPLE_UUID + "-anonym";

            String roundTripped = HasUntersuchungIDWrapper.fromString(input).toExternalForm();

            assertThat(roundTripped).isEqualTo(input);
        }

        @Test
        void fromEinladungId_then_toEinladungId_preserves_original_id() {
            ID<Einladung> original = ID.parse(SAMPLE_UUID, Einladung.class);

            ID<Einladung> roundTripped = HasUntersuchungIDWrapper.fromEinladungId(original).toEinladungId();

            assertThat(roundTripped).isEqualTo(original);
        }

        @Test
        void fromAnonymerEintragId_then_toAnonymerEintragId_preserves_original_id() {
            ID<AnonymerEintrag> original = ID.parse(SAMPLE_UUID, AnonymerEintrag.class);

            ID<AnonymerEintrag> roundTripped =
                HasUntersuchungIDWrapper.fromAnonymerEintragId(original).toAnonymerEintragId();

            assertThat(roundTripped).isEqualTo(original);
        }

        @Test
        void toExternalForm_then_fromString_preserves_einladung_wrapper() {
            HasUntersuchungIDWrapper original = HasUntersuchungIDWrapper.fromEinladungUuid(SAMPLE_UUID);

            HasUntersuchungIDWrapper roundTripped = HasUntersuchungIDWrapper.fromString(original.toExternalForm());

            assertThat(roundTripped.toExternalForm()).isEqualTo(original.toExternalForm());
            assertThat(roundTripped.toEinladungId()).isEqualTo(original.toEinladungId());
        }

        @Test
        void toExternalForm_then_fromString_preserves_anonym_wrapper() {
            HasUntersuchungIDWrapper original = HasUntersuchungIDWrapper.fromAnonymerEintragUuid(SAMPLE_UUID);

            HasUntersuchungIDWrapper roundTripped = HasUntersuchungIDWrapper.fromString(original.toExternalForm());

            assertThat(roundTripped.toExternalForm()).isEqualTo(original.toExternalForm());
            assertThat(roundTripped.toAnonymerEintragId()).isEqualTo(original.toAnonymerEintragId());
        }
    }
}
