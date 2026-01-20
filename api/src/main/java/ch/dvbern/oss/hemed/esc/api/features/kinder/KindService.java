package ch.dvbern.oss.hemed.esc.api.features.kinder;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.KindDuplikatException;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.dvbstarter.shared.types.sozialversicherungsnummer.SozialversicherungsNummer;
import ch.dvbern.oss.hemed.esc.api.features.kinder.duplikate.Duplikat;
import ch.dvbern.oss.hemed.esc.api.features.kinder.duplikate.DuplikatService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.types.KindSaveModeDuplikat;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.shared.person.RemoveKindService;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import ch.dvbern.oss.hemed.esc.api.shared.util.StringNormalizerUtil;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Slf4j
@Service
@RequiredArgsConstructor
public class KindService {

    private final KindRepo kindRepo;
    private final DuplikatService duplikatService;
    private final RemoveKindService removeKindService;

    @NonNull
    public Kind persistAndFlushWithoutDuplicateCheck(@NonNull Kind kind) {
        kindRepo.persistAndFlush(kind);
        return kind;
    }

    @NonNull
    public Kind persistWithoutDuplicateCheck(@NonNull Kind kind) {
        kindRepo.persist(kind);
        return kind;
    }

    @NonNull
    public Kind persistWithoutDuplicateCheck(@NonNull Kind kind, @Nullable Long versionFromFrontend) {
        if (versionFromFrontend == null) {
            persistAndFlushWithoutDuplicateCheck(kind);
            return kind;
        }
        return kindRepo.persistWithOptimisticLocking(kind, versionFromFrontend);
    }

    @Transactional(TxType.REQUIRES_NEW)
    public void ensureNoManualKindWithSameVnrExisting(
        @NonNull SozialversicherungsNummer vnr,
        @Nullable @IgnoreForIdClassTest String externalSchuelerId
    ) {
        Optional<Kind> preexistingKindWithVnrOpt = findBySozialversicherungsnummer(vnr);
        if (preexistingKindWithVnrOpt.isPresent()) {
            Kind kindWithSameVnr = preexistingKindWithVnrOpt.get();
            if (kindWithSameVnr.getExternalSystemIdOrNull() != null) { // escada imported kind
                if (!kindWithSameVnr.getExternalSystemIdOrNull().equals(externalSchuelerId)
                ) {
                    // Es existiert schon ein Escada-Kind mit der gleichen Sozialversicherungsnummer aber anderer
                    // escadaid.
                    // Wir können nichts machen und stürzen dann später automatisch ab
                    LOG.warn(
                        "ESCADA: Kind with Sozialversicherungsnummer '{}' already exists from escada, not adding new "
                            + "Kind",
                        vnr
                    );
                }
            } else {
                // Das bestehende Kind wurde manuell angelegt. Wir entfernen bei diesem die Sozialversicherungsnummer,
                // damit wir das neue Kind mit der Sozialversicherungsnummer anlegen können. Falls die beiden Kinder
                // identisch sind, werden sie im Duplikat-Batchjob aufschlagen, dies ist etwas konservativer als
                // direkt zu mergen da der Kunde noch selber entscheiden kann ob er mergen will
                kindWithSameVnr.setSozialversicherungsNummer(null);
                kindWithSameVnr.disableOptimisticLockingCheck();
                persistWithoutDuplicateCheck(kindWithSameVnr);
                LOG.info(
                    "ESCADA: Kind with Sozialversicherungsnummer '{}' already exists (manually created), removing "
                        + "Sozialversicherungsnummer from existing Kind {}",
                    vnr,
                    kindWithSameVnr.getId()
                );
            }
        }
    }

    public Kind persistWithDuplicateCheck(
        @NonNull Kind kind,
        @Nullable Long versionFromFrontend,
        @NonNull KindSaveModeDuplikat kindSaveMode) throws KindDuplikatException {
        return persistInternal(
            kind,
            kindSaveMode == KindSaveModeDuplikat.MARK_AS_FALSE_POSITIVE,
            versionFromFrontend != null,
            versionFromFrontend
        );
    }

    @NonNull
    private Kind persistInternal(
        @NonNull Kind kind,
        boolean forceSaveAkzeptiereDuplikate,
        boolean withOptimisticLocking,
        @Nullable Long versionFromFrontend) {
        kind.setNormalizedName(StringNormalizerUtil.normalizeAndStripDiarcritics(kind.getName()));
        kind.setNormalizedVorname(StringNormalizerUtil.normalizeAndStripDiarcritics(kind.getVorname()));
        // check unique key constraints first (note that this will be rolled back if kind is new duplicate)
        if (withOptimisticLocking) {
            kindRepo.persistWithOptimisticLocking(kind, Objects.requireNonNull(versionFromFrontend));
        } else {
            kindRepo.persist(kind);
        }
        List<Kind> potentialKindDuplikate = duplikatService.searchPotentialKindDuplikate(kind);

        for (Kind potentialKindDuplikat : potentialKindDuplikate) {
            if (forceSaveAkzeptiereDuplikate) {
                // user checked potential Kind matches and decided that they are false positivs, mark them as such
                duplikatService.ensureDuplikatAndMarkAsFalsePositive(kind, potentialKindDuplikat);
            } else {
                // on save we check if the potentialKind matches are already known as Duplikat. If not we have to
                // inform the user about the new Duplikat match
                Optional<Duplikat> optionalDuplikat =
                    duplikatService.findByKindIds(kind.getId(), potentialKindDuplikat.getId());
                if (optionalDuplikat.isEmpty()) {
                    // Sobald wir ein passendes Kind finden, das noch nicht als Duplikat gespeichert ist,
                    // werfen wir eine KindDuplikatException, um den Prozess zu stoppen
                    throw new KindDuplikatException(potentialKindDuplikate.stream()
                        .map(Kind::getId)
                        .toList());
                }
            }
        }
        return kind;
    }

    public void remove(@NonNull ID<Kind> kindID) {
        removeKindService.remove(kindID); // will remove related entities
    }

    @NonNull
    public List<Kind> findAll() {
        return kindRepo.findAll();
    }

    @NonNull
    public List<ID<Kind>> findAllIds() {
        return kindRepo.findAllIds();
    }

    @NonNull
    public Kind getById(@NonNull ID<Kind> kindId) {
        return kindRepo.getById(kindId);
    }

    @NonNull
    public Optional<Kind> findById(@NonNull ID<Kind> kindId) {
        return kindRepo.findById(kindId);
    }

    @NonNull
    public List<Kind> findBySchule(@NonNull ID<Schule> schuleId) {
        return kindRepo.findBySchule(schuleId);
    }

    @NonNull
    public List<Kind> findByKlasse(@NonNull ID<Klasse> klasseId) {
        return kindRepo.findByKlasse(klasseId);
    }

    @NonNull
    public Optional<Kind> findByExternalId(@IgnoreForIdClassTest @NonNull String externalId) {
        return kindRepo.findByExternalId(externalId);
    }

    @NonNull
    public Kind getByExternalId(@IgnoreForIdClassTest @NonNull String externalId) {
        return kindRepo.getByExternalId(externalId);
    }

    @NonNull
    public Optional<Kind> findBySozialversicherungsnummer(@NonNull SozialversicherungsNummer vnr) {
        return kindRepo.findBySozialversicherungsnummer(vnr);
    }

    @Transactional(TxType.REQUIRES_NEW)
    public void normalizeKindName(ID<Kind> id) {
        Kind kind = getById(id);
        kind.setNormalizedName(StringNormalizerUtil.normalizeAndStripDiarcritics(kind.getName()));
        kind.setNormalizedVorname(StringNormalizerUtil.normalizeAndStripDiarcritics(kind.getVorname()));
        kind.disableOptimisticLockingCheck();
        persistWithoutDuplicateCheck(kind);
    }
}
