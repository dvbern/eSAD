package ch.dvbern.oss.hemed.esc.api.shared.util;

import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AnlassTitelUtilTest {

    private final TL tl = message -> "IMPFTERMIN";

    @Test
    void appends_titel_when_present() {
        var actual = AnlassTitelUtil.getFullAnlassTitel("testanlass", 2025, AnlassTyp.IMPFTERMIN, tl);

        assertThat(actual)
            .isEqualTo("2025/26 IMPFTERMIN testanlass");
    }

    @Test
    void omits_titel_when_null() {
        var actual = AnlassTitelUtil.getFullAnlassTitel(null, 2025, AnlassTyp.IMPFTERMIN, tl);

        assertThat(actual)
            .isEqualTo("2025/26 IMPFTERMIN");
    }

    @Test
    void omits_titel_when_blank() {
        var actual = AnlassTitelUtil.getFullAnlassTitel("   ", 2025, AnlassTyp.IMPFTERMIN, tl);

        assertThat(actual)
            .isEqualTo("2025/26 IMPFTERMIN");
    }

    @Test
    void strips_surrounding_whitespace_from_titel() {
        var actual = AnlassTitelUtil.getFullAnlassTitel("  testanlass  ", 2025, AnlassTyp.IMPFTERMIN, tl);

        assertThat(actual)
            .isEqualTo("2025/26 IMPFTERMIN testanlass");
    }

    @Test
    void formats_schuljahr_across_century_boundary() {
        var actual = AnlassTitelUtil.getFullAnlassTitel("titel", 2099, AnlassTyp.IMPFTERMIN, tl);

        assertThat(actual)
            .isEqualTo("2099/00 IMPFTERMIN titel");
    }
}
