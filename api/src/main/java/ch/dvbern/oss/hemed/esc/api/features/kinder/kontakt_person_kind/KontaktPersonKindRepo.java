package ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.QKind;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Repository
@RequiredArgsConstructor
public class KontaktPersonKindRepo extends BaseRepo<KontaktPersonKind> {

    @NonNull
    public List<KontaktPersonKind> findAll() {
        return db.selectFrom(QKontaktPersonKind.kontaktPersonKind)
            .fetch();
    }

    @NonNull
    public List<KontaktPersonKind> findAktivByKind(@NonNull ID<Kind> kindID) {
        return db
            .selectFrom(QKontaktPersonKind.kontaktPersonKind)
            .innerJoin(QKontaktPersonKind.kontaktPersonKind.kinder, QKind.kind)
            .where(QKind.kind.id.eq(kindID.getUUID())
                .and(QKontaktPersonKind.kontaktPersonKind.externalImportInfo.aktivInExternalSystem.eq(true)
                    .or(QKontaktPersonKind.kontaktPersonKind.externalImportInfo.isNull()))) // check null because
            // manually created kontaktpersonenkind don't have externalImportInfo
            .fetch();
    }

    @NonNull
    public List<KontaktPersonKind> findByKind(@NonNull ID<Kind> kindID) {
        return db
            .selectFrom(QKontaktPersonKind.kontaktPersonKind)
            .innerJoin(QKontaktPersonKind.kontaktPersonKind.kinder, QKind.kind)
            .where(QKind.kind.id.eq(kindID.getUUID()))
            .fetch();
    }

    @NonNull
    public List<KontaktPersonKind> findAllOptionsActive(@Nullable ID<Kind> kindToExclude) {
        List<UUID> currentContacts = List.of();
        if (kindToExclude != null) {
            currentContacts = db.select(QKontaktPersonKind.kontaktPersonKind.id)
                .from(QKontaktPersonKind.kontaktPersonKind)
                .innerJoin(QKontaktPersonKind.kontaktPersonKind.kinder, QKind.kind)
                .where(QKind.kind.id.eq(kindToExclude.getUUID()))
                .fetch();
        }
        return db
            .selectFrom(QKontaktPersonKind.kontaktPersonKind)
            .leftJoin(QKontaktPersonKind.kontaktPersonKind.kinder, QKind.kind)
            .where((QKontaktPersonKind.kontaktPersonKind.externalImportInfo.isNull()
                .or(QKontaktPersonKind.kontaktPersonKind.externalImportInfo.aktivInExternalSystem.isFalse()))
                .and(QKontaktPersonKind.kontaktPersonKind.id.notIn(currentContacts)))
            .fetch();
    }

    @NonNull
    public KontaktPersonKind getByExternalId(@IgnoreForIdClassTest @NonNull String externalId) {
        return this.findByExternalId(externalId)
            .orElseThrow(() -> new AppFailureException(AppFailureMessage.entityNotFoundUsingExternalId(
                KontaktPersonKind.class,
                externalId)));
    }

    @NonNull
    public Optional<KontaktPersonKind> findByExternalId(@IgnoreForIdClassTest @NonNull String externalId) {
        return db.select(QKontaktPersonKind.kontaktPersonKind)
            .from(QKontaktPersonKind.kontaktPersonKind)
            .where(QKontaktPersonKind.kontaktPersonKind.externalImportInfo.idInExternalSystem.eq(externalId))
            .fetchOne();
    }
}
