package ch.dvbern.oss.hemed.esc.api.shared.aufgabe;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.QKind;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.features.labels.QLabel;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.types.AufgabeStatus;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class AufgabeRepo extends BaseRepo<Aufgabe> {

    private final AppClock appClock;

    @NonNull
    public List<Aufgabe> findAll() {
        return db.selectFrom(QAufgabe.aufgabe)
            .orderBy(QAufgabe.aufgabe.faelligBis.asc())
            .fetch();
    }

    @NonNull
    public List<Aufgabe> findAllForKind(@NonNull ID<Kind> kindId) {
        return db.select(QAufgabe.aufgabe)
            .from(QAufgabe.aufgabe)
            .leftJoin(QAufgabe.aufgabe.kind, QKind.kind)
            .where(QKind.kind.id.eq(kindId.getUUID()))
            .orderBy(QAufgabe.aufgabe.faelligBis.asc())
            .fetch();
    }

    @NonNull
    public List<Aufgabe> findAufgabenForAnlassAndLabelNoOptimisticLockingCheck(@NonNull Anlass anlass, @NonNull ID<Label> labelId) {
        List<Aufgabe> aufgaben = db.selectFrom(QAufgabe.aufgabe)
            .leftJoin(QAufgabe.aufgabe.labels, QLabel.label)
            .where(QAufgabe.aufgabe.anlass.id.eq(anlass.getId().getUUID())
                .and(QLabel.label.id.eq(labelId.getUUID())))
            .fetch();
        aufgaben.forEach(AbstractEntity::disableOptimisticLockingCheck);
        return aufgaben;
    }

    @NonNull
    public List<Aufgabe> findAufgabenForKindWithLabelNoOptimisticLockingCheck(@NonNull Kind kind, @NonNull ID<Label> labelId) {
        List<Aufgabe> aufgaben = db.selectFrom(QAufgabe.aufgabe)
            .leftJoin(QAufgabe.aufgabe.labels, QLabel.label)
            .where(QAufgabe.aufgabe.kind.id.eq(kind.getId().getUUID())
                .and(QLabel.label.id.eq(labelId.getUUID())))
            .fetch();
        aufgaben.forEach(AbstractEntity::disableOptimisticLockingCheck);
        return aufgaben;
    }

    @NonNull
    public Aufgabe addUploadedFile(@NonNull ID<Aufgabe> aufgabeID, FileData filedata) {
        Aufgabe lockedAufgabe = db.findLocked(aufgabeID, LockModeType.PESSIMISTIC_WRITE)
            .orElseThrow(() -> new AppFailureException(AppFailureMessage.entityNotFound(aufgabeID)));
        lockedAufgabe.getFiles().add(filedata);
        this.persist(lockedAufgabe);
        return lockedAufgabe;
    }

    public int getNumOfOffeneFaelligeAufgaben() {
        return getOffeneFaelligeAufgaben().size();
    }

    private List<Aufgabe> getOffeneFaelligeAufgaben() {
        return db.select(QAufgabe.aufgabe)
            .from(QAufgabe.aufgabe)
            .where(QAufgabe.aufgabe.aufgabeStatus.ne(AufgabeStatus.ABGESCHLOSSEN)
                .and(QAufgabe.aufgabe.faelligBis.loe(appClock.offsetDateTime().now().toLocalDate())))
            .orderBy(QAufgabe.aufgabe.faelligBis.asc())
            .fetch();
    }

    public List<Aufgabe> getOffeneFaelligeAufgabenByEmpfaenger(@NonNull ID<Benutzer> empfaengerID) {
        return db.select(QAufgabe.aufgabe)
            .from(QAufgabe.aufgabe)
            .where(QAufgabe.aufgabe.aufgabeStatus.ne(AufgabeStatus.ABGESCHLOSSEN)
                .and(QAufgabe.aufgabe.faelligBis.loe(appClock.offsetDateTime().now().toLocalDate()))
                .and(QAufgabe.aufgabe.empfaenger.id.eq(empfaengerID.getUUID())))
            .orderBy(QAufgabe.aufgabe.faelligBis.asc())
            .fetch();
    }

    @NonNull
    public List<Aufgabe> findAllNichtAbgeschlosseneAufgabenForCurrent(@NonNull ID<Benutzer> empfaengerID) {
        return db.select(QAufgabe.aufgabe)
            .from(QAufgabe.aufgabe)
            .where(QAufgabe.aufgabe.empfaenger.id.eq(empfaengerID.getUUID())
                .and(QAufgabe.aufgabe.aufgabeStatus.ne(AufgabeStatus.ABGESCHLOSSEN))
            )
            .orderBy(QAufgabe.aufgabe.faelligBis.asc())
            .fetch();
    }
}
