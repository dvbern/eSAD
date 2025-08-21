package ch.dvbern.oss.hemed.esc.api.features.schule.klasse;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ExternalImportInfo;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.HasExternalImportInfo;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.Lehrperson;
import ch.dvbern.oss.hemed.esc.api.features.schule.types.Klassenstufe;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.envers.Audited;

@Entity
@Table(indexes = {
    @Index(name = "ix_klasse_schule", columnList = "schule_id, id"),
    @Index(name = "ix_klasse_bezeichnung", columnList = "bezeichnung, id"),
    @Index(name = "ix_klasse_klassenstufe", columnList = "klassenstufe, id"),
})
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Klasse extends AbstractTenantEntity<Klasse> implements Serializable, HasExternalImportInfo {

    @Serial
    private static final long serialVersionUID = 1741625114973564294L;

    @Embedded
    @Valid
    @Nullable
    private ExternalImportInfo externalImportInfo;

    @NotNull
    @NonNull
    @ManyToOne(optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_klasse_schule"), nullable = false, updatable = true)
    private Schule schule;

    // (owning side)
    @NotNull
    @NonNull
    @ManyToMany
    @JoinTable(name = "Klasse_Lehrperson",
        joinColumns = @JoinColumn(
            name = "klasse_id",
            foreignKey = @ForeignKey(name = "fk_klasse_lehrperson_klasse")),
        inverseJoinColumns = @JoinColumn(
            name = "lehrperson_id",
            foreignKey = @ForeignKey(name = "fk_klasse_lehrperson_lehrperson")),
        indexes = {
            @Index(name = "ix_klasse_lehrperson_klasse",
                columnList = "klasse_id, lehrperson_id"),
            @Index(name = "ix_klasse_lehrperson_lehrperson",
                columnList = "lehrperson_id, klasse_id"),
        }
    )
    private List<Lehrperson> lehrpersonen;

    @NotNull
    @NonNull
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = false)
    private String bezeichnung;

    @NotNull
    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    private Klassenstufe klassenstufe;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_DEFAULT_MAX_LENGTH)
    private String klasseSchulart; // von Escada z.B. KG, BM, SEKE etc

    /**
     * Status of the Klasse in the eSad system
     */
    @NotNull
    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    private EntityAktivStatus klasseAktivStatus = EntityAktivStatus.AKTIV;

    @Transient
    @Nullable
    public String getExternalSystemIdOrNull() {
        if (externalImportInfo == null) {
            return null;
        }
        return externalImportInfo.getIdInExternalSystem();
    }

    @Transient
    public boolean isManualOrAktivInExternalSystem() {
        return externalImportInfo == null || externalImportInfo.isAktivInExternalSystem();
    }

    public void addLehrperson(@NonNull Lehrperson lpToAdd) {
        if (!lehrpersonen.contains(lpToAdd)) {
            lehrpersonen.add(lpToAdd);
        }
    }

    public void removeLehrperson(@NonNull Lehrperson lpToRemove) {
        lehrpersonen.remove(lpToRemove);
    }
}
