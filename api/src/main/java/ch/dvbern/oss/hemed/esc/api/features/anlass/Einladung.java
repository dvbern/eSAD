package ch.dvbern.oss.hemed.esc.api.features.anlass;

import java.io.Serial;
import java.time.LocalDateTime;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.ArtAbmeldung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.EinladungStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.GrundAbmeldung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.NaechsteStation;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.UniqueConstraint;
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
        @Index(name = "ix_einladung_anlass", columnList = "anlass_id, id"),
        @Index(name = "ix_einladung_dossier", columnList = "dossier_id, id"),
        @Index(name = "ix_einladung_untersuchung", columnList = "untersuchung_id, id"),
        @Index(name = "ix_einladung_notiz_impfausweis", columnList = "impfausweis_id, id"),
        @Index(name = "ix_einladung_notiz_fragebogen", columnList = "fragebogen_id, id"),
        @Index(name = "ix_einladung_notiz_abmeldung", columnList = "abmeldungsnotiz_id, id")
    },
    uniqueConstraints = @UniqueConstraint(name = "uc_einladung_anlass_dossier",
        columnNames = { "anlass_id", "dossier_id" })
)
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Einladung extends AbstractTenantEntity<Einladung> {

    @Serial
    private static final long serialVersionUID = 5285023727443911252L;

    @NotNull
    @NonNull
    @ManyToOne(optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_einladung_anlass"), nullable = false, updatable = false)
    private Anlass anlass;

    @NotNull
    @NonNull
    @ManyToOne(optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_einladung_dossier"), nullable = false, updatable = true)
    private Dossier dossier;

    @Nullable
    @OneToOne(optional = true)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_einladung_untersuchung"), nullable = true)
    private Untersuchung untersuchung;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private EinladungStatus einladungStatus = EinladungStatus.TEILNAHME_BESTAETIGT;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private GrundAbmeldung grundAbmeldung;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private ArtAbmeldung artAbmeldung;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_DEFAULT_MAX_LENGTH)
    private String bemerkungAbmeldung;

    @Nullable
    @OneToOne(optional = true, cascade = CascadeType.REMOVE)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_einladung_notiz_impfausweis"), nullable = true, updatable = true)
    private Notiz impfausweis;

    @Nullable
    @OneToOne(optional = true, cascade = CascadeType.REMOVE)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_einladung_notiz_fragebogen"), nullable = true, updatable = true)
    private Notiz fragebogen;

    @Nullable
    @OneToOne(optional = true, cascade = CascadeType.REMOVE)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_einladung_notiz_abmeldung"), nullable = true, updatable = true)
    private Notiz abmeldungsNotiz;

    @NotNull
    @Column(nullable = false)
    private boolean spssExported = false;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private NaechsteStation naechsteStation;

    @Nullable
    @Column(nullable = true)
    private LocalDateTime naechsteStationSeit;


    @NonNull
    public static Einladung create(@NonNull Anlass anlass, @NonNull Dossier dossier) {
        return new Einladung(
            anlass,
            dossier,
            null,
            EinladungStatus.TEILNAHME_BESTAETIGT,
            null,
            null,
            null,
            null,
            null,
            null,
            false,
            null,
            null
        );
    }
}
