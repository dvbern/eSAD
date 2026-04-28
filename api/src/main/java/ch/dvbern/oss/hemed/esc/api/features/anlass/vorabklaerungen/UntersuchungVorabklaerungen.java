package ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen;

import java.io.Serial;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.types.ChronischeKrankheiten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.types.EinverstaendnisImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.types.FragebogenStatus;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;
import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckUntersuchungVorabklaerungenPresentIfReq;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhanced;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hibernate.envers.Audited;

@Entity
@Table
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@CheckUntersuchungVorabklaerungenPresentIfReq
public class UntersuchungVorabklaerungen extends AbstractEntity<UntersuchungVorabklaerungen>
    implements EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = 6273009786950768017L;

    @NotNull
    @NonNull
    @OneToOne(optional = false, mappedBy = "untersuchungVorabklaerungen")
    private Untersuchung untersuchung;

    @Valid
    @Embedded
    @NonNull
    @NotNull
    @AttributeOverride(name = "value",
        column = @Column(name = "fragebogenStatus", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<FragebogenStatus> fragebogenStatus = SingleValueEnhanced.of();

    @Valid
    @Embedded
    @NonNull
    @NotNull
    @AttributeOverride(name = "value",
        column = @Column(name = "einverstaendnisImpfungen", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<EinverstaendnisImpfungen> einverstaendnisImpfungen = SingleValueEnhanced.of();

    @Valid
    @Embedded
    @NonNull
    @NotNull
    @AttributeOverride(name = "value",
        column = @Column(name = "chronischeKrankheiten", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<ChronischeKrankheiten> chronischeKrankheiten = SingleValueEnhanced.of();

    @NonNull
    public static UntersuchungVorabklaerungen forUntersuchung(@NonNull Untersuchung untersuchung) {
        UntersuchungVorabklaerungen vorabklaerungen = new UntersuchungVorabklaerungen();
        vorabklaerungen.setUntersuchung(untersuchung);
        untersuchung.setUntersuchungVorabklaerungen(vorabklaerungen);
        return vorabklaerungen;
    }

    @NonNull
    public static UntersuchungVorabklaerungen forUntersuchungAbschliessen(
        @NonNull Untersuchung untersuchung,
        boolean nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
    ) {
        UntersuchungVorabklaerungen vorabklaerungen = untersuchung.getUntersuchungVorabklaerungen();
        if (vorabklaerungen == null) {
            vorabklaerungen = forUntersuchung(untersuchung);
        }
        AnlassConfig config = untersuchung.getAnlass().getAnlassConfig();
        vorabklaerungen.setFragebogenStatus(
            ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getVorabklaerungenFragebogenStatus,
                vorabklaerungen,
                UntersuchungVorabklaerungen::getFragebogenStatus,
                FragebogenStatus.NICHT_ERHALTEN,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        vorabklaerungen.setEinverstaendnisImpfungen(
            ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getVorabklaerungenEinverstaendnisImpfungen,
                vorabklaerungen,
                UntersuchungVorabklaerungen::getEinverstaendnisImpfungen,
                EinverstaendnisImpfungen.NICHT_ERHALTEN,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        vorabklaerungen.setChronischeKrankheiten(
            ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getVorabklaerungenChronischeKrankheiten,
                vorabklaerungen,
                UntersuchungVorabklaerungen::getChronischeKrankheiten,
                ChronischeKrankheiten.NICHT_ABGEKLAERT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        return vorabklaerungen;
    }
}
