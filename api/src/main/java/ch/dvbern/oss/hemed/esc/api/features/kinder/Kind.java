package ch.dvbern.oss.hemed.esc.api.features.kinder;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumberConverter;
import ch.dvbern.dvbstarter.shared.types.sozialversicherungsnummer.SozialversicherungsNummer;
import ch.dvbern.dvbstarter.shared.types.sozialversicherungsnummer.SozialversicherungsNummerConverter;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ExternalImportInfo;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.HasExternalImportInfo;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.person.Adresse;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.shared.person.Land;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckEsadId;
import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckKindKlasseSchuleConsistency;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
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
@Table(
    indexes = {
        @Index(name = "ix_kind_sozialversicherungsNummer", columnList = "sozialversicherungsNummer, id"),
        @Index(name = "ix_kind_klasse_id", columnList = "klasse_id, id"),
        @Index(name = "ix_kind_schule_id", columnList = "schule_id, id"),
        @Index(name = "ix_kind_esadId", columnList = "esadId, id"),
        @Index(name = "ix_kind_name", columnList = "name, vorname, id"),
        @Index(name = "ix_kind_geburtsdatum", columnList = "geburtsdatum, id"),
        @Index(name = "ix_kind_duplikat",
            columnList = "normalizedname, normalizedvorname, geburtsdatum, geschlecht, id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uc_kind_sozialversicherungsNummer", columnNames = "sozialversicherungsNummer"),
        @UniqueConstraint(name = "uc_kind_esadId", columnNames = "esadId")
    }
)
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@CheckKindKlasseSchuleConsistency
public class Kind extends AbstractEntity<Kind>
        implements Serializable, HasExternalImportInfo, EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = 8005202370526715032L;

    @Embedded
    @Valid
    @Nullable
    private ExternalImportInfo externalImportInfo;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_ENUM_LENGTH)
    @IgnoreForIdClassTest
    @CheckEsadId
    private String esadId;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Convert(converter = SozialversicherungsNummerConverter.class)
    private SozialversicherungsNummer sozialversicherungsNummer;

    @NotNull
    @NonNull
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = false)
    private String name;

    @NotNull
    @NonNull
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = false)
    private String vorname;

    @NotNull
    @NonNull
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = false)
    private String normalizedName;

    @NotNull
    @NonNull
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = false)
    private String normalizedVorname;

    @NotNull
    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    private Geschlecht geschlecht;

    @NotNull
    @NonNull
    @Column(nullable = false)
    private LocalDate geburtsdatum;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private Land nationalitaet;

    @Nullable
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = true)
    private String erstsprache;

    @Column(nullable = true)
    @Nullable
    private String wohntyp;

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

    /**
     * In principle the Schule is given through the Klasse, but there are cases where the Klasse is unknown but the
     * Schule is known.
     */
    @Nullable
    @ManyToOne(optional = true)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_kind_schule"), nullable = true)
    private Schule schule;

    @Nullable
    @ManyToOne(optional = true)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_kind_klasse"), nullable = true)
    private Klasse klasse;

    /**
     * Status of the Kind in the eSad system, this is a combination of the aktiv flag and the time of the last change.
     */
    @NotNull
    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    private EntityAktivStatus kindAktivStatus = EntityAktivStatus.AKTIV;


    @NonNull
    public String getNameVorname() {
        return name + ' ' + vorname;
    }

    @Nullable
    public String getFullNameIfAllowed(boolean redactFullName) {
        if (redactFullName) {
            return null;
        }
        return this.getNameVorname();
    }

    @Nullable
    @Transient
    public Schule extractSchule() {
        return klasse != null ? klasse.getSchule() : schule;
    }
}
