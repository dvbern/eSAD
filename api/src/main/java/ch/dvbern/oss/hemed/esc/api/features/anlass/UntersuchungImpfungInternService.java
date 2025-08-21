package ch.dvbern.oss.hemed.esc.api.features.anlass;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
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
public class UntersuchungImpfungInternService {

	private final UntersuchungImpfungInternRepo untersuchungImpfungInternRepo;
	private final DossierService dossierService;

	@NonNull
	public UntersuchungImpfungIntern persist(@NonNull UntersuchungImpfungIntern impfstatusKrankheit) {
		untersuchungImpfungInternRepo.persist(impfstatusKrankheit);
		checkImmunitaetenForDossier(impfstatusKrankheit);
		return impfstatusKrankheit;
	}

	private void checkImmunitaetenForDossier(@NonNull UntersuchungImpfungIntern impfungIntern) {
        Dossier dossier = dossierService.getById(impfungIntern.getUntersuchungImpfungen()
            .getUntersuchung()
            .getEinladung()
            .getDossier()
            .getId());
		Set<UntersuchungImpfungKrankheitImmunitaet> immunitaeten = impfungIntern.getKrankheitenImmunitaeten();
		if (immunitaeten.stream().anyMatch(i -> i.getKrankheit() == Krankheit.MASERN
                && i.isImmunitaetGenuegend())) {
			dossier.setImmunitaetMasern(true);
			dossierService.persist(dossier);
		}
		if (immunitaeten.stream().anyMatch(i -> i.getKrankheit() == Krankheit.VARIZELLEN
                && i.isImmunitaetGenuegend())) {
			dossier.setImmunitaetVarizellen(true);
			dossierService.persist(dossier);
		}
	}

	@NonNull
	public List<UntersuchungImpfungIntern> findByUntersuchung(@NonNull ID<Untersuchung> untersuchungID) {
		return untersuchungImpfungInternRepo.findByUntersuchung(untersuchungID);
	}

	@NonNull
	public UntersuchungImpfungIntern getById(@NonNull ID<UntersuchungImpfungIntern> id) {
		return untersuchungImpfungInternRepo.getById(id);
	}

	public void remove(@NonNull ID<Anlass> anlassId, @NonNull ID<UntersuchungImpfungIntern> impfungInternID) {
		UntersuchungImpfungIntern impfung = getById(impfungInternID);
		if (impfung.getAnlassImpfungErfasst().getId().equals(anlassId)) {
			untersuchungImpfungInternRepo.remove(impfungInternID);
		} else {
			throw new AppFailureException(AppFailureMessage.impfungInternCannotBeRemoved());
		}
	}

    @NonNull
    public Optional<UntersuchungImpfungIntern> findMostRecentlyChangedByDossier(@NonNull ID<Kind> kindId) {
        return untersuchungImpfungInternRepo.findMostRecentlyChangedByDossier(kindId);
    }
}
