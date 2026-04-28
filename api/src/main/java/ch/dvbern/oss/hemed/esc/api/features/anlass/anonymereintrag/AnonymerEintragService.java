package ch.dvbern.oss.hemed.esc.api.features.anlass.anonymereintrag;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnonymerEintragService {

    private final AnonymerEintragRepo anonymerEintragRepo;

    public AnonymerEintrag getById(ID<AnonymerEintrag> anonymerEintragID) {
        return anonymerEintragRepo.getById(anonymerEintragID);
    }
}
