package ch.dvbern.oss.hemed.esc.api.features.anlass.anonymereintrag;

import java.io.Serial;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckAnlassTypAnonym;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.envers.Audited;

@CheckAnlassTypAnonym
@Entity
@Table(indexes = {
    @Index(name = "ix_anonymereintrag_anlass", columnList = "anlass_id, id"),
    @Index(name = "ix_anonymereintrag_untersuchung", columnList = "untersuchung_id, id")
})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Audited
public class AnonymerEintrag extends AbstractEntity<AnonymerEintrag> {

    @Serial
    private static final long serialVersionUID = -3883899846152390067L;

    @NotNull
    @NonNull
    @Column(length = DbConst.DB_DEFAULT_MAX_LENGTH, nullable = false)
    private String hash;

    @NotNull
    @NonNull
    @OneToOne(optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_anonymereintrag_anlass"), nullable = false)
    private Anlass anlass;

    @Nullable
    @OneToOne(optional = true)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_anonymereintrag_untersuchung"), nullable = true)
    private Untersuchung untersuchung;
}
