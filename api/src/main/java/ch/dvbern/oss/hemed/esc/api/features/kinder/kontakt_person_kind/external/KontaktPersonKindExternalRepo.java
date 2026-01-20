package ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.external;

import java.util.List;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.QKontaktPersonKind;
import ch.dvbern.oss.hemed.esc.api.shared.externalsystem.ExternalSystemIdDTO;
import ch.dvbern.oss.hemed.esc.api.shared.externalsystem.QExternalSystemIdDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@Slf4j
@Repository
@RequiredArgsConstructor
public class KontaktPersonKindExternalRepo extends BaseRepo<KontaktPersonKindExternal> {

    @NonNull
    public List<ExternalSystemIdDTO> findAllExternalIds() {
        return this.db.select(
                new QExternalSystemIdDTO(
                    QKontaktPersonKindExternal.kontaktPersonKindExternal.adressierungId,
                    QKontaktPersonKind.kontaktPersonKind.id)
            )
            .from(QKontaktPersonKindExternal.kontaktPersonKindExternal)
            .innerJoin(QKontaktPersonKind.kontaktPersonKind)
            .on(QKontaktPersonKindExternal.kontaktPersonKindExternal.kontaktPersonKind
                .eq(QKontaktPersonKind.kontaktPersonKind))
            .fetch();

    }

    @NonNull
    public Optional<KontaktPersonKindExternal> findExternalForKontaktPerson(
        @NonNull ID<KontaktPersonKind> id
    ) {
        return this.db.select(QKontaktPersonKindExternal.kontaktPersonKindExternal)
            .from(QKontaktPersonKindExternal.kontaktPersonKindExternal)
            .where(QKontaktPersonKindExternal.kontaktPersonKindExternal.kontaktPersonKind.id.eq(id.getUUID()))
            .fetchOne();
    }

    @NonNull
    public KontaktPersonKindExternal getExternalForKontaktPerson(@NonNull ID<KontaktPersonKind> id) {
        return this.findExternalForKontaktPerson(id)
            .orElseThrow(() -> new AppFailureException(AppFailureMessage.entityNotFound(id)));
    }
}
