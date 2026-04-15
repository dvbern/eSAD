package ch.dvbern.oss.hemed.esc.api.features.anlass.signatur;

import java.io.Serial;
import java.time.LocalDateTime;

import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

@Entity
@Table(indexes = @Index(name = "ix_untersuchungsignatur_benutzer", columnList = "person_id, id"))
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UntersuchungSignatur extends AbstractEntity<UntersuchungSignatur> implements EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = 7537304981837013143L;

    @NotNull
    @NonNull
    @OneToOne(optional = false, mappedBy = "untersuchungSignatur")
    private Untersuchung untersuchung;

    @Nullable
    @ManyToOne(optional = true)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchungsignatur_benutzer"), nullable = true)
    private Benutzer person;

    @Nullable
    @Column(nullable = true)
    private LocalDateTime timestampVisiert;

    @NonNull
    public static UntersuchungSignatur forUntersuchung(@NonNull Untersuchung untersuchung) {
        UntersuchungSignatur signatur = new UntersuchungSignatur();
        signatur.setUntersuchung(untersuchung);
        untersuchung.setUntersuchungSignatur(signatur);
        return signatur;
    }
}
