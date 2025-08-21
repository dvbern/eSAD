package ch.dvbern.oss.hemed.esc.api.features.impfstoff;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.ImpfstoffBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ImpfstoffTest {

    @Test
    void setKrankheiten_is_empty_if_empty() {
        var impfstoff = ImpfstoffBuilder.defaultImpfstoff();
        impfstoff.setKrankheiten(List.of());
        Assertions.assertThat(impfstoff.getKrankheiten()).isEmpty();
    }

    @Test
    void setKrankheiten_is_set() {
        var impfstoff = ImpfstoffBuilder.defaultImpfstoff();
        impfstoff.setKrankheiten(List.of(Krankheit.MUMPS));
        Assertions.assertThat(impfstoff.getKrankheiten()).isNotEmpty().hasSize(1).contains(Krankheit.MUMPS);
        impfstoff.setKrankheiten(List.of(Krankheit.MUMPS, Krankheit.MASERN));

        impfstoff.setKrankheiten(List.of(
            Krankheit.MUMPS,
            Krankheit.MASERN,
            Krankheit.HPV,
            Krankheit.POLIO,
            Krankheit.DIPHTERIE,
            Krankheit.TETANUS,
            Krankheit.PERTUSSIS,
            Krankheit.HEPATITIS_B,
            Krankheit.VARIZELLEN,
            Krankheit.ROETELN,
            Krankheit.MENINGOKOKKEN_A_C_W_Y,
            Krankheit.MENINGOKOKKEN_B));
        Assertions.assertThat(impfstoff.getKrankheiten())
            .isNotEmpty()
            .hasSize(12)
            .contains(
                Krankheit.MUMPS,
                Krankheit.HPV,
                Krankheit.POLIO,
                Krankheit.DIPHTERIE,
                Krankheit.TETANUS,
                Krankheit.PERTUSSIS,
                Krankheit.HEPATITIS_B,
                Krankheit.VARIZELLEN,
                Krankheit.ROETELN,
                Krankheit.MENINGOKOKKEN_A_C_W_Y,
                Krankheit.MENINGOKOKKEN_B);
    }

}
