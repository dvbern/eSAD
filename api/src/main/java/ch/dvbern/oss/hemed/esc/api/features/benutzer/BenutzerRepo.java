package ch.dvbern.oss.hemed.esc.api.features.benutzer;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

import ch.dvbern.dvbstarter.infrastructure.db.SmartJPAQuery;
import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.organisation.Organisation;
import ch.dvbern.oss.hemed.esc.api.features.organisation.QOrganisation;
import ch.dvbern.oss.hemed.esc.api.features.organisation.QOrganisationRollen;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.QBenutzer;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;

import com.querydsl.core.NonUniqueResultException;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Repository
@RequiredArgsConstructor
@Slf4j
public class BenutzerRepo extends BaseRepo<Benutzer> {

    private final AppClock appClock;

    /**
     * Reads the Benutzer with a row lock, so that concurrent requests for the same Benutzer queue up
     * instead of racing each other into an {@code OptimisticLockException} on the version column.
     * <p>
     * The lock is taken with a query rather than {@code db.findLocked()}, see
     * {@code EinladungRepo#getByIdLockedQuery} for why that one does not lock.
     */
    @NonNull
    public Benutzer getByIdLockedQuery(@NonNull ID<Benutzer> id) {
        return db.select(QBenutzer.benutzer)
            .from(QBenutzer.benutzer)
            .where(QBenutzer.benutzer.id.eq(id.getUUID()))
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .fetchOne()
            .orElseThrow(() -> new AppFailureException(AppFailureMessage.entityNotFound(id)));
    }

    @NonNull
    public Optional<Benutzer> findByEmail(@NonNull EmailAddress email) {
        return db.selectFrom(QBenutzer.benutzer)
            .where(QBenutzer.benutzer.email.eq(email))
            .fetchOne();
    }

    // to read this from the CurrentUserService we make sure to run this in a new transaction, if it uses preexisting
    // transactions it may produce stack overflows
    @NonNull
    @Transactional(TxType.REQUIRES_NEW)
    public Optional<Benutzer> getByBenutzernameWithBerechtigungen(@NonNull EmailAddress email) {
        return getByPredicateWithBerechtigungen(QBenutzer.benutzer.email.eq(email));
    }

    @NonNull
    public Benutzer getByIdWithBerechtigungen(@NonNull ID<Benutzer> benutzerID) {
        return getByPredicateWithBerechtigungen(QBenutzer.benutzer.id.eq(benutzerID.getUUID()))
            .orElseThrow(() -> new AppFailureException(AppFailureMessage.entityNotFound(benutzerID)));
    }

    // to read this from the CurrentUserService we make sure to run this in a new transaction, if it uses preexisting
    // transactions it may produce stack overflows
    @Transactional(TxType.REQUIRES_NEW)
    @NonNull
    public Optional<ID<Benutzer>> findBenutzerIdFromBenutzername(@NonNull EmailAddress parsedEmail) {
        Optional<UUID> userIdOpt = db.select(QBenutzer.benutzer.id)
            .from(QBenutzer.benutzer)
            .where(QBenutzer.benutzer.email.eq(parsedEmail))
            .fetchOne();
        return userIdOpt.map(uuid -> ID.of(uuid, Benutzer.class));
    }

    @NonNull
    private Optional<Benutzer> getByPredicateWithBerechtigungen(@NonNull Predicate where) {
        EntityGraph<Benutzer> entityGraph = db.getEntityManager().createEntityGraph(Benutzer.class);
        SmartJPAQuery<Benutzer> benutzerSmartJPAQuery = db.selectFrom(QBenutzer.benutzer)
            .leftJoin(QBenutzer.benutzer.systemRollen, QSystemRollen.systemRollen).fetchJoin()
            .leftJoin(QBenutzer.benutzer.organisationRollen, QOrganisationRollen.organisationRollen).fetchJoin()
            .where(where);
        benutzerSmartJPAQuery = benutzerSmartJPAQuery.setHint("jakarta.persistence.loadgraph", entityGraph);

        List<Benutzer> fetchedUsers = benutzerSmartJPAQuery.fetch();

        if (fetchedUsers.size() > 1) {
            throw new NonUniqueResultException("Found more than one Benutzer");
        }

        if (fetchedUsers.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(fetchedUsers.getFirst());
    }

    @NonNull
    public List<Benutzer> findAll() {
        // Unsere Systemadmins nicht anzeigen
        List<UUID> systemAdmins = List.of(
            Benutzer.SYSTEM_ADMIN_BS_ID.getUUID(),
            Benutzer.SYSTEM_ADMIN_BE_ID.getUUID(),
            Benutzer.SYSTEM_ADMIN_WIN_ID.getUUID(),
            Benutzer.ESCADA_BENUTZER_ID.getUUID(),
            Benutzer.FILE_INGRESS_BENUTZER_ID.getUUID()
        );
        return db.selectFrom(QBenutzer.benutzer)
            .where(QBenutzer.benutzer.id.notIn(systemAdmins))
            .orderBy(QBenutzer.benutzer.vorname.asc(), QBenutzer.benutzer.nachname.asc())
            .fetch();
    }

    @NonNull
    public List<Benutzer> findAllForOrganisation(@NonNull ID<Organisation> organisationId) {
        return db.selectFrom(QBenutzer.benutzer)
            .join(QBenutzer.benutzer.organisationRollen, QOrganisationRollen.organisationRollen)
            .join(QOrganisationRollen.organisationRollen.organisation, QOrganisation.organisation)
            .where(QBenutzer.benutzer.benutzerAktivStatus.eq(EntityAktivStatus.AKTIV)
                .and(QOrganisation.organisation.id.eq(organisationId.getUUID())))
            .orderBy(QBenutzer.benutzer.vorname.asc(), QBenutzer.benutzer.nachname.asc())
            .fetch();
    }

    @NonNull
    public List<Benutzer> findAllActive() {
        return db.selectFrom(QBenutzer.benutzer)
            .where(QBenutzer.benutzer.benutzerAktivStatus.eq(EntityAktivStatus.AKTIV))
            .orderBy(QBenutzer.benutzer.vorname.asc(), QBenutzer.benutzer.nachname.asc())
            .fetch();
    }

    @NonNull
    public List<ID<Benutzer>> getBenutzerToDeactivate(
        int minutes,
        @Nullable Integer limit
    ) {
        OffsetDateTime cutoffDateTimeForOldSessions = appClock.offsetDateTime().now().minusMinutes(minutes);
        SmartJPAQuery<UUID> query =
            db.select(QBenutzer.benutzer.id)
                .from(QBenutzer.benutzer)
                .leftJoin(QBenutzer.benutzer.systemRollen, QSystemRollen.systemRollen)
                .distinct()
                .where(
                    (
                        // did not log in for a while (or never)
                        (QBenutzer.benutzer.loginInfos.lastUsedSessionTimestamp.lt(cutoffDateTimeForOldSessions)
                            .or(QBenutzer.benutzer.loginInfos.lastUsedSessionTimestamp.isNull())
                        )
                        // and was not manually activated for a while (or never)
                        .and(
                            (QBenutzer.benutzer.timestampLastManuallyActivated.lt(cutoffDateTimeForOldSessions)
                                .or(QBenutzer.benutzer.timestampLastManuallyActivated.isNull()))
                        )
                    )
                    .and(QBenutzer.benutzer.benutzerAktivStatus.eq(EntityAktivStatus.AKTIV))
                    .and(QBenutzer.benutzer.systemRollen.isNull()
                        .or(QBenutzer.benutzer.systemRollen.fachRollen.contains(FachRolle.SYSTEM_ADMIN.name()).not())
                    )
                );

        List<UUID> resultList;
        if (limit != null) {
            resultList = query.limit(limit).fetch();
        } else {
            resultList = query.fetch();
        }
        return resultList.stream().map((UUID t) -> ID.of(t, Benutzer.class)).toList();
    }

    public long deactivateListOfBenutzer(@NonNull List<ID<Benutzer>> benutzerToDeactivate) {
        List<UUID> uuidList = benutzerToDeactivate.stream().map(ID::getUUID).toList();
        return db.update(QBenutzer.benutzer)
            .set(QBenutzer.benutzer.benutzerAktivStatus, EntityAktivStatus.INAKTIV)
            .where(QBenutzer.benutzer.id.in(uuidList))
            .execute();
    }

    @NonNull
    public Benutzer getSystemAdmin(@NonNull TenantEnum tenant) {
        Benutzer benutzer = db.selectFrom(QBenutzer.benutzer)
            .where(QBenutzer.benutzer.id.eq(Benutzer.getSystemAdminId(tenant).getUUID()))
            .fetchFirst();
        if (benutzer == null) {
            throw new IllegalStateException("System admin not found for tenant: " + tenant);
        }
        return benutzer;
    }
}
