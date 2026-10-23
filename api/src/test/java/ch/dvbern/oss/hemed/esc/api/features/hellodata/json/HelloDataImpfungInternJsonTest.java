package ch.dvbern.oss.hemed.esc.api.features.hellodata.json;

import java.time.LocalDate;
import java.util.UUID;

import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfStatus;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(ReplaceUnderscores.class)
class HelloDataImpfungInternJsonTest {

    @Test
    void constructor_parses_aggregated_diseases_string() {
        UUID id = UUID.randomUUID();
        UUID untersuchungId = UUID.randomUUID();
        UUID untersuchungImpfungenId = UUID.randomUUID();
        UUID impfstoffId = UUID.randomUUID();
        UUID anlassErfasstId = UUID.randomUUID();

        HelloDataImpfungInternJson json = new HelloDataImpfungInternJson(
            id,
            untersuchungId,
            untersuchungImpfungenId,
            impfstoffId,
            null,
            LocalDate.of(2025, 5, 10),
            ImpfStatus.DURCHGEFUEHRT,
            anlassErfasstId,
            null,
            "HEPATITIS_B:true;MASERN:false"
        );

        assertThat(json.getId()).isEqualTo(id);
        assertThat(json.getUntersuchungId()).isEqualTo(untersuchungId);
        assertThat(json.getUntersuchungImpfungenId()).isEqualTo(untersuchungImpfungenId);
        assertThat(json.getImpfstoffId()).isEqualTo(impfstoffId);
        assertThat(json.getDatumImpfung()).isEqualTo(LocalDate.of(2025, 5, 10));
        assertThat(json.getImpfStatus()).isEqualTo(ImpfStatus.DURCHGEFUEHRT);
        assertThat(json.getAnlassImpfungErfasstId()).isEqualTo(anlassErfasstId);

        assertThat(json.getKrankheitenImmunitaeten()).containsExactly(
            new HelloDataImpfungInternKrankheitImmunitaetJson(Krankheit.HEPATITIS_B, true),
            new HelloDataImpfungInternKrankheitImmunitaetJson(Krankheit.MASERN, false)
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   ", ";" })
    void constructor_handles_null_empty_or_blank_aggregated_diseases(String aggregated) {
        HelloDataImpfungInternJson json = new HelloDataImpfungInternJson(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            null,
            null,
            ImpfStatus.GEPLANT,
            UUID.randomUUID(),
            null,
            aggregated
        );

        assertThat(json.getKrankheitenImmunitaeten()).isEmpty();
    }
}
