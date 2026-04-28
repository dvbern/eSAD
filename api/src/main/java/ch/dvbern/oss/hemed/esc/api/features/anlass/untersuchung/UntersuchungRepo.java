package ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import jakarta.persistence.EntityGraph;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class UntersuchungRepo extends BaseRepo<Untersuchung> {

    @NonNull
    public Untersuchung getByIdWithLoadedSubuntersuchungen(@NonNull ID<Untersuchung> untersuchungId) {
        EntityGraph<Untersuchung> untersuchungGraph = db.getEntityManager().createEntityGraph(Untersuchung.class);
        untersuchungGraph.addAttributeNodes(QUntersuchung.untersuchung.untersuchungKindDaten.getMetadata().getName());
        untersuchungGraph.addAttributeNodes(QUntersuchung.untersuchung.untersuchungArztBeurteilung.getMetadata().getName());
        untersuchungGraph.addAttributeNodes(QUntersuchung.untersuchung.untersuchungArztBemerkung.getMetadata().getName());
        untersuchungGraph.addAttributeNodes(QUntersuchung.untersuchung.untersuchungDurchfuehrung.getMetadata().getName());
        untersuchungGraph.addAttributeNodes(QUntersuchung.untersuchung.untersuchungHoerenAudiogramm.getMetadata().getName());
        untersuchungGraph.addAttributeNodes(QUntersuchung.untersuchung.untersuchungHoerenOtoskopie.getMetadata().getName());
        untersuchungGraph.addAttributeNodes(QUntersuchung.untersuchung.untersuchungSehenBeurteilung.getMetadata().getName());
        untersuchungGraph.addAttributeNodes(QUntersuchung.untersuchung.untersuchungSehenBemerkungFragebogen.getMetadata().getName());
        untersuchungGraph.addAttributeNodes(QUntersuchung.untersuchung.untersuchungGroesseGewicht.getMetadata().getName());
        untersuchungGraph.addAttributeNodes(QUntersuchung.untersuchung.untersuchungImpfungen.getMetadata().getName());
        untersuchungGraph.addAttributeNodes(QUntersuchung.untersuchung.untersuchungTriage.getMetadata().getName());
        untersuchungGraph.addAttributeNodes(QUntersuchung.untersuchung.untersuchungMassnahmen.getMetadata().getName());
        return db.selectFrom(QUntersuchung.untersuchung)
            .where(QUntersuchung.untersuchung.id.eq(untersuchungId.getUUID()))
            .applyEntityGraph(untersuchungGraph)
            .fetchOne()
            .orElseThrow(() -> new AppFailureException(AppFailureMessage.entityNotFound(untersuchungId)));
    }
}
