package ch.dvbern.oss.hemed.esc.api.shared.benutzer;

import java.io.Serial;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumberConverter;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.envers.Audited;

@Entity
@Table(
    indexes = {
        @Index(name = "ix_benutzer_id", columnList = "id, version"),
        @Index(name = "ix_benutzer_email", columnList = "email, id"),
        @Index(name = "ix_benutzer_keycloakId", columnList = "keycloakId, id"),
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uc_benutzer_keycloakId", columnNames = "keycloakId"),
        @UniqueConstraint(name = "uc_benutzer_email", columnNames = "email"),
    }
)
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Benutzer extends AbstractEntity<Benutzer> implements EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = -4471131500166716925L;

    public static final ID<Benutzer> SYSTEM_ADMIN_BS_ID =
        ID.parse("11111111-1111-1111-1111-111111111112", Benutzer.class);
    public static final ID<Benutzer> SYSTEM_ADMIN_BE_ID =
        ID.parse("11111111-1111-1111-1111-111111111113", Benutzer.class);
    public static final ID<Benutzer> SYSTEM_ADMIN_WIN_ID =
        ID.parse("11111111-1111-1111-1111-111111111114", Benutzer.class);
    public static final ID<Benutzer> FILE_INGRESS_BENUTZER_ID =
        ID.parse("03a61ae1-41a1-4b2f-bc8c-8b4caf2cc85e", Benutzer.class);
    public static final ID<Benutzer> ESCADA_BENUTZER_ID =
        ID.parse("53483f35-fd62-437f-b366-32f08cc9d868", Benutzer.class);
    public static final ID<Benutzer> PUPIL_BENUTZER_ID =
        ID.parse("774e7a9f-8908-4aab-8fa2-2d21cdc7cda8", Benutzer.class);

    @Nullable
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = true)
    @IgnoreForIdClassTest
    private String keycloakId;

    @NotNull
    @NonNull
    @Column(nullable = false)
    private EmailAddress email;

    @Nullable
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = true, length = DbConst.DB_DEFAULT_MAX_LENGTH)
    private String titel;

    @NotNull
    @NonNull
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = false)
    private String nachname;

    @NotNull
    @NonNull
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = false)
    private String vorname;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Convert(converter = NormalizedPhoneNumberConverter.class)
    private NormalizedPhoneNumber telefon;

    @Valid
    @Embedded
    @Nullable
    private BenutzerLoginInfos loginInfos;

    @Nullable
    @Size(max = DbConst.DB_ENUM_LIST_LENGTH)
    @Column(nullable = true, length = DbConst.DB_ENUM_LIST_LENGTH)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String fachRollen;

    @NonNull
    @Valid
    @OneToMany(mappedBy = "benutzer", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<BenutzerBerechtigung> berechtigungen = new HashSet<>();

    /**
     * The actual deactivation happens in the login system (i.e. Keycloak or Entra) but this serves as a flag, so we
     * know which users were last thought of by esc as deaktiviert, this will be used to remove them from dropdowns etc
     */
    @Column(nullable = false)
    private boolean aktiv = true;

    @Nullable
    @Column(nullable = true)
    private OffsetDateTime timestampLastManuallyActivated;

    @NonNull
    public String getNameVorname() {
        return nachname + ' ' + vorname;
    }

    @NonNull
    public String getVornameName() {
        return vorname + ' ' + nachname;
    }

    public void setFachRollen(@NonNull EnumSet<FachRolle> fachRollen) {
        this.fachRollen = EnumSetUtil.toString(fachRollen);
    }

    @NonNull
    public EnumSet<FachRolle> getFachRollen() {
        return EnumSetUtil.fromString(FachRolle.class, fachRollen);
    }

    @NonNull
    public static ID<Benutzer> getSystemAdminId(@NonNull TenantEnum tenant) {
        return switch (tenant) {
            case BS -> SYSTEM_ADMIN_BS_ID;
            case BE -> SYSTEM_ADMIN_BE_ID;
            case WIN -> SYSTEM_ADMIN_WIN_ID;
            default -> throw new IllegalArgumentException("Unknown tenant: " + tenant);
        };
    }
}
