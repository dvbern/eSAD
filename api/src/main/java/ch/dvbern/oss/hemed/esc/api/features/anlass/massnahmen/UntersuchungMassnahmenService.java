package ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen;

import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.AutoAufgabeService;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class UntersuchungMassnahmenService {

    private final UntersuchungMassnahmenRepo massnahmenRepo;
    private final AutoAufgabeService autoAufgabeService;

    @NonNull
    public UntersuchungMassnahmen persist(@NonNull UntersuchungMassnahmen massnahmen) {
        massnahmenRepo.persist(massnahmen);
        createOrUpdateAufgaben(massnahmen);
        return massnahmen;
    }

    @NonNull
    public UntersuchungMassnahmen persistWithOptimisticLocking(@NonNull UntersuchungMassnahmen entity, @NonNull Long versionFromFrontend) {
        UntersuchungMassnahmen persisted = massnahmenRepo.persistWithOptimisticLocking(entity, versionFromFrontend);
        createOrUpdateAufgaben(persisted);
        return persisted;
    }

    private void createOrUpdateAufgaben(@NonNull UntersuchungMassnahmen massnahmen) {
        massnahmen.disableOptimisticLockingCheck();
        Anlass anlass = massnahmen.getUntersuchung().getEinladung().getAnlass();
        Kind kind = massnahmen.getUntersuchung().getEinladung().getDossier().getKind();
        if (massnahmen.getInformationEltern() != null && massnahmen.getInformationEltern() != InformationEltern.KEINE) {
            autoAufgabeService.createOrUpdateAufgabeMassnahmeInformationEltern(massnahmen, kind, anlass);
        } else {
            autoAufgabeService.aufgabeAbschliessen(massnahmen.getAufgabeInformationEltern());
        }
        if (massnahmen.isUeberweisungHausarzt()) {
            autoAufgabeService.createOrUpdateAufgabeMassnahmeUeberweisungHausarzt(massnahmen, kind, anlass);
        } else {
            autoAufgabeService.aufgabeAbschliessen(massnahmen.getAufgabeUeberweisungHausarzt());
        }
        if (massnahmen.isUeberweisungAugenarzt()) {
            autoAufgabeService.createOrUpdateAufgabeMassnahmeUeberweisungAugenarzt(massnahmen, kind, anlass);
        } else {
            autoAufgabeService.aufgabeAbschliessen(massnahmen.getAufgabeUeberweisungAugenarzt());
        }
        if (massnahmen.getNachkontrolle().hasAnyNachkontrolle()) {
            autoAufgabeService.createOrUpdateAufgabeMassnahmeNachkontrolle(massnahmen, kind, anlass);
        } else {
            autoAufgabeService.aufgabeAbschliessen(massnahmen.getAufgabeNachkontrolle());
        }
    }

    @NonNull
    public UntersuchungMassnahmen getById(@NonNull ID<UntersuchungMassnahmen> massnahmenID) {
        return massnahmenRepo.getById(massnahmenID);
    }

    @NonNull
    public Optional<UntersuchungMassnahmen> findLatestByKind(@NonNull ID<Kind> kindID) {
        return massnahmenRepo.findLatestByKind(kindID);
    }

    public void untersuchungAbschliessen(@NonNull Untersuchung untersuchung) {
        UntersuchungMassnahmen massnahmen = UntersuchungMassnahmen.forUntersuchungAbschliessen(untersuchung);
        massnahmen.disableOptimisticLockingCheck();
        persist(massnahmen);
    }
}
