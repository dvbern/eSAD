package ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren;

import java.io.Serial;
import java.util.List;
import java.util.stream.Stream;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.Util;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import ch.dvbern.oss.hemed.esc.api.shared.util.UntersuchungHoerenUtil;
import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckUntersuchungHoerenOtoskopiePresentIfReq;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.envers.Audited;

@Entity
@Table(
    indexes = {
    }
)
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@CheckUntersuchungHoerenOtoskopiePresentIfReq
public class UntersuchungHoerenOtoskopie extends AbstractTenantEntity<UntersuchungHoerenOtoskopie> {

    @Serial
    private static final long serialVersionUID = 1446398935944895849L;

    @NotNull
    @NonNull
    @OneToOne(optional = false, mappedBy = "untersuchungHoerenOtoskopie")
    private Untersuchung untersuchung;

    @Nullable
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = true)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String otoskopieRechts;

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH)
    private String otoskopieRechtsResultatAndere;

    @Nullable
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = true)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String otoskopieLinks;

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH)
    private String otoskopieLinksResultatAndere;

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH)
    private String bemerkungen;

    @NonNull
    public static UntersuchungHoerenOtoskopie forUntersuchung(@NonNull Untersuchung untersuchung) {
        UntersuchungHoerenOtoskopie hoerenOtoskopie = new UntersuchungHoerenOtoskopie();
        hoerenOtoskopie.setUntersuchung(untersuchung);
        untersuchung.setUntersuchungHoerenOtoskopie(hoerenOtoskopie);
        return hoerenOtoskopie;
    }

    @NonNull
    public static UntersuchungHoerenOtoskopie forUntersuchungAbschliessen(
        @NonNull Untersuchung untersuchung
    ) {
        UntersuchungHoerenOtoskopie hoerenOtoskopie = untersuchung.getUntersuchungHoerenOtoskopie();
        if (hoerenOtoskopie == null) {
            hoerenOtoskopie = forUntersuchung(untersuchung);
        }
        AnlassConfig config = untersuchung.getEinladung().getAnlass().getAnlassConfig();

        hoerenOtoskopie.setOtoskopieRechts(UntersuchungHoerenUtil.getOtoskopieRechts(
            config, hoerenOtoskopie, OtoskopieResultat.NICHT_GEPRUEFT));
        hoerenOtoskopie.setOtoskopieLinks(UntersuchungHoerenUtil.getOtoskopieLinks(
            config, hoerenOtoskopie, OtoskopieResultat.NICHT_GEPRUEFT));

        return hoerenOtoskopie;
    }

    // Otoskopie Rechts
    public void setOtoskopieRechts(@Nullable List<OtoskopieResultat> otoskopieResultatList) {
        this.otoskopieRechts = Util.mapEnumListToString(otoskopieResultatList);
    }

    @NonNull
    public List<OtoskopieResultat> getOtoskopieRechts() {
        if (otoskopieRechts == null) {
            return List.of();
        }
        return Stream.of(otoskopieRechts.split(","))
            .map(StringUtils::trim)
            .filter(StringUtils::isNotBlank)
            .map(OtoskopieResultat::valueOf)
            .toList();
    }

    // Otoskopie Links
    public void setOtoskopieLinks(@Nullable List<OtoskopieResultat> otoskopieResultatList) {
        this.otoskopieLinks = Util.mapEnumListToString(otoskopieResultatList);
    }

    @NonNull
    public List<OtoskopieResultat> getOtoskopieLinks() {
        if (otoskopieLinks == null) {
            return List.of();
        }
        return Stream.of(otoskopieLinks.split(","))
            .map(StringUtils::trim)
            .filter(StringUtils::isNotBlank)
            .map(OtoskopieResultat::valueOf)
            .toList();
    }
}

