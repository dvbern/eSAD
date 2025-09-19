package ch.dvbern.oss.hemed.esc.api.features.infomaterial;

import java.io.Serial;

import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import ch.dvbern.oss.hemed.esc.api.shared.mandant.TranslatedString;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hibernate.envers.Audited;

@Entity
@Table
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Infomaterial extends AbstractTenantEntity<Infomaterial> implements EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = -2616852567861298939L;

    @NonNull
    @NotNull
    @Valid
    @Embedded
    private TranslatedString name;

    @NotNull
    @Column(nullable = false)
    private boolean aktiv = true;
}
