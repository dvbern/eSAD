package ch.dvbern.oss.hemed.esc.api.shared.validator;

import java.util.Set;

import ch.dvbern.oss.hemed.esc.api.features.benutzer.SystemRollen;
import ch.dvbern.oss.hemed.esc.api.features.organisation.OrganisationRollen;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CheckBenutzerFachRollenValidator extends AbstractValidator<CheckBenutzerFachRollen, Benutzer> {

    @Override
    public boolean isEntityValid(@NonNull Benutzer benutzer) {
        Set<OrganisationRollen> organisationsRollen = benutzer.getOrganisationRollen();
        SystemRollen systemRollen = benutzer.getSystemRollen();
        return organisationsRollen.isEmpty() || systemRollen == null;
    }
}
