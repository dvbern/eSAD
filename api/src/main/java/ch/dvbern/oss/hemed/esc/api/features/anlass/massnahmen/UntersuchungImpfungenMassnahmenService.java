package ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungImpfungenService;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.AutoAufgabeService;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class UntersuchungImpfungenMassnahmenService {

    private final UntersuchungImpfungenMassnahmenRepo massnahmenRepo;
    private final AutoAufgabeService autoAufgabeService;
    private final EinladungService einladungService;
    private final UntersuchungImpfungenService untersuchungImpfungenService;


    @NonNull
    public UntersuchungImpfungenMassnahmen persist(@NonNull UntersuchungImpfungenMassnahmen massnahmen) {
        massnahmenRepo.persist(massnahmen);
        return afterPersist(massnahmen);
    }

    @NonNull
    public UntersuchungImpfungenMassnahmen persistWithOptimisticLocking(@NonNull UntersuchungImpfungenMassnahmen massnahmen, @NonNull Long versionFromFrontend) {
        UntersuchungImpfungenMassnahmen persistedEntity = massnahmenRepo.persistWithOptimisticLocking(massnahmen, versionFromFrontend);
        return afterPersist(persistedEntity);
    }

    @NonNull
    private UntersuchungImpfungenMassnahmen afterPersist(@NonNull UntersuchungImpfungenMassnahmen massnahmen) {
        UntersuchungImpfungen impfungen =
            untersuchungImpfungenService.getById(massnahmen.getUntersuchungImpfungen().getId());
        impfungen.disableOptimisticLockingCheck();
        impfungen.setUntersuchungImpfungenMassnahmen(massnahmen);
        createOrUpdateAufgaben(massnahmen);
        return massnahmen;
    }

    private void createOrUpdateAufgaben(@NonNull UntersuchungImpfungenMassnahmen massnahmen) {
        massnahmen.disableOptimisticLockingCheck();
        Einladung einladung = massnahmen.getUntersuchungImpfungen().getUntersuchung().getEinladung();
        if (massnahmen.getInformationEltern() != null && massnahmen.getInformationEltern() != InformationEltern.KEINE) {
            autoAufgabeService.createOrUpdateAufgabeMassnahmeImpfenInformationEltern(massnahmen, einladung);
        } else {
            autoAufgabeService.aufgabeAbschliessen(massnahmen.getAufgabeInformationEltern());
        }
        if (massnahmen.isInformationHausarzt()) {
            autoAufgabeService.createOrUpdateAufgabeMassnahmeImpfenInformationHausarzt(massnahmen, einladung);
        } else {
            autoAufgabeService.aufgabeAbschliessen(massnahmen.getAufgabeInformationHausarzt());
        }
        if (massnahmen.isRueckfrageHausarzt()) {
            autoAufgabeService.createOrUpdateAufgabeMassnahmeImpfenRueckfrageHausarzt(massnahmen, einladung);
        } else {
            autoAufgabeService.aufgabeAbschliessen(massnahmen.getAufgabeRueckfrageHausarzt());
        }
    }

    public void einzelTerminErstellen(@NonNull ID<Einladung> einladungId) {
        Einladung einladung = einladungService.getById(einladungId);
        autoAufgabeService.createAufgabeMassnahmeImpfenEinzelTerminErstellen(einladung);
    }
}
