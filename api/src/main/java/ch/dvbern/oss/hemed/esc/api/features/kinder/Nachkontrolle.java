package ch.dvbern.oss.hemed.esc.api.features.kinder;

import java.io.Serial;

import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import ch.dvbern.oss.hemed.esc.api.shared.anlass.anlassflags.AnlassFlags;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
@Table(
    indexes = {
        @Index(name = "ix_nachkontrolle_dossier_id", columnList = "dossier_id, id"),
    }
)
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Nachkontrolle extends AbstractTenantEntity<Nachkontrolle> {

    @Serial
    private static final long serialVersionUID = -8389886912784253120L;

    @NonNull
    @NotNull
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_nachkontrolle_dossier"), nullable = false)
    private Dossier dossier;

    @Valid
    @Embedded
    @NonNull
    @NotNull
    private AnlassFlags anlassFlags;

    @NonNull
    public static Nachkontrolle forDossier(@NonNull Dossier dossier) {
        return new Nachkontrolle(
            dossier,
            new AnlassFlags()
        );
    }
}
