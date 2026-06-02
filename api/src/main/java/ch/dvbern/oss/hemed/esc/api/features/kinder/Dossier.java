package ch.dvbern.oss.hemed.esc.api.features.kinder;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.organisation.Organisation;
import ch.dvbern.oss.hemed.esc.api.shared.util.HasZustaendigkeit;
import ch.dvbern.oss.hemed.esc.api.features.kinder.types.PapierakteStatus;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckArztRequiredForDezentraleTenants;
import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckArztUndMpaSameOrganisation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
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
        @Index(name = "ix_dossier_benutzer_arzt_id", columnList = "zustaendigArzt_id, id"),
        @Index(name = "ix_dossier_benutzer_mpa_id", columnList = "zustaendigMPA_id, id"),
        @Index(name = "ix_dossier_kind_id", columnList = "kind_id, id")
    }
)
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@CheckArztUndMpaSameOrganisation
@CheckArztRequiredForDezentraleTenants
public class Dossier extends AbstractEntity<Dossier> implements EntityWithOptimisticLocking, HasZustaendigkeit {

    @Serial
    private static final long serialVersionUID = -114539261691711361L;

    @NotNull
    @NonNull
    @OneToOne(optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_dossier_kind"), nullable = false, updatable = false)
    private Kind kind;

    @NotNull
    @Column(nullable = false)
    private boolean kinderschutzfall = false;

    @NotNull
    @Column(nullable = false)
    private boolean keineAuskunftAnDrittpersonen = false;

    @NotNull
    @Column(nullable = false)
    private boolean sansPapier = false;

    @NotNull
    @Column(nullable = false)
    private boolean andereZusatzinformation = false;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    private String andereZusatzInformationText;

    @NotNull
    @Column(nullable = false)
    private boolean immunitaetMasern = false;

    @NotNull
    @Column(nullable = false)
    private boolean immunitaetVarizellen = false;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private PapierakteStatus papierakteStatus = PapierakteStatus.UNKLAR;

    @Nullable
    @ManyToOne(optional = true)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_dossier_benutzer_arzt"), nullable = true)
    private Benutzer zustaendigArzt;

    @Nullable
    @ManyToOne(optional = true)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_dossier_benutzer_mpa"), nullable = true)
    private Benutzer zustaendigMPA;

    @Nullable
    @Column(nullable = true)
    private LocalDateTime timestampDefinitivArchiviert;

    @Nullable
    public Benutzer getZustaendigArztFromSchule() {
        Schule schule = this.getKind().extractSchule();
        return schule != null ? schule.getZustaendigArzt() : null;
    }

    @Nullable
    public Benutzer getZustaendigMpaFromSchule() {
        Schule schule = this.getKind().extractSchule();
        return schule != null ? schule.getZustaendigMPA() : null;
    }

    @Nullable
    public ID<Benutzer> getZustaendigArztIdFromSchule() {
        Benutzer arzt = getZustaendigArztFromSchule();
        return arzt != null ? arzt.getId() : null;
    }

    @Nullable
    public ID<Benutzer> getZustaendigMpaIdFromSchule() {
        Benutzer arzt = getZustaendigMpaFromSchule();
        return arzt != null ? arzt.getId() : null;
    }

    @Nullable
    public Benutzer getZustaendigArztFromDossierOrSchule() {
        Schule schule = this.getKind().extractSchule();
        if (zustaendigArzt == null && schule != null) {
            return schule.getZustaendigArzt();
        }
        return this.zustaendigArzt;
    }

    public boolean isBenutzerZustaendigForDossier(@NonNull Benutzer benutzer) {
        if (benutzer.hasFachRolleSupport()) {
            return true;
        }
        Organisation benutzerOrganisation = benutzer.getSingleOrganisation();
        if (benutzerOrganisation == null) {
            return false;
        }

        Organisation dossierOrganisation = this.getSingleOrganisation();
        if (benutzerOrganisation.equals(dossierOrganisation)) {
            return true;
        }

        Organisation schuleOrganisation = this.getKind().getSchule() != null ? this.getKind().getSchule().getSingleOrganisation() : null;
        return benutzerOrganisation.equals(schuleOrganisation);
    }

    @Nullable
    public Benutzer getZustaendigMPAFromDossierOrSchule() {
        Schule schule = this.getKind().extractSchule();
        if (zustaendigMPA == null && schule != null) {
            return schule.getZustaendigMPA();
        }
        return this.zustaendigMPA;
    }

    @NonNull
    public static Dossier forKind(@NonNull Kind kind) {
        return new Dossier(
            kind,
            false,
            false,
            false,
            false,
            null,
            false,
            false,
            PapierakteStatus.UNKLAR,
            null,
            null,
            null
        );
    }
}
