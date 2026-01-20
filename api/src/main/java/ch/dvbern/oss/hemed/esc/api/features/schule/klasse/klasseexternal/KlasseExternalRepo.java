package ch.dvbern.oss.hemed.esc.api.features.schule.klasse.klasseexternal;

import java.util.List;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.QKlasse;
import ch.dvbern.oss.hemed.esc.api.shared.externalsystem.ExternalSystemIdDTO;
import ch.dvbern.oss.hemed.esc.api.shared.externalsystem.QExternalSystemIdDTO;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class KlasseExternalRepo extends BaseRepo<KlasseExternal> {

    @NonNull
    public List<ExternalSystemIdDTO> findAllExternalIds() {
        return this.db.select(
                new QExternalSystemIdDTO(QKlasseExternal.klasseExternal.klasseId, QKlasse.klasse.id))
            .from(QKlasseExternal.klasseExternal)
            .innerJoin(QKlasse.klasse).on(QKlasseExternal.klasseExternal.klasse.eq(QKlasse.klasse))
            .fetch();
    }

    @NonNull
    public Optional<KlasseExternal> findExternalKlasseForKlasse(@NonNull ID<Klasse> id) {
        return this.db.select(QKlasseExternal.klasseExternal)
            .from(QKlasseExternal.klasseExternal)
            .where(QKlasseExternal.klasseExternal.klasse.id.eq(id.getUUID()))
            .fetchOne();
    }

    @NonNull
    public KlasseExternal getExternalKlasseForKlasse(@NonNull ID<Klasse> id) {
        return this.findExternalKlasseForKlasse(id)
            .orElseThrow(() -> new AppFailureException(AppFailureMessage.entityNotFound(id)));
    }
}
