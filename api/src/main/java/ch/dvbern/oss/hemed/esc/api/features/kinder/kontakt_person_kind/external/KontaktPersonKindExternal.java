package ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.external;

import java.io.Serial;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKind;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.externalsystem.HasExternalId;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
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
        @Index(name = "ix_kontaktpersonkindexternal_adressierungid", columnList = "adressierungid, id"),
        // kontaktpersonkindid is the external id from the csv
        @Index(name = "ix_kontaktpersonkindexternal_kontaktpersonkind", columnList = "kontaktpersonkind_id, id"),
        // kontaktpersonkind_id is fk reference id to our internal kontaktpersonkind
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uc_kontaktpersonkindexternal_adressierungid", columnNames = "adressierungid"),
    }
)
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class KontaktPersonKindExternal extends AbstractEntity<KontaktPersonKindExternal>
    implements HasExternalId {

    @Serial
    private static final long serialVersionUID = 8200429045683710581L;

    @Nullable
    @OneToOne(optional = true)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_kontaktpersonkindexternal_kontaktpersonkind"), nullable = true)
    private KontaktPersonKind kontaktPersonKind;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_DEFAULT_MAX_LENGTH)
    @IgnoreForIdClassTest
    private String schuelerIds; // Kommaseparierte Liste von schuelerId

    /**
     * Es werden zuerst alle Rows von Lehrpersonen in ein KontaktPersonSchuleExternal
     * geschrieben. Danach erst werden die doppelten zusammengefasst und die IDs unter
     * escadaKlasseIds gemappt. escadaKlasseId brauchen wir daher nur temporär, bis wir
     * alle gleichen gefunden haben.
     */
    @Transient
    @IgnoreForIdClassTest
    String schuelerId;

    @NotNull
    @NonNull
    @Column(nullable = false, unique = true)
    @IgnoreForIdClassTest
    String adressierungId;

    @Nullable
    @Column(nullable = true)
    String adressierungAnrede;

    @Nullable
    @Column(nullable = true)
    String adressierungBeziehung;

    @Nullable
    @Column(nullable = true)
    String adressierungName;

    @Nullable
    @Column(nullable = true)
    String adressierungVorname;

    @Nullable
    @Column(nullable = true)
    String adressierungStrasse;

    @Nullable
    @Column(nullable = true)
    String adressierungHausnummer;

    @Nullable
    @Column(nullable = true)
    String adressierungPlz;

    @Nullable
    @Column(nullable = true)
    String adressierungOrt;

    @Nullable
    @Column(nullable = true)
    String adressierungTelefonPrivat;

    @Nullable
    @Column(nullable = true)
    String adressierungTelefonGeschaeft;

    @Nullable
    @Column(nullable = true)
    String adressierungNatel;

    @Nullable
    @Column(nullable = true)
    String adressierungEmail;

    @Override
    @NonNull
    public String getExternalId() {
        return adressierungId;
    }

    @NonNull
    public String getFullName() {
        return Stream.of(adressierungVorname, adressierungName)
            .filter(StringUtils::isNotEmpty)
            .collect(Collectors.joining(" "));
    }

    @Nullable
    public String getFullNameIfAllowed(boolean escadaRedactPersonalInfo) {
        if (escadaRedactPersonalInfo) {
            return null;
        }
        return this.getFullName();
    }
}
