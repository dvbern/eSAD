package ch.dvbern.oss.hemed.esc.api.features.benutzer;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.BenutzerBerechtigung;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.QBenutzer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.QBenutzerBerechtigung;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class BenutzerberechtigungRepo extends BaseRepo<BenutzerBerechtigung> {


    @NonNull
    public List<BenutzerBerechtigung> findBenutzerBerechtigungenByBenutzerId(@NonNull ID<Benutzer> benutzerID) {
        return db.select(QBenutzerBerechtigung.benutzerBerechtigung)
            .from(QBenutzerBerechtigung.benutzerBerechtigung)
            .innerJoin(QBenutzerBerechtigung.benutzerBerechtigung.benutzer, QBenutzer.benutzer)
            .where(QBenutzer.benutzer.id.eq(benutzerID.getUUID()))
            .fetch();
    }


}
