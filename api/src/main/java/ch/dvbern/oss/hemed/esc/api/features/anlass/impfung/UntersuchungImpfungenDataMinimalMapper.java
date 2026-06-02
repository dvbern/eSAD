package ch.dvbern.oss.hemed.esc.api.features.anlass.impfung;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.HasUntersuchungIDWrapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfungenDataMinimalJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfungenDataMinimalSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.Immunsuppression;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfStatusMinimal;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfausweisGeprueft;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfempfehlungMinimal;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfungDurchgefuehrtMinimal;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhancedMapper;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Consumer;

@JaxMapper
@RequiredArgsConstructor
public class UntersuchungImpfungenDataMinimalMapper {

    private final UntersuchungService untersuchungService;
    private final UntersuchungImpfungenService untersuchungImpfungenService;
    private final UntersuchungImpfungenDataMinimalService impfungenDataMinimalService;

    @Nullable
    public static UntersuchungImpfungenDataMinimalJson toJson(
        @Nullable UntersuchungImpfungenDataMinimal dataMinimal
    ) {
        if (dataMinimal == null) {
            return null;
        }
        return new UntersuchungImpfungenDataMinimalJson(
            dataMinimal.getId(),
            EntityInfoJson.fromEntity(dataMinimal),
            new UntersuchungImpfungenDataMinimalSaveJson(
                SingleValueEnhancedMapper.toJson(ImpfausweisGeprueft.class, dataMinimal.getImpfausweisGeprueft()),
                SingleValueEnhancedMapper.toJson(Immunsuppression.class, dataMinimal.getImmunsuppression()),
                SingleValueEnhancedMapper.toJson(ImpfStatusMinimal.class, dataMinimal.getMasernImpfstatus()),
                dataMinimal.getMasernAnzahlImpfungen(),
                SingleValueEnhancedMapper.toJson(ImpfStatusMinimal.class, dataMinimal.getHpvImpfstatus()),
                dataMinimal.getHpvAnzahlImpfungen(),
                SingleValueEnhancedMapper.toJson(ImpfempfehlungMinimal.class, dataMinimal.getImpfempfehlung()),
                SingleValueEnhancedMapper.toJson(ImpfungDurchgefuehrtMinimal.class, dataMinimal.getImpfungDurchgefuehrt()),
                dataMinimal.getVersion()
            )
        );
    }

    @NonNull
    private UntersuchungImpfungenDataMinimal toEntity(
        @NonNull UntersuchungImpfungen impfungen,
        @Nullable UntersuchungImpfungenDataMinimal dataMinimal,
        @NonNull UntersuchungImpfungenDataMinimalSaveJson saveJson
    ) {
        if (dataMinimal == null) {
            dataMinimal = new UntersuchungImpfungenDataMinimal();
            dataMinimal.setUntersuchungImpfungen(impfungen);
            impfungen.setUntersuchungImpfungenDataMinimal(dataMinimal);
        }
        dataMinimal.setImpfausweisGeprueft(SingleValueEnhancedMapper.toEntity(ImpfausweisGeprueft.class, saveJson.getImpfausweisGeprueft()));
        dataMinimal.setImmunsuppression(SingleValueEnhancedMapper.toEntity(Immunsuppression.class, saveJson.getImmunsuppression()));
        dataMinimal.setMasernImpfstatus(SingleValueEnhancedMapper.toEntity(ImpfStatusMinimal.class, saveJson.getMasernImpfstatus()));
        dataMinimal.setMasernAnzahlImpfungen(saveJson.getMasernAnzahlImpfungen());
        dataMinimal.setHpvImpfstatus(SingleValueEnhancedMapper.toEntity(ImpfStatusMinimal.class, saveJson.getHpvImpfstatus()));
        dataMinimal.setHpvAnzahlImpfungen(saveJson.getHpvAnzahlImpfungen());
        dataMinimal.setImpfempfehlung(SingleValueEnhancedMapper.toEntity(ImpfempfehlungMinimal.class, saveJson.getImpfempfehlung()));
        dataMinimal.setImpfungDurchgefuehrt(SingleValueEnhancedMapper.toEntity(ImpfungDurchgefuehrtMinimal.class, saveJson.getImpfungDurchgefuehrt()));
        return dataMinimal;
    }

    @NonNull
    public UntersuchungImpfungenDataMinimalJson save(
        @NonNull HasUntersuchungIDWrapper wrappedId,
        @NonNull UntersuchungImpfungenDataMinimalSaveJson saveJson,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(wrappedId);
        hasAuthorizationUntersuchung.accept(untersuchung);
        UntersuchungImpfungen impfungen =
            untersuchungImpfungenService.getOrCreateUntersuchungImpfungen(untersuchung.getId());
        impfungen.disableOptimisticLockingCheck();
        UntersuchungImpfungenDataMinimal dataMinimal = impfungen.getUntersuchungImpfungenDataMinimal();
        UntersuchungImpfungenDataMinimal entity = toEntity(impfungen, dataMinimal, saveJson);
        return toJson(impfungenDataMinimalService.persistWithOptimisticLocking(
            entity,
            saveJson.getVersion()));
    }
}
