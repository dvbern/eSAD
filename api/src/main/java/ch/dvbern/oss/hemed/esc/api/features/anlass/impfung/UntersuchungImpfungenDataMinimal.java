package ch.dvbern.oss.hemed.esc.api.features.anlass.impfung;

import java.io.Serial;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.Immunsuppression;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfStatusMinimal;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfausweisGeprueft;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfempfehlungMinimal;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfungDurchgefuehrtMinimal;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;
import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckUntersuchungImpfungenDataMinimalPresentIfReq;
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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.envers.Audited;

@Entity
@Table
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@CheckUntersuchungImpfungenDataMinimalPresentIfReq
public class UntersuchungImpfungenDataMinimal
    extends AbstractEntity<UntersuchungImpfungenDataMinimal>
    implements EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = 7512251037224910832L;

    @NotNull
    @NonNull
    @OneToOne(optional = false, mappedBy = "untersuchungImpfungenDataMinimal")
    private UntersuchungImpfungen untersuchungImpfungen;

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value", column = @Column(name = "impfausweisGeprueft", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<ImpfausweisGeprueft> impfausweisGeprueft;

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value", column = @Column(name = "immunsuppression", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<Immunsuppression> immunsuppression;

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value", column = @Column(name = "masernImpfstatus", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<ImpfStatusMinimal> masernImpfstatus;

    @Nullable
    @Column(nullable = true)
    private Integer masernAnzahlImpfungen = null;

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value", column = @Column(name = "hpvImpfstatus", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<ImpfStatusMinimal> hpvImpfstatus;

    @Nullable
    @Column(nullable = true)
    private Integer hpvAnzahlImpfungen = null;

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value", column = @Column(name = "impfempfehlung", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<ImpfempfehlungMinimal> impfempfehlung;

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value", column = @Column(name = "impfungDurchgefuehrt", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<ImpfungDurchgefuehrtMinimal> impfungDurchgefuehrt;


    @NonNull
    public static UntersuchungImpfungenDataMinimal forUntersuchung(@NonNull UntersuchungImpfungen untersuchung) {
        UntersuchungImpfungenDataMinimal dataMinimal = new UntersuchungImpfungenDataMinimal();
        dataMinimal.setUntersuchungImpfungen(untersuchung);
        untersuchung.setUntersuchungImpfungenDataMinimal(dataMinimal);
        return dataMinimal;
    }

    @NonNull
    public static UntersuchungImpfungenDataMinimal forUntersuchungAbschliessen(
        @NonNull UntersuchungImpfungen untersuchung
    ) {
        UntersuchungImpfungenDataMinimal dataMinimal = untersuchung.getUntersuchungImpfungenDataMinimal();
        if (dataMinimal == null) {
            dataMinimal = forUntersuchung(untersuchung);
        }
        AnlassConfig config = untersuchung.getUntersuchung().getEinladung().getAnlass().getAnlassConfig();
        if (ValueEnhancedUtil.isVisibleAndNull(config.getImpfungenDataMinimalImpfausweisGeprueft(), dataMinimal.getImpfausweisGeprueft(), ImpfausweisGeprueft.class)) {
            dataMinimal.setImpfausweisGeprueft(SingleValueEnhanced.of(ImpfausweisGeprueft.NEIN, null));
        }
        if (ValueEnhancedUtil.isVisibleAndNull(config.getImpfungenDataMinimalImmunsuppression(), dataMinimal.getImmunsuppression(), Immunsuppression.class)) {
            dataMinimal.setImmunsuppression(SingleValueEnhanced.of(Immunsuppression.UNBEKANNT, null));
        }
        if (ValueEnhancedUtil.isVisibleAndNull(config.getImpfungenDataMinimalMasernImpfstatus(), dataMinimal.getMasernImpfstatus(), ImpfStatusMinimal.class)) {
            dataMinimal.setMasernImpfstatus(SingleValueEnhanced.of(ImpfStatusMinimal.UNBEKANNT, null));
        }
        if (ValueEnhancedUtil.isVisibleAndNull(config.getImpfungenDataMinimalHpvImpfstatus(), dataMinimal.getHpvImpfstatus(), ImpfStatusMinimal.class)) {
            dataMinimal.setHpvImpfstatus(SingleValueEnhanced.of(ImpfStatusMinimal.UNBEKANNT, null));
        }
        if (ValueEnhancedUtil.isVisibleAndNull(config.getImpfungenDataMinimalImpfempfehlung(), dataMinimal.getImpfempfehlung(), ImpfempfehlungMinimal.class)) {
            dataMinimal.setImpfempfehlung(SingleValueEnhanced.of(ImpfempfehlungMinimal.UNBEKANNT, null));
        }
        if (ValueEnhancedUtil.isVisibleAndNull(config.getImpfungenDataMinimalImpfungDurchgefuehrt(), dataMinimal.getImpfungDurchgefuehrt(), ImpfungDurchgefuehrtMinimal.class)) {
            dataMinimal.setImpfungDurchgefuehrt(SingleValueEnhanced.of(ImpfungDurchgefuehrtMinimal.UNBEKANNT, null));
        }
        return dataMinimal;
    }
}
