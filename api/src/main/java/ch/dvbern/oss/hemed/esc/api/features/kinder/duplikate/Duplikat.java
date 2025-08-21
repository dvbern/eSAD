package ch.dvbern.oss.hemed.esc.api.features.kinder.duplikate;

import java.io.Serial;
import java.util.UUID;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.envers.Audited;

/**
 * This Entity is used to represent matches in our duplicate detection algorithm.
 * it contains the ids of the two kinds that are considered duplicates.
 * It also contains the status of the match (the match can be resolved)
 * and the kind objects that are considered duplicates. If two entities are merged one of them may be null
 * hence the two foreign keys are nullabl but we keep the original UUIDs that had the match for auditing reasons
 */
@Entity
@Table(
    indexes = {
        @Index(name = "ix_duplikat_kind_kind1", columnList = "kind1_id, id"),
        @Index(name = "ix_duplikat_kind_kind2", columnList = "kind2_id, id"),
    }
)
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Duplikat extends AbstractTenantEntity<Duplikat> {

    @Serial
    private static final long serialVersionUID = 835643576794254070L;

    @NonNull
    @NotNull
    @Column(nullable = false)
    private UUID idKind1;

    @NonNull
    @NotNull
    @Column(nullable = false)
    private UUID idKind2;

    @Nullable
    @ManyToOne(optional = true)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_duplikat_kind_kind1"), nullable = true)
    private Kind kind1;

    @Nullable
    @ManyToOne(optional = true)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_duplikat_kind_kind2"), nullable = true)
    private Kind kind2;

    @NotNull
    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    private DuplikatStatus duplikatStatus = DuplikatStatus.OFFEN;

    public static Duplikat forKinder(
        @NonNull Kind kind1,
        @NonNull Kind kind2
    ) {
        return new Duplikat(
                kind1.getId().getUUID(),
                kind2.getId().getUUID(),
                kind1,
                kind2,
                DuplikatStatus.OFFEN
        );
    }
}
