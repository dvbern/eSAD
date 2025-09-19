package ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ExternalImportInfo;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.HasExternalImportInfo;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class Lehrperson
    extends AbstractTenantEntity<Lehrperson>
    implements Serializable, HasExternalImportInfo, EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = -2616852567861298939L;

    @Embedded
    @Valid
    @Nullable
    private ExternalImportInfo externalImportInfo;

    @NotNull
    @NonNull
    @ManyToMany(mappedBy = "lehrpersonen")
    private Set<Klasse> klassen;

    @NotNull
    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    private Geschlecht geschlecht;

    @NotNull
    @NonNull
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = false)
    private String name;

    @Nullable
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = true)
    private String vorname;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_DEFAULT_MAX_LENGTH)
    private EmailAddress email;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_DEFAULT_MAX_LENGTH)
    private NormalizedPhoneNumber telefon;


    @NonNull
    public String getNameVorname() {
        if(vorname != null) {
            return name + ' ' + vorname;
        }
        return name;
    }
}
