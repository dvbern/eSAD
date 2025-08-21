package ch.dvbern.oss.hemed.esc.api.features.schule;

import java.io.Serial;
import java.io.Serializable;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumberConverter;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ExternalImportInfo;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.HasExternalImportInfo;
import ch.dvbern.oss.hemed.esc.api.features.schule.quartier.Quartier;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.person.Adresse;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.envers.Audited;

@Entity
@Table(indexes = {
    @Index(name = "ix_schule_benutzer_arzt_id", columnList = "zustaendigArzt_id, id"),
    @Index(name = "ix_schule_benutzer_mpa_id", columnList = "zustaendigMPA_id, id"),
    @Index(name = "ix_schule_quartier_id", columnList = "quartier_id, id"),
    @Index(name = "ix_schule_schulhaus_id", columnList = "schulhaus, id"),
})
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Schule extends AbstractTenantEntity<Schule> implements Serializable, HasExternalImportInfo {

    @Serial
    private static final long serialVersionUID = 1741625114973564294L;

    @Embedded
    @Valid
    @Nullable
    private ExternalImportInfo externalImportInfo;

    @NotNull
    @NonNull
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = false)
    private String schulhaus;

    @Nullable
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = true)
    private String schulstandort;

    @Nullable
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = true)
    private String schulkreis;

    @Nullable
    @ManyToOne(optional = true)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_schule_quartier"), nullable = true)
    private Quartier quartier;

    @Nullable
    @Size(max = 3) // Max 999 which is 3 digits
    @Column(nullable = true, length = 3)
    private String schulhausnummer;

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

    @Nullable
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = true)
    private String bemerkungen;

    @Nullable
    @ManyToOne(optional = true)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_schule_benutzer_arzt"), nullable = true)
    private Benutzer zustaendigArzt;

    @Nullable
    @ManyToOne(optional = true)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_schule_benutzer_mpa"), nullable = true)
    private Benutzer zustaendigMPA;

    /**
     * Status of the Schule in the eSad system
     */
    @NotNull
    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    private EntityAktivStatus schuleAktivStatus = EntityAktivStatus.AKTIV;

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

    @Transient
    @NonNull
    public String getFormattedSchulhausnummer() {
        if (StringUtils.isEmpty(schulhausnummer) && !StringUtils.isNumeric(schulhausnummer)) {
            throw new AppValidationException(AppValidationMessage.noSchulhausnummer(
                schulhaus));
        }

        return String.format("%03d", Integer.parseInt(schulhausnummer));
    }
}
