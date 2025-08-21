package ch.dvbern.oss.hemed.esc.api.features.schule.quartier;

import java.io.Serial;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hibernate.envers.Audited;

@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(name = "uc_quartier_quartiername", columnNames = "quartierName")
})
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Quartier extends AbstractTenantEntity<Quartier> {

    @Serial
    private static final long serialVersionUID = -8993998933687829415L;

    @NonNull
    @NotNull
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = false)
    private String quartierName;

}
