package ch.dvbern.oss.hemed.esc.api.features.schule.kontaktpersonschule;

import java.io.Serial;
import java.util.Set;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.types.KontaktPersonSchuleFunktion;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
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
public class KontaktPersonSchule
        extends AbstractTenantEntity<KontaktPersonSchule>
        implements EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = -2616852567861298939L;

    @NotNull
    @NonNull
    @ManyToMany
    @JoinTable(name = "Schule_KontaktPersonSchule",
        joinColumns = @JoinColumn(
            name = "kontaktpersonschule_id",
            foreignKey = @ForeignKey(name = "fk_schule_kontaktpersonschule_kontaktpersonschule")),
        inverseJoinColumns = @JoinColumn(
            name = "schule_id",
            foreignKey = @ForeignKey(name = "fk_schule_kontaktpersonschule_schule")),
        indexes = {
            @Index(name = "ix_schule_kontaktpersonschule_schule",
                columnList = "schule_id, kontaktpersonschule_id"),
            @Index(name = "ix_schule_kontaktpersonschule_kontaktpersonschule",
                columnList = "kontaktpersonschule_id, schule_id"),
        }
    )
    private Set<Schule> schulen;

    @NotNull
    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    private Geschlecht geschlecht;

    @NotNull
    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    private KontaktPersonSchuleFunktion funktion;

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

    @Column(nullable = false)
    private boolean aktiv = true;

}
