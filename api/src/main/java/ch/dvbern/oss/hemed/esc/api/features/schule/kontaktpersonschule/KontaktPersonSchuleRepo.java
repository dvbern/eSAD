package ch.dvbern.oss.hemed.esc.api.features.schule.kontaktpersonschule;

import java.util.List;
import java.util.UUID;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.schule.QSchule;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Repository
@RequiredArgsConstructor
public class KontaktPersonSchuleRepo extends BaseRepo<KontaktPersonSchule> {

    @NonNull
    public List<KontaktPersonSchule> findAktivBySchule(@NonNull ID<Schule> schuleId) {
        return db
            .selectFrom(QKontaktPersonSchule.kontaktPersonSchule)
            .innerJoin(QKontaktPersonSchule.kontaktPersonSchule.schulen, QSchule.schule)
            .where(QSchule.schule.id.eq(schuleId.getUUID())
                .and(QKontaktPersonSchule.kontaktPersonSchule.aktiv.eq(true)))
            .fetch();
    }

    @NonNull
    public List<KontaktPersonSchule> findAllOptionsActive(@Nullable ID<Schule> schuleToExclude) {
        List<UUID> currentContacts = List.of();
        if (schuleToExclude != null) {
            currentContacts = db
                .select(QKontaktPersonSchule.kontaktPersonSchule.id)
                .from(QKontaktPersonSchule.kontaktPersonSchule)
                .innerJoin(QKontaktPersonSchule.kontaktPersonSchule.schulen, QSchule.schule)
                .where(QSchule.schule.id.eq(schuleToExclude.getUUID()))
                .fetch();
        }
        return db
            .selectFrom(QKontaktPersonSchule.kontaktPersonSchule)
            .leftJoin(QKontaktPersonSchule.kontaktPersonSchule.schulen, QSchule.schule)
            .where(QKontaktPersonSchule.kontaktPersonSchule.aktiv.eq(true)
                .and(QKontaktPersonSchule.kontaktPersonSchule.id.notIn(currentContacts))
            )
            .fetch();
    }
}
