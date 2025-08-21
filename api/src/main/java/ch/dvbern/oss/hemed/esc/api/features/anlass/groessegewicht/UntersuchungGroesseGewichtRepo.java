package ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht;

import java.time.LocalDate;
import java.util.List;

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
public class UntersuchungGroesseGewichtRepo extends BaseRepo<UntersuchungGroesseGewicht> {

    @NonNull
    public List<UntersuchungGroesseGewicht> findPreviousByKind(
        @NonNull ID<Kind> kindID,
        @NonNull LocalDate stichtag
    ) {
        return db.selectFrom(QUntersuchungGroesseGewicht.untersuchungGroesseGewicht)
            .innerJoin(QUntersuchungGroesseGewicht.untersuchungGroesseGewicht.untersuchung, QUntersuchung.untersuchung)
            .innerJoin(QUntersuchung.untersuchung.einladung, QEinladung.einladung)
            .innerJoin(QEinladung.einladung.dossier, QDossier.dossier)
            .innerJoin(QEinladung.einladung.anlass, QAnlass.anlass)
            .innerJoin(QDossier.dossier.kind, QKind.kind)
            .where(QKind.kind.id.eq(kindID.getUUID())
                .and(QAnlass.anlass.datum.before(stichtag)))
            .fetch();
    }
}
