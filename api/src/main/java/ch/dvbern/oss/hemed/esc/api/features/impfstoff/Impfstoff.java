package ch.dvbern.oss.hemed.esc.api.features.impfstoff;

import java.io.Serial;
import java.util.List;
import java.util.stream.Stream;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
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
import org.apache.commons.lang3.StringUtils;
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
public class Impfstoff extends AbstractTenantEntity<Impfstoff> implements EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = 8005202370526715032L;

    @NotNull
    @NonNull
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = false)
    private String name;

    @NotNull
    @NonNull
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = false)
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
        this.krankheiten = krankheiten.stream()
            .map(Krankheit::name)
            .reduce((a, b) -> a + ", " + b)
            .orElse("");
    }

    @NonNull
    public List<Krankheit> getKrankheiten() {
        return Stream.of(krankheiten.split(","))
            .map(StringUtils::trim)
            .filter(StringUtils::isNotBlank)
            .map(Krankheit::valueOf)
            .toList();
    }
}
