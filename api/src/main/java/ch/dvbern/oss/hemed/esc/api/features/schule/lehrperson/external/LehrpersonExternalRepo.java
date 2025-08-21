package ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.external;

import java.util.List;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.Lehrperson;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.QLehrperson;
import ch.dvbern.oss.hemed.esc.api.shared.escada.ExternalSystemIdDTO;
import ch.dvbern.oss.hemed.esc.api.shared.escada.QExternalSystemIdDTO;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class LehrpersonExternalRepo extends BaseRepo<LehrpersonExternal> {

    @NonNull
    public List<ExternalSystemIdDTO> findAllExternalIds() {
        return this.db.select(
                new QExternalSystemIdDTO(
                    QLehrpersonExternal.lehrpersonExternal.klasseLehrpersonId,
                    QLehrperson.lehrperson.id)
            )
            .from(QLehrpersonExternal.lehrpersonExternal)
            .innerJoin(QLehrperson.lehrperson)
            .on(QLehrpersonExternal.lehrpersonExternal.lehrperson
                .eq(QLehrperson.lehrperson))
            .fetch();
    }

    @NonNull
    public Optional<LehrpersonExternal> findExternalForLehrperson(
        @NonNull ID<Lehrperson> id
    ) {
        return this.db.select(QLehrpersonExternal.lehrpersonExternal)
            .from(QLehrpersonExternal.lehrpersonExternal)
            .where(QLehrpersonExternal.lehrpersonExternal.lehrperson.id.eq(id.getUUID()))
            .fetchOne();
    }

    @NonNull
    public LehrpersonExternal getExternalForLehrperson(@NonNull ID<Lehrperson> id) {
        return this.findExternalForLehrperson(id)
            .orElseThrow(() -> new AppFailureException(AppFailureMessage.entityNotFound(id)));
    }
}
