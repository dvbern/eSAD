package ch.dvbern.oss.hemed.esc.api.features.kinder.external;

import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class KindExternalService {

    private final KindExternalRepo kindExternalRepo;

    public void removeForKind(@NonNull ID<Kind> kindID) {
        Optional<KindExternal> externalKindOptional = kindExternalRepo.findExternalKindForKind(kindID);
        externalKindOptional.ifPresent(kindExternal -> kindExternalRepo.remove(kindExternal.getId()));
    }
}
