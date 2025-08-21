package ch.dvbern.oss.hemed.esc.api.features.kinder;

import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import java.io.Serial;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.features.kinder.types.PapierakteStatus;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
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
import lombok.AccessLevel;
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
public class Dossier extends AbstractTenantEntity<Dossier> {

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
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH)
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
    @Getter(AccessLevel.PRIVATE)
    private Benutzer zustaendigArzt;

    @Nullable
    @ManyToOne(optional = true)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_dossier_benutzer_mpa"), nullable = true)
    @Getter(AccessLevel.PRIVATE)
    private Benutzer zustaendigMPA;


    @Nullable
    public Benutzer getZustaendigArztFromDossierOrSchule() {
        Schule schule = this.getKind().getSchule();
        if (zustaendigArzt == null && schule != null) {
            return schule.getZustaendigArzt();
        }
        return this.zustaendigArzt;
    }

    @Nullable
    public Benutzer getZustaendigMPAFromDossierOrSchule() {
        Schule schule = this.getKind().getSchule();
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
            null
        );
    }
}
