package ch.dvbern.oss.hemed.esc.api.features.organisation;

import java.util.EnumSet;
import java.util.Set;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class OrganisationRollenService {

    private final OrganisationRollenRepo organisationRollenRepo;

    @NonNull
    public OrganisationRollen persist(@NonNull OrganisationRollen organisationRollen) {
        organisationRollenRepo.persist(organisationRollen);
        return organisationRollen;
    }

    @NonNull
    public OrganisationRollen persistWithOptimisticLocking(
        @NonNull OrganisationRollen organisationRollen,
        @NonNull Long versionFromFrontend
    ) {
        organisationRollenRepo.persistWithOptimisticLocking(organisationRollen, versionFromFrontend);
        return organisationRollen;
    }

    @NonNull
    public OrganisationRollen createForOrganisation(
        @NonNull Organisation organisation,
        @NonNull Benutzer benutzer,
        @NonNull EnumSet<FachRolle> fachrollen
    ) {
        OrganisationRollen organisationRollen = new OrganisationRollen();
        organisationRollen.setOrganisation(organisation);
        organisationRollen.setBenutzer(benutzer);
        organisationRollen.setFachRollen(fachrollen);

        Set<OrganisationRollen> mitglieder = organisation.getMitglieder();
        mitglieder.add(organisationRollen);
        organisation.setMitglieder(mitglieder);

        return persist(organisationRollen);
    }
}
