package ch.dvbern.oss.hemed.esc.api.features.anlass.einladung;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.types.ArtAbmeldung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.types.EinladungStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.types.GrundAbmeldung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.types.NaechsteStation;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizTyp;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckEinladungNotizUniqueness;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
    },
    uniqueConstraints = @UniqueConstraint(name = "uc_einladung_anlass_dossier",
        columnNames = { "anlass_id", "dossier_id" })
)
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@CheckEinladungNotizUniqueness
public class Einladung extends AbstractEntity<Einladung> implements EntityWithOptimisticLocking {

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
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    private String bemerkungAbmeldung;

    @NotNull
    @NonNull
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "Einladung_Notiz",
        joinColumns = @JoinColumn(
            name = "einladung_id",
            foreignKey = @ForeignKey(name = "fk_einladung_notiz_einladung")),
        inverseJoinColumns = @JoinColumn(
            name = "notiz_id",
            foreignKey = @ForeignKey(name = "fk_einladung_notiz_notiz")),
        indexes = {
            @Index(name = "ix_einladung_notiz_einladung",
                columnList = "einladung_id"),
            @Index(name = "ix_einladung_notiz_notiz",
                columnList = "notiz_id"),
        }
    )
    private List<Notiz> notizen = List.of();

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
            List.of(),
            false,
            null,
            null
        );
    }

    @NonNull
    public Optional<Notiz> getImpfausweisNotiz() {
        return getNotizByType(NotizTyp.IMPFAUSWEIS);
    }

    @NonNull
    public Optional<Notiz> getFragebogenNotiz() {
        return getNotizByType(NotizTyp.FRAGEBOGEN);
    }

    @NonNull
    public Optional<Notiz> getAnmeldungBestaetigungNotiz() {
        return getNotizByType(NotizTyp.ANMELDUNG_BESTAETIGUNG);
    }

    @NonNull
    public Optional<Notiz> getAbmeldungsNotiz() {
        return getNotizByType(NotizTyp.ABMELDUNG);
    }

    @NonNull
    private Optional<Notiz> getNotizByType(@NonNull NotizTyp notizTyp) {
        return notizen.stream()
            .filter(notiz -> notiz.getTyp() == notizTyp)
            .findFirst();
    }
}
