package ch.dvbern.oss.hemed.esc.api.features.kinder.external;

import java.util.List;
import java.util.Optional;

import org.checkerframework.checker.nullness.qual.NonNull;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.QKind;
import ch.dvbern.oss.hemed.esc.api.shared.escada.ExternalSystemIdDTO;
import ch.dvbern.oss.hemed.esc.api.shared.escada.QExternalSystemIdDTO;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class KindExternalRepo extends BaseRepo<KindExternal> {

    @NonNull
    public List<ExternalSystemIdDTO> findAllExternalIds() {
        return this.db.select(
                new QExternalSystemIdDTO(QKindExternal.kindExternal.schuelerId, QKind.kind.id))
            .from(QKindExternal.kindExternal)
            .innerJoin(QKind.kind).on(QKindExternal.kindExternal.kind.eq(QKind.kind))
            .fetch();
    }

    @NonNull
    public Optional<KindExternal> findExternalKindForKind(@NonNull ID<Kind> id) {
        return this.db.select(QKindExternal.kindExternal)
            .from(QKindExternal.kindExternal)
            .where(QKindExternal.kindExternal.kind.id.eq(id.getUUID()))
            .fetchOne();
    }

    @NonNull
    public KindExternal getExternalKindForKind(@NonNull ID<Kind> id) {
        return this.findExternalKindForKind(id)
            .orElseThrow(() -> new AppFailureException(AppFailureMessage.entityNotFound(id)));
    }
} 