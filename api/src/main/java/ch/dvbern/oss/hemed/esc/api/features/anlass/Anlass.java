package ch.dvbern.oss.hemed.esc.api.features.anlass;

import java.io.Serial;
import java.time.LocalDate;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassArt;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassStatus;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import ch.dvbern.oss.hemed.esc.api.shared.util.AnlassTitelUtil;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.types.DateTimeRange;
import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckAnlassConfigZeitraum;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
        @Index(name = "ix_anlass_anlassconfig", columnList = "anlassconfig_id, id"),
        @Index(name = "ix_anlass_benutzer_arzt", columnList = "arzt_id, id"),
        @Index(name = "ix_anlass_benutzer_mpa", columnList = "mpa_id, id"),
        @Index(name = "ix_anlass_benutzer_weiterer", columnList = "weitereperson_id, id")
    }
)
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@CheckAnlassConfigZeitraum
public class Anlass extends AbstractTenantEntity<Anlass> {

    @Serial
    private static final long serialVersionUID = 5285023727443911252L;

    @NotNull
    @NonNull
    @ManyToOne(optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_anlass_anlassconfig"), nullable = false, updatable = false)
    private AnlassConfig anlassConfig;

    @NotNull
    @NonNull
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = false)
    private String titel;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private AnlassStatus anlassStatus = AnlassStatus.IN_PLANUNG;

    @NotNull
    @NonNull
    @Column(nullable = false)
    private LocalDate datum;

    @NotNull
    @NonNull
    @Column(nullable = false)
    @Min(1995)
    @Max(9999)
    private Integer schuljahr;

    @Embedded
    @Nullable
    @Valid
    @Column(nullable = true)
    private DateTimeRange zeitVonBis;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private AnlassArt anlassArt;

    @Nullable
    @ManyToOne(optional = true)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_anlass_benutzer_arzt"), nullable = true)
    private Benutzer arzt;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_DEFAULT_MAX_LENGTH)
    private String arztInfo;

    @Nullable
    @ManyToOne(optional = true)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_anlass_benutzer_mpa"), nullable = true)
    private Benutzer mpa;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_DEFAULT_MAX_LENGTH)
    private String mpaInfo;

    @Nullable
    @ManyToOne(optional = true)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_anlass_benutzer_weiterer"), nullable = true)
    private Benutzer weiterePerson;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_DEFAULT_MAX_LENGTH)
    private String weiterePersonInfo;


    @NonNull
    public String getFullAnlassTitel(@NonNull TL tl) {
        return AnlassTitelUtil.getFullAnlassTitel(getTitel(), getSchuljahr(), getAnlassConfig().getAnlassTyp(), tl);
    }

}
