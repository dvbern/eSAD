package ch.dvbern.oss.hemed.esc.api.shared.aktivitaet;

import java.io.Serial;

import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(indexes = {
    @Index(name = "ix_kindaktivitaethistory_kind", columnList = "kind_id, id"),
    @Index(name = "ix_kindaktivitaethistory_usererstellt_timestamperstellt", columnList = "usererstellt, timestamperstellt, id"),
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KindAktivitaetHistory extends AbstractEntity<KindAktivitaetHistory> {

    @Serial
    private static final long serialVersionUID = 3519510810266363137L;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_kindaktivitaethistory_kind"))
    private Kind kind;
}
