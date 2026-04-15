package ch.dvbern.oss.hemed.esc.api.features.benutzer;

import java.util.EnumSet;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class SystemRollenService {

    private final SystemRollenRepo systemRollenRepo;

    @NonNull
    public SystemRollen persist(@NonNull SystemRollen systemRollen) {
        systemRollenRepo.persist(systemRollen);
        return systemRollen;
    }

    @NonNull
    public SystemRollen persistWithOptimisticLocking(
        @NonNull SystemRollen systemRollen,
        @NonNull Long versionFromFrontend) {
        systemRollenRepo.persistWithOptimisticLocking(systemRollen, versionFromFrontend);
        return systemRollen;
    }

    @NonNull
    public SystemRollen createNewSystemRollen(@NonNull Benutzer benutzer, @NonNull EnumSet<FachRolle> fachRollen) {
        SystemRollen systemRollen = new SystemRollen();
        systemRollen.setBenutzer(benutzer);
        systemRollen.setFachRollen(fachRollen);

        return persist(systemRollen);
    }
}
