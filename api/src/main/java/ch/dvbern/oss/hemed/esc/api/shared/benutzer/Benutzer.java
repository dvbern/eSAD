package ch.dvbern.oss.hemed.esc.api.shared.benutzer;

import java.io.Serial;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumberConverter;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
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
import org.apache.commons.lang3.StringUtils;
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
public class Benutzer extends AbstractTenantEntity<Benutzer> {

    @Serial
    private static final long serialVersionUID = -4471131500166716925L;

    public static final ID<Benutzer> SYSTEM_ADMIN_ID =
        ID.parse("11111111-1111-1111-1111-111111111111", Benutzer.class);
    public static final EmailAddress SYSTEM_ADMIN_EMAIL = EmailAddress.fromString("dev-esc@dvbern.ch");
    public static final ID<Benutzer> SYSTEM_ADMIN_BS_ID =
        ID.parse("11111111-1111-1111-1111-111111111112", Benutzer.class);
    public static final ID<Benutzer> SYSTEM_ADMIN_BE_ID =
        ID.parse("11111111-1111-1111-1111-111111111113", Benutzer.class);

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
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = true)
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

    public void setFachRollen(@NonNull List<FachRolle> fachRollen) {
        this.fachRollen = mapRollenlistToString(fachRollen);
    }

    @NonNull
    public static String mapRollenlistToString(@NonNull Collection<FachRolle> fachRollen) {
        return fachRollen.stream()
            .sorted()
            .map(FachRolle::name)
            .reduce((a, b) -> a + ", " + b)
            .orElse("");
    }

    @NonNull
    public List<FachRolle> getFachRollen() {
        if (fachRollen == null) {
            return List.of();
        }
        return Stream.of(fachRollen.split(","))
            .map(StringUtils::trim)
            .filter(StringUtils::isNotBlank)
            .map(FachRolle::valueOf)
            .toList();
    }

}
