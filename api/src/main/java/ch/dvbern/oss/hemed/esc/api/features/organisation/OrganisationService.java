package ch.dvbern.oss.hemed.esc.api.features.organisation;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class OrganisationService {

    private final OrganisationRepo organisationRepo;

    @NonNull
    public Organisation persist(@NonNull Organisation organisation) {
        organisationRepo.persist(organisation);
        return organisation;
    }

    @NonNull
    public Organisation persistWithOptimisticLocking(
        @NonNull Organisation organisation,
        @NonNull Long versionFromFrontend
    ) {
        organisationRepo.persistWithOptimisticLocking(organisation, versionFromFrontend);
        return organisation;
    }

    @NonNull
    public List<Organisation> findAll() {
        return organisationRepo.findAll();
    }

    @NonNull
    public Organisation getById(ID<Organisation> id) {
        return organisationRepo.getById(id);
    }

    public void remove(@NonNull ID<Organisation> organisationID) {
        organisationRepo.remove(organisationID);
    }

    @NonNull
    public Organisation getZentraleOrganisation() {
        return organisationRepo.getZentraleOrganisation();
    }

    @NonNull
    public Organisation createOrganisationForBenutzer(@NonNull Benutzer benutzer) {
        Organisation organisation = new Organisation();
        organisation.setVerantwortlicherArzt(benutzer);
        return persist(organisation);
    }
}
