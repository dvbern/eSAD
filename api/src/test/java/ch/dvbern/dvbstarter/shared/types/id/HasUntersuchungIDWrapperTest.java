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
        void plain_uuid_throws_IllegalArgumentException() {
            assertThatThrownBy(() -> HasUntersuchungIDWrapper.fromString(SAMPLE_UUID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("shortname");
        }

        @Test
        void uuid_with_einladung_suffix_resolves_to_Einladung() {
            HasUntersuchungIDWrapper wrapper = HasUntersuchungIDWrapper.fromString(SAMPLE_UUID + "-einladung");

            assertThat(wrapper.isAnonymerEintrag()).isFalse();
            assertThat(wrapper.toExternalForm()).isEqualTo(SAMPLE_UUID + "-einladung");
        }

        @Test
        void uuid_with_anonym_suffix_resolves_to_AnonymerEintrag() {
            HasUntersuchungIDWrapper wrapper = HasUntersuchungIDWrapper.fromString(SAMPLE_UUID + "-anonym");

            assertThat(wrapper.isAnonymerEintrag()).isTrue();
            assertThat(wrapper.toExternalForm()).isEqualTo(SAMPLE_UUID + "-anonym");
        }

        @Test
        void unknown_suffix_throws_IllegalArgumentException() {
            assertThatThrownBy(() -> HasUntersuchungIDWrapper.fromString(SAMPLE_UUID + "-unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unknown");
        }
    }

    @Nested
    class ToExternalForm {

        @Test
        void einladung_class_renders_with_einladung_suffix() {
            HasUntersuchungIDWrapper wrapper = new HasUntersuchungIDWrapper(UUID.fromString(SAMPLE_UUID), Einladung.class);

            assertThat(wrapper.toExternalForm()).isEqualTo(SAMPLE_UUID + "-einladung");
        }

        @Test
        void anonym_class_renders_with_anonym_suffix() {
            HasUntersuchungIDWrapper wrapper = new HasUntersuchungIDWrapper(UUID.fromString(SAMPLE_UUID), AnonymerEintrag.class);

            assertThat(wrapper.toExternalForm()).isEqualTo(SAMPLE_UUID + "-anonym");
        }
    }

    @Nested
    class ToEinladungId {

        @Test
        void returns_ID_for_Einladung_wrapper() {
            HasUntersuchungIDWrapper wrapper = new HasUntersuchungIDWrapper(UUID.fromString(SAMPLE_UUID), Einladung.class);

            ID<Einladung> id = wrapper.toEinladungId();

            assertThat(id.getUUID()).isEqualTo(UUID.fromString(SAMPLE_UUID));
        }

        @Test
        void throws_IllegalStateException_for_AnonymerEintrag_wrapper() {
            HasUntersuchungIDWrapper wrapper = new HasUntersuchungIDWrapper(UUID.fromString(SAMPLE_UUID), AnonymerEintrag.class);

            assertThatThrownBy(wrapper::toEinladungId)
                .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    class ToAbstractEntityId {

        @Test
        void returns_ID_for_AnonymerEintrag_wrapper() {
            HasUntersuchungIDWrapper wrapper = new HasUntersuchungIDWrapper(UUID.fromString(SAMPLE_UUID), AnonymerEintrag.class);

            ID<AnonymerEintrag> id = wrapper.toAbstractEntityId();

            assertThat(id.getUUID()).isEqualTo(UUID.fromString(SAMPLE_UUID));
        }

        @Test
        void throws_IllegalStateException_for_Einladung_wrapper() {
            HasUntersuchungIDWrapper wrapper = new HasUntersuchungIDWrapper(UUID.fromString(SAMPLE_UUID), Einladung.class);

            assertThatThrownBy(wrapper::toAbstractEntityId)
                .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    class IsAnonymerEintrag {

        @Test
        void returns_true_for_AnonymerEintrag() {
            HasUntersuchungIDWrapper wrapper = HasUntersuchungIDWrapper.fromString(SAMPLE_UUID + "-anonym");

            assertThat(wrapper.isAnonymerEintrag()).isTrue();
        }

        @Test
        void returns_false_for_Einladung() {
            HasUntersuchungIDWrapper wrapper = HasUntersuchungIDWrapper.fromString(SAMPLE_UUID + "-einladung");

            assertThat(wrapper.isAnonymerEintrag()).isFalse();
        }
    }
}
