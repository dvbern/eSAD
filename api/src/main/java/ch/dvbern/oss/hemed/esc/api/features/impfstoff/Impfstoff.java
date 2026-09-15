package ch.dvbern.oss.hemed.esc.api.features.impfstoff;

import java.io.Serial;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hibernate.envers.Audited;

@Entity
@Table(
    indexes = @Index(name = "ix_impfstoff_name", columnList = "name, id"),
    uniqueConstraints = @UniqueConstraint(name = "uc_impfstoff_name", columnNames = "name")
)
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Impfstoff extends AbstractEntity<Impfstoff> implements EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = 8005202370526715032L;

    @NotNull
    @NonNull
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = false)
    private String name;

    @NotNull
    @NonNull
    @Size(max = DbConst.DB_ENUM_LIST_LENGTH)
    @Column(nullable = false, length = DbConst.DB_ENUM_LIST_LENGTH)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String krankheiten;

    /**
     * Status of the Impfstoff in the eSad system
     */
    @NotNull
    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    private EntityAktivStatus impfstoffAktivStatus = EntityAktivStatus.AKTIV;

    public void setKrankheiten(@NonNull List<Krankheit> krankheiten) {
        Validate.notEmpty(krankheiten, "krankheiten list for Impfstoff must not be empty");
        this.krankheiten = Objects.requireNonNull(EnumSetUtil.toString(krankheiten));
    }

    @NonNull
    public EnumSet<Krankheit> getKrankheiten() {
        return EnumSetUtil.fromString(Krankheit.class, krankheiten);
    }
}
