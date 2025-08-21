package ch.dvbern.oss.hemed.esc.api.features.example;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ExampleRepo extends BaseRepo<Benutzer> {

}
