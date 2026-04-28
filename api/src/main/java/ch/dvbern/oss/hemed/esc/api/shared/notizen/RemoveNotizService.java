package ch.dvbern.oss.hemed.esc.api.shared.notizen;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungRepo;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class RemoveNotizService {

    private final NotizService notizService;
    private final EinladungRepo einladungRepo;

    public void removeNotizWithUnlink(@NonNull ID<Notiz> notizId) {
        einladungRepo.unlinkNotizFromEinladung(notizId);
        notizService.remove(notizId, true); // in this case we always want to delete the file in s3 as well
    }
}
