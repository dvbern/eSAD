package ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumberConverter;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ExternalImportInfo;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.HasExternalImportInfo;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.types.KontaktPersonKindBeziehung;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import ch.dvbern.oss.hemed.esc.api.shared.person.Adresse;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
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
@Table
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KontaktPersonKind
        extends AbstractTenantEntity<KontaktPersonKind>
        implements Serializable, HasExternalImportInfo, EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = -2616852567861298939L;

    @Embedded
    @Valid
    @Nullable
    private ExternalImportInfo externalImportInfo;

    @NotNull
    @NonNull
    @ManyToMany
    @JoinTable(name = "Kind_KontaktPersonKind",
        joinColumns = @JoinColumn(
            name = "kontaktpersonkind_id",
            foreignKey = @ForeignKey(name = "fk_kind_kontaktpersonkind_kontaktpersonkind")),
        inverseJoinColumns = @JoinColumn(
            name = "kind_id",
            foreignKey = @ForeignKey(name = "fk_kind_kontaktpersonkind_kind")),
        indexes = {
            @Index(name = "ix_kind_kontaktpersonkind_kind",
                columnList = "kind_id, kontaktpersonkind_id"),
            @Index(name = "ix_kind_kontaktpersonkind_kontaktpersonkind",
                columnList = "kontaktpersonkind_id, kind_id"),
        }
    )
    private Set<Kind> kinder;

    @NotNull
    @NonNull
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = false)
    private String name;

    @Nullable
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = true)
    private String vorname;

    @NotNull
    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    private Geschlecht geschlecht;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = DbConst.DB_ENUM_LENGTH)
    private KontaktPersonKindBeziehung beziehung;

    @Valid
    @Embedded
    @Nullable
    private Adresse adresse;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_DEFAULT_MAX_LENGTH)
    private EmailAddress email;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Convert(converter = NormalizedPhoneNumberConverter.class)
    private NormalizedPhoneNumber telefon;

    @NotNull
    @Column(nullable = false)
    private boolean korrespondenz;

    @NonNull
    public String getNameVorname() {
        if(vorname != null) {
            return name + ' ' + vorname;
        }
        return name;
    }
}
