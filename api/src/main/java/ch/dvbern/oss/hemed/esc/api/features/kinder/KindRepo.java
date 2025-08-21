package ch.dvbern.oss.hemed.esc.api.features.kinder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.dvbstarter.shared.types.sozialversicherungsnummer.SozialversicherungsNummer;
import ch.dvbern.oss.hemed.esc.api.features.schule.QSchule;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.QKlasse;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class KindRepo extends BaseRepo<Kind> {

    @NonNull
    public List<Kind> findAll() {
        return db.selectFrom(QKind.kind).orderBy(QKind.kind.name.asc(), QKind.kind.vorname.asc()).fetch();
    }

    @NonNull
    public List<ID<Kind>> findAllIds() {
        List<UUID> uuidList = db.select(QKind.kind.id).from(QKind.kind).fetch();
        return uuidList.stream()
            .map(uid -> ID.of(uid, Kind.class))
            .toList();
    }

    @NonNull
    public List<Kind> findBySchule(@NonNull ID<Schule> schuleId) {
        return db.selectFrom(QKind.kind)
            .innerJoin(QKind.kind.schule, QSchule.schule) // trust schule on kind to be same as schule on klasse
            .where(QSchule.schule.id.eq(schuleId.getUUID())
                .and(QKind.kind.kindAktivStatus.ne(EntityAktivStatus.INAKTIV)))
            .orderBy(QKind.kind.name.asc(), QKind.kind.vorname.asc())
            .fetch();
    }

    @NonNull
    public List<Kind> findByKlasse(@NonNull ID<Klasse> klasseId) {
        return db.selectFrom(QKind.kind)
            .innerJoin(QKind.kind.klasse)
            .where(QKlasse.klasse.id.eq(klasseId.getUUID())
                .and(QKind.kind.kindAktivStatus.ne(EntityAktivStatus.INAKTIV)))
            .orderBy(QKind.kind.name.asc(), QKind.kind.vorname.asc())
            .fetch();
    }

    @NonNull
    public Optional<Kind> findByExternalId(@IgnoreForIdClassTest @NonNull String externalId) {
        return db.select(QKind.kind)
            .from(QKind.kind)
            .where(QKind.kind.externalImportInfo.idInExternalSystem.eq(externalId))
            .fetchOne();

    }

    @NonNull
    public Kind getByExternalId(@IgnoreForIdClassTest @NonNull String externalId) {
        return findByExternalId(externalId).orElseThrow(() -> new AppFailureException(AppFailureMessage.entityNotFoundUsingExternalId(
            Kind.class,
            externalId)));
    }

    @NonNull
    public Optional<Kind> findBySozialversicherungsnummer(@NonNull SozialversicherungsNummer vnr) {
        return db.select(QKind.kind)
            .from(QKind.kind)
            .where(QKind.kind.sozialversicherungsNummer.eq(vnr))
            .fetchOne();
    }
}
