package ch.dvbern.oss.hemed.esc.api.features.kinddeactivation;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ch.dvbern.dvbstarter.infrastructure.db.Db;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.QAnlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.QEinladung;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.QDossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.QKind;
import ch.dvbern.oss.hemed.esc.api.features.notizen.QNotiz;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import com.querydsl.jpa.JPAExpressions;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class KindDeactivationRepo {

    private final AppClock appClock;
    private final Db db;

    /**
     * Find Kinder that have not been used for a certain amount of months and are
     * - manually added and older than manualKinderDeactivationAge <br/>
     * OR <br/>
     * - are created through the external system (e.g. esacada) and are inactive there <br/>
     * Not used means: no Notiz created for the last 'monthsUnused' months and no Anlass later than 'monthsUnused'
     * months ago and last import of the external system is older than 'monthsUnused' months.
     */
    @NonNull
    public List<ID<Kind>> findKinderToDeactivate(
        int monthsUnused,
        int manualKinderDeactivationAge,
        int batchsize
    ) {
        OffsetDateTime cutoffDateTime = appClock.offsetDateTime().now().minusMonths(monthsUnused);
        LocalDate cutoffDate = appClock.offsetDateTime().now().minusMonths(monthsUnused).toLocalDate();
        LocalDate manuallyCreatedKindAgeCutoff =
            appClock.offsetDateTime().now().minusYears(manualKinderDeactivationAge).toLocalDate();

        List<UUID> kinderWithoutDossier = db.select(QKind.kind.id)
            .from(QKind.kind)
            .leftJoin(QDossier.dossier).on(QDossier.dossier.kind.eq(QKind.kind))
            .where(
                (
                    (
                        QKind.kind.externalImportInfo.idInExternalSystem.isNotNull()
                            .and(QKind.kind.externalImportInfo.aktivInExternalSystem.eq(false))
                            .and(QKind.kind.externalImportInfo.timestampLatestImport.lt(cutoffDateTime))
                    )
                        .or(
                            QKind.kind.externalImportInfo.idInExternalSystem.isNull()
                                .and(QKind.kind.geburtsdatum.lt(manuallyCreatedKindAgeCutoff))
                        )
                )
                    .and(
                        (JPAExpressions.selectOne()
                            .from(QNotiz.notiz)
                            .where(
                                QNotiz.notiz.dossier.id.eq(QDossier.dossier.id)
                                    .and(QNotiz.notiz.timestampErstellt.goe(cutoffDateTime))
                            ).notExists()
                        )
                    ).and(
                        (JPAExpressions.selectOne()
                            .from(QEinladung.einladung)
                            .innerJoin(QEinladung.einladung.anlass, QAnlass.anlass)
                            .where(QEinladung.einladung.dossier.id.eq(QDossier.dossier.id)
                                .and(QAnlass.anlass.datum.goe(cutoffDate)))
                            .notExists())
                    ).and(
                        QKind.kind.kindAktivStatus.ne(EntityAktivStatus.INAKTIV)
                    )
            )
            .limit(batchsize)
            .fetch();

        return new ArrayList<>(kinderWithoutDossier
            .stream()
            .map(uid -> ID.of(uid, Kind.class)).toList()
        );
    }
}
