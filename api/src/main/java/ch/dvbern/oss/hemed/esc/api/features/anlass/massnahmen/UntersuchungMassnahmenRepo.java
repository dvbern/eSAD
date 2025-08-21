package ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen;

import java.util.List;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.QAnlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.QEinladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.QUntersuchung;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.QDossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.QKind;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class UntersuchungMassnahmenRepo extends BaseRepo<UntersuchungMassnahmen> {

    @NonNull
    public Optional<UntersuchungMassnahmen> findLatestByKind(@NonNull ID<Kind> kindID) {
        List<UntersuchungMassnahmen> fetch = db.selectFrom(QUntersuchungMassnahmen.untersuchungMassnahmen)
            .innerJoin(QUntersuchungMassnahmen.untersuchungMassnahmen.untersuchung, QUntersuchung.untersuchung)
            .innerJoin(QUntersuchung.untersuchung.einladung, QEinladung.einladung)
            .innerJoin(QEinladung.einladung.dossier, QDossier.dossier)
            .innerJoin(QEinladung.einladung.anlass, QAnlass.anlass)
            .innerJoin(QDossier.dossier.kind, QKind.kind)
            .where(QKind.kind.id.eq(kindID.getUUID()))
            .orderBy(QAnlass.anlass.datum.desc())
            .limit(1)
            .fetch();
        if (fetch.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(fetch.getFirst());
    }
}
