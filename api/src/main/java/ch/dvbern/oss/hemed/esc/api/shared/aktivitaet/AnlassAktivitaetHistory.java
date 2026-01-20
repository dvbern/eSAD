package ch.dvbern.oss.hemed.esc.api.shared.aktivitaet;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(indexes = { @Index(name = "ix_anlassaktivitaethistory_anlass", columnList = "anlass_id, id"),
    @Index(name = "ix_anlassaktivitaethistory_usererstellt_timestamperstellt", columnList = "usererstellt, timestamperstellt, id"),})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AnlassAktivitaetHistory extends AbstractEntity<AnlassAktivitaetHistory> {

    @Serial
    private static final long serialVersionUID = 3519510810266363137L;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_anlassaktivitaethistory_anlass"))
    private Anlass anlass;
}
