package ch.dvbern.oss.hemed.esc.api.shared.benutzer;

import java.util.Objects;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.DossierService;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.json.SchuleSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZustaendigePersonService {

    private final BenutzerService benutzerService;
    private final DossierService dossierService;


    public void resetZustaendigkeitenOnDossierIfChangedOnSchule(
        @NonNull Schule existingSchule,
        @NonNull SchuleSaveJson schuleSaveJson
    ) {
        Benutzer arztFromSchuleNew = benutzerService.getBenutzerIfCorrectRole(schuleSaveJson.getZustaendigArzt(), FachRolle.SCHULAERZTIN);
        Benutzer mpaFromSchuleNew = benutzerService.getBenutzerIfCorrectRole(schuleSaveJson.getZustaendigMPA(), FachRolle.MPA);
        if (!Objects.equals(existingSchule.getZustaendigArzt(), arztFromSchuleNew)
                || !Objects.equals(existingSchule.getZustaendigMPA(), mpaFromSchuleNew)) {
            dossierService.findBySchule(existingSchule.getId()).forEach(dossier -> {
                dossier.setZustaendigArzt(null);
                dossier.setZustaendigMPA(null);
                dossierService.persist(dossier);
            });
        }
    }
}
