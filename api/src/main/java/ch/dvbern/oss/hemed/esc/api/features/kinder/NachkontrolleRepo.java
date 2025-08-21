package ch.dvbern.oss.hemed.esc.api.features.kinder;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NachkontrolleRepo extends BaseRepo<Nachkontrolle> {

    @NonNull
    public Optional<Nachkontrolle> findByDossierId(@NonNull ID<Dossier> dossierId) {
        return db.selectFrom(QNachkontrolle.nachkontrolle)
            .where(QNachkontrolle.nachkontrolle.dossier.id.eq(dossierId.getUUID()))
            .fetchOne();
    }
}
