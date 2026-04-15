package ch.dvbern.oss.hemed.esc.api.features.anlass.signatur;

import java.time.LocalDateTime;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Service
@RequiredArgsConstructor
public class UntersuchungSignaturService {

    private final UntersuchungSignaturRepo signaturRepo;
    private final BenutzerService benutzerService;
    private final AppClock appClock;

    @NonNull
    public UntersuchungSignatur persist(@NonNull UntersuchungSignatur signatur) {
        signaturRepo.persist(signatur);
        return signatur;
    }

    @NonNull
    public UntersuchungSignatur getById(@NonNull ID<UntersuchungSignatur> untersuchungSignaturID) {
        return signaturRepo.getById(untersuchungSignaturID);
    }

    @NonNull
    public UntersuchungSignatur signieren(
        @NonNull Untersuchung untersuchung
    ) {
        return changeSignatur(
            untersuchung,
            benutzerService.getCurrentBenutzerOrThrowException(),
            appClock.localDateTime().now());
    }

    @NonNull
    public UntersuchungSignatur resetSignatur(
        @NonNull Untersuchung untersuchung
    ) {
       return changeSignatur(
           untersuchung,
           null,
           null);
    }

    @NonNull
    private UntersuchungSignatur changeSignatur(
        @NonNull Untersuchung untersuchung,
        @Nullable Benutzer person,
        @Nullable LocalDateTime timestampVisiert
    ) {
        UntersuchungSignatur signatur = untersuchung.getUntersuchungSignatur();
        if (signatur == null) {
            signatur = UntersuchungSignatur.forUntersuchung(untersuchung);
        }
        signatur.setPerson(person);
        signatur.setTimestampVisiert(timestampVisiert);
        signatur.disableOptimisticLockingCheck();
        signaturRepo.persist(signatur);
        return signatur;
    }
}
