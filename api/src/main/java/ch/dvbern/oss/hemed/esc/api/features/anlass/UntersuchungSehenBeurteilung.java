package ch.dvbern.oss.hemed.esc.api.features.anlass;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.Util;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.*;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import ch.dvbern.oss.hemed.esc.api.shared.util.UntersuchungSehenUtil;
import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckUntersuchungSehenBeurteilungPresentIfReq;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.envers.Audited;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

@Entity
@Table
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@CheckUntersuchungSehenBeurteilungPresentIfReq
public class UntersuchungSehenBeurteilung extends AbstractTenantEntity<UntersuchungSehenBeurteilung> implements EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = 1446398935944895849L;

    @NotNull
    @NonNull
    @OneToOne(optional = false, mappedBy = "untersuchungSehenBeurteilung")
    private Untersuchung untersuchung;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private BrilleLinsenResultat brilleLinsen;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private StereotestResultat stereotest;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private FarbsehenResultat farbsehen;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private VerwendetTafelResultat verwendeteTafel;

    @Nullable
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = true)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String fernvisus;

    @Nullable
    @Schema(required = false)
    @Column(nullable = true)
    private BigDecimal fernvisusRechts;

    @Nullable
    @Schema(required = false)
    @Column(nullable = true)
    private BigDecimal fernvisusLinks;

    @Nullable
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = true)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String nahvisus;

    @Nullable
    @Schema(required = false)
    @Column(nullable = true)
    private BigDecimal nahvisusRechts;

    @Nullable
    @Schema(required = false)
    @Column(nullable = true)
    private BigDecimal nahvisusLinks;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private MotilitaetResultat motilitaet;

    @Nullable
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = true)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String strabismus;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private StrabismusAuspraegungResultat strabismusAuspraegung;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private StrabismusAuftretenResultat strabismusAuftreten;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private InspektionKonvergenzResultat inspektion;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private InspektionKonvergenzResultat konvergenz;

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    private String bemerkungen;

    // Fernvisus
    public void setFernvisus(@Nullable List<DistanzSehenResultat> distanzSehenResultate) {
        this.fernvisus = Util.mapEnumListToString(distanzSehenResultate);
    }

    @NonNull
    public List<DistanzSehenResultat> getFernvisus() {
        if (fernvisus == null) {
            return List.of();
        }
        return Stream.of(fernvisus.split(","))
            .map(StringUtils::trim)
            .filter(StringUtils::isNotBlank)
            .map(DistanzSehenResultat::valueOf)
            .toList();
    }

    // Nahvisus
    public void setNahvisus(@Nullable List<DistanzSehenResultat> distanzSehenResultate) {
        this.nahvisus = Util.mapEnumListToString(distanzSehenResultate);
    }

    @NonNull
    public List<DistanzSehenResultat> getNahvisus() {
        if (nahvisus == null) {
            return List.of();
        }
        return Stream.of(nahvisus.split(","))
            .map(StringUtils::trim)
            .filter(StringUtils::isNotBlank)
            .map(DistanzSehenResultat::valueOf)
            .toList();
    }

    // Strabismus
    public void setStrabismus(@Nullable List<StrabismusResultat> strabismusResultate) {
        this.strabismus = Util.mapEnumListToString(strabismusResultate);
    }

    @NonNull
    public List<StrabismusResultat> getStrabismus() {
        if (strabismus == null) {
            return List.of();
        }
        return Stream.of(strabismus.split(","))
            .map(StringUtils::trim)
            .filter(StringUtils::isNotBlank)
            .map(StrabismusResultat::valueOf)
            .toList();
    }

    @NonNull
    public static UntersuchungSehenBeurteilung forUntersuchung(@NonNull Untersuchung untersuchung) {
        UntersuchungSehenBeurteilung sehen = new UntersuchungSehenBeurteilung();
        sehen.setUntersuchung(untersuchung);
        untersuchung.setUntersuchungSehenBeurteilung(sehen);
        return sehen;
    }

    @NonNull
    public static UntersuchungSehenBeurteilung forUntersuchungAbschliessen(
        @NonNull Untersuchung untersuchung
    ) {
        UntersuchungSehenBeurteilung sehen = untersuchung.getUntersuchungSehenBeurteilung();
        if (sehen == null) {
            sehen = forUntersuchung(untersuchung);
        }
        AnlassConfig config = untersuchung.getEinladung().getAnlass().getAnlassConfig();

        sehen.setBrilleLinsen(UntersuchungSehenUtil.getBrilleLinsen(
            config, sehen, BrilleLinsenResultat.NICHT_GEPRUEFT));
        sehen.setStereotest(UntersuchungSehenUtil.getStereoTest(
            config, sehen, StereotestResultat.NICHT_GEPRUEFT));
        sehen.setFarbsehen(UntersuchungSehenUtil.getFarbsehen(
            config, sehen, FarbsehenResultat.NICHT_GEPRUEFT));
        sehen.setVerwendeteTafel(UntersuchungSehenUtil.getVerwendeteTafel(
            config, sehen, VerwendetTafelResultat.NICHT_GEPRUEFT));
        sehen.setFernvisus(UntersuchungSehenUtil.getFernvisus(
            config, sehen, DistanzSehenResultat.NICHT_GEPRUEFT));
        sehen.setNahvisus(UntersuchungSehenUtil.getNahvisus(
            config, sehen, DistanzSehenResultat.NICHT_GEPRUEFT));
        sehen.setMotilitaet(UntersuchungSehenUtil.getMotilitaet(
            config, sehen, MotilitaetResultat.NICHT_GEPRUEFT));
        sehen.setStrabismus(UntersuchungSehenUtil.getStrabismus(
            config, sehen, StrabismusResultat.NICHT_GEPRUEFT));
        sehen.setStrabismusAuspraegung(UntersuchungSehenUtil.getStrabismusAuspraegung(
            config, sehen, StrabismusAuspraegungResultat.NICHT_GEPRUEFT));
        sehen.setStrabismusAuftreten(UntersuchungSehenUtil.getStrabismusAuftreten(
            config, sehen, StrabismusAuftretenResultat.NICHT_GEPRUEFT));
        sehen.setInspektion(UntersuchungSehenUtil.getInspektion(
            config, sehen, InspektionKonvergenzResultat.NICHT_GEPRUEFT));
        sehen.setKonvergenz(UntersuchungSehenUtil.getKonvergenz(
            config, sehen, InspektionKonvergenzResultat.NICHT_GEPRUEFT));
        return sehen;
    }
}

