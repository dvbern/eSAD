package ch.dvbern.oss.hemed.esc.api.shared.anlass.anlassflags;

import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Embeddable
@Getter
@Setter
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AnlassFlags implements Serializable {

    @Serial
    private static final long serialVersionUID = -8389886912784253120L;

    @NotNull
    @Column(nullable = false)
    private boolean nachkontrolleSehen = false;

    @NotNull
    @Column(nullable = false)
    private boolean nachkontrolleHoeren = false;

    @NotNull
    @Column(nullable = false)
    private boolean nachkontrolleGroesseGewicht = false;

    @NotNull
    @Column(nullable = false)
    private boolean nachkontrolleSprache = false;

    @NotNull
    @Column(nullable = false)
    private boolean nachkontrolleMotorik = false;

    @NotNull
    @Column(nullable = false)
    private boolean nachkontrolleAnderes = false;


    public boolean hasAnyNachkontrolle() {
        return nachkontrolleSehen
            || nachkontrolleHoeren
            || nachkontrolleGroesseGewicht
            || nachkontrolleSprache
            || nachkontrolleMotorik
            || nachkontrolleAnderes;
    }
}
