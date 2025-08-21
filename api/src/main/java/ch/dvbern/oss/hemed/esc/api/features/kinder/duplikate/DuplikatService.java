package ch.dvbern.oss.hemed.esc.api.features.kinder.duplikate;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindService;
import ch.dvbern.oss.hemed.esc.api.shared.person.DuplikatMergeService;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class DuplikatService {

    private final DuplikatRepo duplikatRepo;
    private final KindService kindService;
    private final DuplikatMergeService duplikatMergeService;


    @NonNull
    public Duplikat persist(@NonNull Duplikat duplikat) {
        duplikatRepo.persist(duplikat);
        return duplikat;
    }

    public void remove(@NonNull ID<Duplikat> duplikatID) {
        duplikatRepo.remove(duplikatID);
    }

    public void removeForKind(@NonNull ID<Kind> kindID) {
        List<Duplikat> duplikate = findByKindId(kindID);
        for (Duplikat duplikat : duplikate) {
            // Wenn beide Kinder null sind, dann löschen wir den Duplikat-Eintrag
            if (duplikat.getKind1() == null && duplikat.getKind2() == null) {
                remove(duplikat.getId());
            }
            // Ansonsten wird das zu loeschende Kind aus dem Duplikat entfernt (auf null gesetzt)
            if (duplikat.getKind1() != null && duplikat.getKind1().getId().equals(kindID) ) {
                duplikat.setKind1(null);
            }
            if (duplikat.getKind2() != null && duplikat.getKind2().getId().equals(kindID) ) {
                duplikat.setKind2(null);
            }
        }
    }

    @NonNull
    public Duplikat getById(@NonNull ID<Duplikat> duplikatID) {
        return duplikatRepo.getById(duplikatID);
    }

    @NonNull
    public List<Duplikat> findAll() {
        return duplikatRepo.findAll();
    }

    @NonNull
    public List<Duplikat> findUnmergedDuplikate() {
        return duplikatRepo.findUnmergedDuplikate();
    }

    @Transactional(TxType.REQUIRES_NEW)
    public int reevaluateExistingDuplikat(@NonNull Duplikat duplikat) {
        Objects.requireNonNull(duplikat.getKind1()); // Schon ausserhalb geprüft
        List<Kind> list = searchPotentialKindDuplikate(duplikat.getKind1());
        if (!list.contains(duplikat.getKind2())) {
            LOG.info("ESC-RELOAD_DUPLIKATE: Duplikat {} is no longer present and will be removed", duplikat.getId());
            remove(duplikat.getId());
            return 1;
        }
        return 0;
    }

    @Transactional(TxType.REQUIRES_NEW)
    public int reevaluateKind(@NonNull ID<Kind> kindID) {
        boolean newDuplikatCreated = reloadDuplikate(kindID);
        if (newDuplikatCreated) {
            return 1;
        }
        return 0;
    }

    private boolean reloadDuplikate(@NonNull ID<Kind> id) {
        Kind kind = kindService.getById(id);
        List<Kind> duplikate = searchPotentialKindDuplikate(kind);
        if (!duplikate.isEmpty()) {
            LOG.info("ESC-RELOAD_DUPLIKATE: Found {} Duplikat(e) for Kind {}", duplikate.size(), kind.getId());
            for (Kind duplikat : duplikate) {
                LOG.info(
                    "ESC-RELOAD_DUPLIKATE: Duplikat gefunden: {} {} ({})",
                    duplikat.getVorname(),
                    duplikat.getName(),
                    duplikat.getId());
                // Sicherstellen, dass diese Konstellation noch nicht in den Duplikaten existiert
                Optional<Duplikat> existingDuplikat = findByKindIds(kind.getId(), duplikat.getId());
                if (existingDuplikat.isEmpty()) {
                    LOG.info(
                        "ESC-RELOAD_DUPLIKATE: Creating new Duplikat for Kind1={} and Kind2={} ",
                        kind.getId(),
                        duplikat.getId());
                    Duplikat newDuplikat = Duplikat.forKinder(kind, duplikat);
                    persist(newDuplikat);
                    return true;
                }
                LOG.info(
                    "ESC-RELOAD_DUPLIKATE: Duplikat for Kind {} and {} existiert bereits",
                    kind.getId(),
                    duplikat.getId());
            }
        }
        return false;
    }

    @NonNull
    public Optional<Duplikat> findByKindIds(@NonNull ID<Kind> idA, @NonNull ID<Kind> idB) {
        return duplikatRepo.findByKindIds(idA, idB);
    }

    @NonNull
    public List<Duplikat> findByKindId(@NonNull ID<Kind> id) {
        return duplikatRepo.findByKindId(id);
    }

    @NonNull
    public List<Kind> searchPotentialKindDuplikate(@NonNull Kind kind) {
        var quickSearchTerm = new DuplikatSearchTerm(
            kind.getId(),
            kind.getName(),
            kind.getVorname(),
            kind.getGeburtsdatum(),
            kind.getGeschlecht()
        );
        return duplikatRepo.searchDuplikate(quickSearchTerm);
    }

    @NonNull
    public Duplikat ensureDuplikatAndMarkAsFalsePositive(@NonNull ID<Duplikat> duplikatId) {
        Duplikat duplikat = duplikatRepo.getById(duplikatId);
        duplikat.setDuplikatStatus(DuplikatStatus.FALSE_POSITIVE);
        return persist(duplikat);
    }

    public void ensureDuplikatAndMarkAsFalsePositive(@NonNull Kind kindA, Kind kindB) {
        Optional<Duplikat> optionalDuplikat = this.findByKindIds(kindA.getId(), kindB.getId());

        if (optionalDuplikat.isEmpty()) {
            Duplikat newDuplikat = Duplikat.forKinder(kindA, kindB);
            newDuplikat.setDuplikatStatus(DuplikatStatus.FALSE_POSITIVE);
            persist(newDuplikat);
        } else {
            Duplikat foundDuplikat = optionalDuplikat.get();
            if (foundDuplikat.getDuplikatStatus() == DuplikatStatus.OFFEN) {
                foundDuplikat.setDuplikatStatus(DuplikatStatus.FALSE_POSITIVE);
                persist(foundDuplikat);
            }
        }
    }

    @NonNull
    public Duplikat merge(@NonNull ID<Duplikat> duplikatId, @NonNull ID<Kind> basisKindId) {
        return duplikatMergeService.merge(duplikatId, basisKindId);
    }
}
