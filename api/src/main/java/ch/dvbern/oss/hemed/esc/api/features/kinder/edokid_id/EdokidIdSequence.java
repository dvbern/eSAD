package ch.dvbern.oss.hemed.esc.api.features.kinder.edokid_id;

import java.io.Serial;
import java.io.Serializable;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Table(
    uniqueConstraints = {
        @UniqueConstraint(name = "uc_edokididsequence_eintrittsjahr", columnNames = { "eintrittsJahr", "tenant" })
    }
)
public class EdokidIdSequence extends AbstractTenantEntity<EdokidIdSequence> implements Serializable {

    private static final char MAX_CHARACTER = 'Z';
    private static final int MAX_LAUFNUMMER = 999;
    private static final int STEP_INCREMENT = 1;

    @Serial
    private static final long serialVersionUID = 7991093682253093241L;

    @Min(24)// Because the the ID was introduced in 2024
    @Max(99)
    @NotNull
    @Column(nullable = false)
    private int eintrittsJahr;

    @NotNull
    @Column(nullable = false)
    private char character;

    @Min(1)
    @Max(999)
    @NotNull
    @Column(nullable = false)
    private int laufnummer;

    public void increment() {
        if (this.getCharacter() == MAX_CHARACTER &&
            this.getLaufnummer() == MAX_LAUFNUMMER) {
            throw new AppFailureException(AppFailureMessage.edokidIdLastLaufnummerReached());
        }
        if (this.getLaufnummer() == MAX_LAUFNUMMER) {
            character = ((char) (character + STEP_INCREMENT));
            laufnummer = STEP_INCREMENT;
        } else {
           laufnummer = laufnummer + STEP_INCREMENT;
        }
    }

   public String getFormattedLaufnummer() {
        return String.format("%03d", this.getLaufnummer());
    }

}
