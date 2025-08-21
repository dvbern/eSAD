package ch.dvbern.oss.hemed.esc.api.features.anlass;

import java.util.List;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.DossierService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class UntersuchungImpfstatusKrankheitService {

	private final UntersuchungImpfstatusKrankheitRepo impfstatusKrankheitRepo;
	private final DossierService dossierService;

	@NonNull
	public UntersuchungImpfstatusKrankheit persist(@NonNull UntersuchungImpfstatusKrankheit impfstatusKrankheit) {
		impfstatusKrankheitRepo.persist(impfstatusKrankheit);
		checkImmunitaetenForDossier(impfstatusKrankheit);
		return impfstatusKrankheit;
	}

	private void checkImmunitaetenForDossier(@NonNull UntersuchungImpfstatusKrankheit impfstatusKrankheit) {
        Dossier dossier = dossierService.getById(impfstatusKrankheit.getUntersuchungImpfungen()
            .getUntersuchung()
            .getEinladung()
            .getDossier()
            .getId());
		if (impfstatusKrankheit.getKrankheit() == Krankheit.MASERN && impfstatusKrankheit.isImpfschutzGegeben()) {
			dossier.setImmunitaetMasern(true);
			dossierService.persist(dossier);
		}
		if (impfstatusKrankheit.getKrankheit() == Krankheit.VARIZELLEN && impfstatusKrankheit.isImpfschutzGegeben()) {
			dossier.setImmunitaetVarizellen(true);
			dossierService.persist(dossier);
		}
	}

	@NonNull
	public List<UntersuchungImpfstatusKrankheit> findByUntersuchung(@NonNull ID<Untersuchung> untersuchungID) {
		return impfstatusKrankheitRepo.findByUntersuchung(untersuchungID);
	}

	@NonNull
	public Optional<UntersuchungImpfstatusKrankheit> findByUntersuchungAndKrankheit(
			@NonNull ID<UntersuchungImpfungen> untersuchungImpfungenID,
			@NonNull Krankheit krankheit) {
		return impfstatusKrankheitRepo.findByUntersuchungAndKrankheit(untersuchungImpfungenID, krankheit);
	}

    @NonNull
    public Optional<UntersuchungImpfstatusKrankheit> findMostRecentlyChangedByDossier(@NonNull ID<Kind> kindId) {
        return impfstatusKrankheitRepo.findMostRecentlyChangedByDossier(kindId);
    }
}
