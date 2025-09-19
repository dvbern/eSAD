package ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.Util;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import ch.dvbern.oss.hemed.esc.api.shared.util.UntersuchungHoerenUtil;
import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckUntersuchungHoerenAudiogrammPresentIfReq;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.envers.Audited;

import java.io.Serial;
import java.util.List;
import java.util.stream.Stream;

@Entity
@Table(
    indexes = {
        @Index(name = "ix_untersuchunghoerenaudiogramm_audiogrammrechts", columnList = "audiogrammrechts_id, id"),
        @Index(name = "ix_untersuchunghoerenaudiogramm_audiogrammlinks", columnList = "audiogrammlinks_id, id"),
    }
)
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@CheckUntersuchungHoerenAudiogrammPresentIfReq
public class UntersuchungHoerenAudiogramm extends AbstractTenantEntity<UntersuchungHoerenAudiogramm>
        implements EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = 1446398935944895849L;

    @NotNull
    @NonNull
    @OneToOne(optional = false, mappedBy = "untersuchungHoerenAudiogramm")
    private Untersuchung untersuchung;

    @Nullable
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchunghoerenaudiogramm_audiogramm_rechts"), nullable = true)
    private Audiogramm audiogrammRechts;

    @Nullable
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchunghoerenaudiogramm_audiogramm_links"), nullable = true)
    private Audiogramm audiogrammLinks;

    @Nullable
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = true)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String audiogrammBeurteilung;

    @Nullable
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = true)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String zusaetzlicheAngaben;

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    private String bemerkungen;

    @NonNull
    public static UntersuchungHoerenAudiogramm forUntersuchung(@NonNull Untersuchung untersuchung) {
        UntersuchungHoerenAudiogramm hoerenAudiogramm = new UntersuchungHoerenAudiogramm();
        hoerenAudiogramm.setUntersuchung(untersuchung);
        untersuchung.setUntersuchungHoerenAudiogramm(hoerenAudiogramm);
        return hoerenAudiogramm;
    }

    @NonNull
    public static UntersuchungHoerenAudiogramm forUntersuchungAbschliessen(
        @NonNull Untersuchung untersuchung
    ) {
        UntersuchungHoerenAudiogramm hoerenAudiogramm = untersuchung.getUntersuchungHoerenAudiogramm();
        if (hoerenAudiogramm == null) {
            hoerenAudiogramm = forUntersuchung(untersuchung);
        }
        AnlassConfig config = untersuchung.getEinladung().getAnlass().getAnlassConfig();

        hoerenAudiogramm.setAudiogrammBeurteilung(UntersuchungHoerenUtil.getAudiogrammBeurteilung(
            config, hoerenAudiogramm, AudiogrammBeurteilung.NICHT_GEPRUEFT));
        hoerenAudiogramm.setZusaetzlicheAngaben(UntersuchungHoerenUtil.getZusaetzlicheAngaben(
            config, hoerenAudiogramm, HoerenZusaetzlicheAngaben.KEINE));
        return hoerenAudiogramm;
    }

    // AudiogrammBeurteilung
    public void setAudiogrammBeurteilung(@Nullable List<AudiogrammBeurteilung> audiogrammBeurteilungList) {
        this.audiogrammBeurteilung = Util.mapEnumListToString(audiogrammBeurteilungList);
    }

    @NonNull
    public List<AudiogrammBeurteilung> getAudiogrammBeurteilung() {
        if (audiogrammBeurteilung == null) {
            return List.of();
        }
        return Stream.of(audiogrammBeurteilung.split(","))
            .map(StringUtils::trim)
            .filter(StringUtils::isNotBlank)
            .map(AudiogrammBeurteilung::valueOf)
            .toList();
    }

    // ZusaetzlicheAngaben
    public void setZusaetzlicheAngaben(@Nullable List<HoerenZusaetzlicheAngaben> zusaetzlicheAngabenList) {
        this.zusaetzlicheAngaben = Util.mapEnumListToString(zusaetzlicheAngabenList);
    }

    @NonNull
    public List<HoerenZusaetzlicheAngaben> getZusaetzlicheAngaben() {
        if (zusaetzlicheAngaben == null) {
            return List.of();
        }
        return Stream.of(zusaetzlicheAngaben.split(","))
            .map(StringUtils::trim)
            .filter(StringUtils::isNotBlank)
            .map(HoerenZusaetzlicheAngaben::valueOf)
            .toList();
    }
}

