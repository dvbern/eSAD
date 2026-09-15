package ch.dvbern.oss.hemed.esc.api.features.anlass.impfung;

import java.util.function.Consumer;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.HasUntersuchungIDWrapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfungenNurUnvollstaendigeJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfungenNurUnvollstaendigeSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfausweisVollstaendig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfschutzGegeben;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;

import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@RequiredArgsConstructor
public class UntersuchungImpfungenNurUnvollstaendigeMapper {

    private final UntersuchungService untersuchungService;
    private final UntersuchungImpfungenService untersuchungImpfungenService;
    private final UntersuchungImpfungenNurUnvollstaendigeService impfungenNurUnvollstaendigeService;
    private final UntersuchungImpfstatusKrankheitService impfstatusKrankheitService;

    @Nullable
    public static UntersuchungImpfungenNurUnvollstaendigeJson toJson(
        @Nullable UntersuchungImpfungenNurUnvollstaendige nurUnvollstaendige
    ) {
        if (nurUnvollstaendige == null) {
            return null;
        }
        return new UntersuchungImpfungenNurUnvollstaendigeJson(
            nurUnvollstaendige.getId(),
            EntityInfoJson.fromEntity(nurUnvollstaendige),
            new UntersuchungImpfungenNurUnvollstaendigeSaveJson(
                nurUnvollstaendige.getImpfausweisVollstaendig(),
                nurUnvollstaendige.getImpfausweisGrundUnvollstaendig(),
                nurUnvollstaendige.getVersion()
            )
        );
    }

    @NonNull
    private UntersuchungImpfungenNurUnvollstaendige toEntity(
        @NonNull UntersuchungImpfungen impfungen,
        @Nullable UntersuchungImpfungenNurUnvollstaendige nurUnvollstaendige,
        @NonNull UntersuchungImpfungenNurUnvollstaendigeSaveJson saveJson
    ) {
        if (nurUnvollstaendige == null) {
            nurUnvollstaendige = new UntersuchungImpfungenNurUnvollstaendige();
            nurUnvollstaendige.setUntersuchungImpfungen(impfungen);
            impfungen.setUntersuchungImpfungenNurUnvollstaendige(nurUnvollstaendige);
        }
        nurUnvollstaendige.setImpfausweisVollstaendig(saveJson.getImpfausweisVollstaendig());
        nurUnvollstaendige.setImpfausweisGrundUnvollstaendig(saveJson.getImpfausweisGrundUnvollstaendig());
        return nurUnvollstaendige;
    }

    @NonNull
    public UntersuchungImpfungenNurUnvollstaendigeJson save(
        @NonNull HasUntersuchungIDWrapper wrappedId,
        @NonNull UntersuchungImpfungenNurUnvollstaendigeSaveJson saveJson,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(wrappedId);
        hasAuthorizationUntersuchung.accept(untersuchung);
        UntersuchungImpfungen impfungen =
            untersuchungImpfungenService.getOrCreateUntersuchungImpfungen(untersuchung.getId());
        impfungen.disableOptimisticLockingCheck();
        UntersuchungImpfungenNurUnvollstaendige nurUnvollstaendige = impfungen.getUntersuchungImpfungenNurUnvollstaendige();
        UntersuchungImpfungenNurUnvollstaendige entity = toEntity(impfungen, nurUnvollstaendige, saveJson);

        UntersuchungImpfungenNurUnvollstaendige saved =
            impfungenNurUnvollstaendigeService.persistWithOptimisticLocking(
                entity,
                saveJson.getVersion());

        // Bei ImpfausweisVollstaendig = KEINE_ANGABE wird der Impfschutz aller Krankheiten auf KEINE_ANGABE gesetzt.
        if (saved.getImpfausweisVollstaendig() == ImpfausweisVollstaendig.KEINE_ANGABE) {
            impfstatusKrankheitService.setAllTo(untersuchung.getId(), ImpfschutzGegeben.KEINE_ANGABE);
        }
        // Bei JA, wird der Impfschutz auf JA gesetzt
        if (saved.getImpfausweisVollstaendig() == ImpfausweisVollstaendig.JA) {
            impfstatusKrankheitService.setAllTo(untersuchung.getId(), ImpfschutzGegeben.JA);
        }

        return toJson(saved);
    }
}
