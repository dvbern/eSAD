package ch.dvbern.oss.hemed.esc.api.features.organisation;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class OrganisationRepo extends BaseRepo<Organisation> {

    @NonNull
    public Organisation getZentraleOrganisation() {
        return db.selectFrom(QOrganisation.organisation)
            .fetchOne()
            .orElseThrow(() -> new AppFailureException(AppFailureMessage.zentraleOrganisationNotUnique()));
    }

    @NonNull
    public List<Organisation> findAll() {
        return db.selectFrom(QOrganisation.organisation)
            .orderBy(QOrganisation.organisation.timestampErstellt.desc())
            .fetch();
    }
}
