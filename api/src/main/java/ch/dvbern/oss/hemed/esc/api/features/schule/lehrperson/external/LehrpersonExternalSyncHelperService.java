package ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.external;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ExternalImportInfoJson;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ImportTyp;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.KlasseService;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.Lehrperson;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.LehrpersonService;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.externalsystem.ExternalSystemIdDTO;
import ch.dvbern.oss.hemed.esc.api.shared.externalsystem.ItemSyncResultJson;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Service
@Slf4j
@RequiredArgsConstructor
public class LehrpersonExternalSyncHelperService {

    private final LehrpersonExternalRepo lehrpersonExternalRepo;
    private final LehrpersonService lehrpersonService;
    private final KlasseService klasseService;
    private final AppClock appClock;
    private final ConfigService configService;

    @NonNull
    public static Lehrperson toEntityForCreate(
        @NonNull Set<Klasse> klassen,
        @NonNull LehrpersonExternal lehrpersonExternal,
        @NonNull OffsetDateTime now
    ) {
        return new Lehrperson(
            ExternalImportInfoJson.toEntityOrCreate(null, lehrpersonExternal.getExternalId(), now, ImportTyp.ESCADA),
            klassen,
            mapGeschlechtFromEscadaAnrede(lehrpersonExternal.getKlasseLehrpersonAnrede()),
            Objects.toString(lehrpersonExternal.getKlasseLehrpersonName(), "N/A"),
            Objects.toString(lehrpersonExternal.getKlasseLehrpersonVorname(), "N/A"),
            null,
            null
        );
    }

    @NonNull
    public static LehrpersonExternal toEntityLehrpersonExternal(
        @NonNull LehrpersonExternal existingLehrpersonExternal,
        @NonNull LehrpersonExternal remoteLehrpersonExternal
    ) {
        existingLehrpersonExternal.setKlasseLehrpersonAnrede(remoteLehrpersonExternal.getKlasseLehrpersonAnrede());
        existingLehrpersonExternal.setKlasseLehrpersonName(remoteLehrpersonExternal.getKlasseLehrpersonName());
        existingLehrpersonExternal.setKlasseLehrpersonVorname(remoteLehrpersonExternal.getKlasseLehrpersonVorname());
        existingLehrpersonExternal.setEscadaKlasseIds(remoteLehrpersonExternal.getEscadaKlasseIds());
        return existingLehrpersonExternal;
    }

    @NonNull
    public static Lehrperson updateLehrpersonBasedOnExternal(
        @NonNull Lehrperson lehrperson,
        @NonNull LehrpersonExternal updatedExternal,
        @NonNull OffsetDateTime now
    ) {
        lehrperson.setGeschlecht(mapGeschlechtFromEscadaAnrede(updatedExternal.getKlasseLehrpersonAnrede()));
        lehrperson.setName(Objects.toString(updatedExternal.getKlasseLehrpersonName(), "N/A"));
        lehrperson.setVorname(Objects.toString(updatedExternal.getKlasseLehrpersonVorname(), "N/A"));
        lehrperson.setExternalImportInfo(ExternalImportInfoJson.toEntityOrCreate(
            lehrperson.getExternalImportInfo(),
            updatedExternal.getExternalId(),
            now,
            ImportTyp.ESCADA
        ));
        return lehrperson;
    }

    @NonNull
    @Transactional(TxType.REQUIRES_NEW)
    public ItemSyncResultJson<Lehrperson> addNewLehrperson(
        @NonNull LehrpersonExternal lehrpersonExternalToAdd
    ) {
        // Alle Lehrer muessen mindestens einer Klasse zugeordnet sein
        Set<Klasse> klassen = loadLinkedKlassen(lehrpersonExternalToAdd);
        Optional<Lehrperson> preexistingLehrpersonOpt = findPreexistingLehrperson(lehrpersonExternalToAdd);

        // Die Lehrperson erstellen, speichern und den Klassen zuordnen
        Lehrperson internalLehrpersonToStore = preexistingLehrpersonOpt
            .map(existingLehrperson -> updateLehrpersonBasedOnExternal(
                existingLehrperson,
                lehrpersonExternalToAdd,
                appClock.offsetDateTime().now()
            ))
            .orElseGet(() -> toEntityForCreate(
                klassen,
                lehrpersonExternalToAdd,
                appClock.offsetDateTime().now()
            ));
        internalLehrpersonToStore = lehrpersonService.persist(internalLehrpersonToStore);

        handleKlasseLehrerRelationChanges(internalLehrpersonToStore, klassen, internalLehrpersonToStore.getKlassen());

        // Die Externe Lehrperson ebenfalls speichern
        lehrpersonExternalToAdd.setLehrperson(internalLehrpersonToStore);
        lehrpersonExternalRepo.persist(lehrpersonExternalToAdd);

        var result = ItemSyncResultJson.createAddOkItem(
            lehrpersonExternalToAdd,
            internalLehrpersonToStore.getId(),
            internalLehrpersonToStore.getFullNameIfAllowed(configService.isEscadaRedactPersonalInfo())
        );

        LOG.info(
            "ESCADA: Lehrperson {} added{}, internalId: '{}', externalId: '{}'",
            result.getFriendlyName(),
            preexistingLehrpersonOpt.isEmpty() ? "" : " (preexisting)",
            result.getInternalId(),
            result.getExternalId()
        );
        return result;
    }

    private Optional<Lehrperson> findPreexistingLehrperson(
        @NonNull LehrpersonExternal lehrpersonExternalToAdd
    ) {
        return lehrpersonService.findByExternalId(lehrpersonExternalToAdd.getExternalId());
    }

    @NonNull
    @Transactional(TxType.REQUIRES_NEW)
    public ItemSyncResultJson<Lehrperson> updateExistingLehrperson(
        @NonNull ExternalSystemIdDTO idDTO,
        @NonNull LehrpersonExternal remoteLehrpersonExternal
    ) {
        Lehrperson existingLehrperson =
            lehrpersonService.getById(ID.of(idDTO.getInternalId(), Lehrperson.class));
        LehrpersonExternal externalToUpdate =
            lehrpersonExternalRepo.getExternalForLehrperson(existingLehrperson.getId());

        Validate.isTrue(
            Objects.requireNonNull(externalToUpdate).getExternalId().equals(idDTO.getExternalId()),
            "ExternalId mismatch, expected: '%s', actual: '%s' for existingLehrperson",
            idDTO.getExternalId(),
            externalToUpdate.getExternalId(),
            existingLehrperson.getId());

        LehrpersonExternal updatedExternal = toEntityLehrpersonExternal(
            externalToUpdate,
            remoteLehrpersonExternal
        );
        lehrpersonExternalRepo.persist(updatedExternal);

        Lehrperson updatedLehrperson = updateLehrpersonBasedOnExternal(
            existingLehrperson,
            updatedExternal,
            appClock.offsetDateTime().now()
        );
        updatedLehrperson.disableOptimisticLockingCheck();

        // update klassen relations
        Set<Klasse> klassen = loadLinkedKlassen(updatedExternal);
        handleKlasseLehrerRelationChanges(updatedLehrperson, klassen, existingLehrperson.getKlassen());
        updatedLehrperson.getKlassen().clear();
        updatedLehrperson.getKlassen().addAll(klassen);
        updatedLehrperson = lehrpersonService.persist(updatedLehrperson);

        return ItemSyncResultJson.createUpdateOkItem(
            idDTO,
            Lehrperson.class,
            updatedLehrperson.getFullNameIfAllowed(configService.isEscadaRedactPersonalInfo())
        );
    }

    private void handleKlasseLehrerRelationChanges(
        @NonNull Lehrperson updatedLehrperson,
        @NonNull Set<Klasse> klassenLehrerBelongsToExternally,
        @NonNull Set<Klasse> klassenLehrpersCurrentlyBelongsToInDb
    ) {

        // remove still existing klassen from all currently linked klassen to get those from which we must remove the
        // lehrer
        List<Klasse> klassenToRemoveLehrerFrom = new ArrayList<>(klassenLehrpersCurrentlyBelongsToInDb);
        klassenToRemoveLehrerFrom.removeAll(klassenLehrerBelongsToExternally);
        // add lehrperson to klassen since klassen is owing side
        for (Klasse klasse : klassenLehrerBelongsToExternally) {
            klasse.disableOptimisticLockingCheck();
            klasseService.addLehrpersonToKlasse(klasse, updatedLehrperson);
        }
        // remove lehrer from klassen that he no longer belongs to
        for (Klasse klasse : klassenToRemoveLehrerFrom) {
            klasse.disableOptimisticLockingCheck();
            klasseService.removeLehrpersonFromKlasse(klasse, updatedLehrperson);
        }
    }

    @NonNull
    private Set<Klasse> loadLinkedKlassen(@NonNull LehrpersonExternal lehrpersonExternalToAdd) {
        String[] escadaKlasseIds = StringUtils.split(lehrpersonExternalToAdd.getEscadaKlasseIds(), ",");
        Validate.isTrue(
            escadaKlasseIds != null && escadaKlasseIds.length > 0,
            "escada Klasse ID is required to assign Lehrer to Klasse");

        // read Klassen from database
        Set<Klasse> klassen = new HashSet<>();
        for (String escadaKlasseId : escadaKlasseIds) {
            klassen.add(klasseService.getByExternalIdLockedQuery(escadaKlasseId));
        }
        return klassen;
    }

    @NonNull
    @Transactional(TxType.REQUIRES_NEW)
    public ItemSyncResultJson<Lehrperson> deactivateRemovedLehrperson(
        @NonNull ExternalSystemIdDTO externalSystemIdDTO
    ) {
        Lehrperson lehrperson = lehrpersonService.getById(
            ID.of(externalSystemIdDTO.getInternalId(), Lehrperson.class));
        LehrpersonExternal externalToDelete =
            lehrpersonExternalRepo.getExternalForLehrperson(lehrperson.getId());

        Validate.isTrue(
            Objects.requireNonNull(externalToDelete).getExternalId()
                .equals(externalSystemIdDTO.getExternalId()),
            "ExternalId mismatch, expected: '%s', actual: '%s' for KontaktPerson-Schule",
            externalSystemIdDTO.getExternalId(),
            externalToDelete.getExternalId(),
            lehrperson.getId());

        if (lehrperson.getExternalImportInfo() != null && lehrperson.isAktivInExternalSystem()) {
            lehrperson.getExternalImportInfo().setAktivInExternalSystem(false);
            lehrperson.disableOptimisticLockingCheck();
        }

        lehrpersonExternalRepo.remove(externalToDelete.getId());

        var result = ItemSyncResultJson.createDeactivateOkItem(
            externalSystemIdDTO,
            Lehrperson.class,
            lehrperson.getFullNameIfAllowed(configService.isEscadaRedactPersonalInfo())
        );
        LOG.info(
            "ESCADA: Lehrperson {} deactivated, internalId: '{}', externalId: '{}'",
            result.getFriendlyName(),
            result.getInternalId(),
            result.getExternalId()
        );
        return result;
    }

    @NonNull
    private static Geschlecht mapGeschlechtFromEscadaAnrede(@Nullable String klasseLehrpersonAnrede) {
        if ("Herr".equals(klasseLehrpersonAnrede)) {
            return Geschlecht.MAENNLICH;
        }
        if ("Frau".equals(klasseLehrpersonAnrede)) {
            return Geschlecht.WEIBLICH;
        }
        return Geschlecht.UNBEKANNT;
    }
}
