package ch.dvbern.oss.hemed.esc.api.features.schule.schulenexternal;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ExternalImportInfoJson;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ImportTyp;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.SchuleService;
import ch.dvbern.oss.hemed.esc.api.shared.externalsystem.ExternalSystemIdDTO;
import ch.dvbern.oss.hemed.esc.api.shared.externalsystem.ItemSyncResultJson;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@Slf4j
@RequiredArgsConstructor
public class SchuleExternalSyncHelperService {

    private final SchuleExternalRepo schuleExternalRepo;
    private final SchuleService schuleService;
    private final AppClock appClock;

    @NonNull
    public static Schule toEntityForCreate(
        @NonNull SchuleExternal schuleExternal,
        @NonNull OffsetDateTime now,
        @NonNull ImportTyp importTyp
    ) {
        return new Schule(
            ExternalImportInfoJson.toEntityOrCreate(null, schuleExternal.getSchulhausId(), now, importTyp),
            Objects.toString(schuleExternal.getSchulhausBezeichnung(), "N/A"),
            schuleExternal.getSchulanlageBezeichnung(),
            schuleExternal.getSchulkreisBezeichnung(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            EntityAktivStatus.AKTIV
        );
    }

    @NonNull
    public static SchuleExternal toEntitySchuleExternal(
        @NonNull SchuleExternal existingSchuleExternal,
        @NonNull SchuleExternal remoteSchuleExternal
    ) {
        existingSchuleExternal.setSchulanlageId(remoteSchuleExternal.getSchulanlageId());
        existingSchuleExternal.setSchulanlageBezeichnung(remoteSchuleExternal.getSchulanlageBezeichnung());
        existingSchuleExternal.setSchulhausBezeichnung(remoteSchuleExternal.getSchulhausBezeichnung());
        existingSchuleExternal.setSchulkreisBezeichnung(existingSchuleExternal.getSchulkreisBezeichnung());
        return existingSchuleExternal;
    }

    @NonNull
    public static Schule updateSchuleBasedOnSchuleExternal(
        @NonNull Schule schule,
        @NonNull SchuleExternal updatedSchuleExt,
        @NonNull OffsetDateTime now,
        @NonNull ImportTyp importTyp
    ) {
        schule.setSchulhaus(Objects.toString(updatedSchuleExt.getSchulhausBezeichnung(), "N/A"));
        schule.setSchulstandort(updatedSchuleExt.getSchulanlageBezeichnung());
        schule.setSchulkreis(updatedSchuleExt.getSchulkreisBezeichnung());

        schule.setExternalImportInfo(ExternalImportInfoJson.toEntityOrCreate(
            schule.getExternalImportInfo(),
            updatedSchuleExt.getExternalId(),
            now,
            importTyp
        ));

        return schule;
    }

    @Transactional(TxType.REQUIRES_NEW)
    @NonNull
    public ItemSyncResultJson<Schule> updateExistingSchule(
        @NonNull ExternalSystemIdDTO idDTO,
        @NonNull SchuleExternal remoteSchuleExternal,
        @NonNull ImportTyp importTyp
    ) {
        Schule existingSchule = schuleService.getById(ID.of(idDTO.getInternalId(), Schule.class));
        SchuleExternal schuleExternalToUpdate = schuleExternalRepo.getExternalSchuleForSchule(existingSchule.getId());

        Validate.isTrue(
            Objects.requireNonNull(schuleExternalToUpdate).getExternalId().equals(idDTO.getExternalId()),
            "ExternalId mismatch, expected: '%s', actual: '%s' for existingSchule", idDTO.getExternalId(),
            schuleExternalToUpdate.getExternalId(), existingSchule.getId());

        SchuleExternal updatedSchuleExt =
            SchuleExternalSyncHelperService.toEntitySchuleExternal(schuleExternalToUpdate, remoteSchuleExternal);
        schuleExternalRepo.persist(updatedSchuleExt);
        Schule updatedSchule =
            SchuleExternalSyncHelperService.updateSchuleBasedOnSchuleExternal(
                existingSchule,
                updatedSchuleExt,
                appClock.offsetDateTime().now(),
                importTyp
            );
        ensureSchuleAktivStatus(updatedSchule, importTyp);
        updatedSchule.disableOptimisticLockingCheck();
        schuleService.persist(updatedSchule);
        return ItemSyncResultJson.createUpdateOkItem(idDTO, Schule.class, updatedSchule.getSchulhaus());
    }

    @NonNull
    @Transactional(TxType.REQUIRES_NEW)
    public ItemSyncResultJson<Schule> addNewSchule(@NonNull SchuleExternal schuleExternalToAdd, @NonNull ImportTyp importTyp) {
        Optional<Schule> preexistingSchuleOpt = findPreexistingSchule(schuleExternalToAdd);
        Schule internalSchuleToStore = preexistingSchuleOpt
            .map(existingSchule -> {
                var schuleUpdated = updateSchuleBasedOnSchuleExternal(
                    existingSchule,
                    schuleExternalToAdd,
                    appClock.offsetDateTime().now(),
                    importTyp
                );
                ensureSchuleAktivStatus(schuleUpdated, importTyp);
                return schuleUpdated;
            })
            .orElseGet(() -> SchuleExternalSyncHelperService.toEntityForCreate(
                schuleExternalToAdd,
                appClock.offsetDateTime().now(), importTyp
            ));

        internalSchuleToStore = schuleService.persist(internalSchuleToStore);
        schuleExternalToAdd.setSchule(internalSchuleToStore);
        schuleExternalRepo.persist(schuleExternalToAdd);

        var result = ItemSyncResultJson.createAddOkItem(
            schuleExternalToAdd,
            internalSchuleToStore.getId(),
            internalSchuleToStore.getSchulhaus());
        LOG.info(
            "{}: Schule {} added{},  internalId: '{}', externalId: '{}'",
            importTyp.toString().toUpperCase(),
            result.getFriendlyName(),
            preexistingSchuleOpt.isEmpty() ? "" : " (preexisting)",
            result.getInternalId(),
            result.getExternalId()
        );
        return result;
    }

    private static void ensureSchuleAktivStatus(Schule schuleUpdated, ImportTyp importTyp) {
        if (schuleUpdated.getSchuleAktivStatus() == EntityAktivStatus.INAKTIV) {
            LOG.info(
                "{}: Schule with UUID {} and ExternalID {} was INAKTIV, resetting to AKTIV",
                importTyp.toString().toUpperCase(),
                schuleUpdated.getId().getUUID(),
                schuleUpdated.getExternalSystemIdOrNull());
            schuleUpdated.setSchuleAktivStatus(EntityAktivStatus.AKTIV);
        }
    }

    private Optional<Schule> findPreexistingSchule(@NonNull SchuleExternal schuleExternalToAdd) {
        return schuleService.findByExternalId(schuleExternalToAdd.getExternalId());
    }

    @NonNull
    @Transactional(TxType.REQUIRES_NEW)
    public ItemSyncResultJson<Schule> deactivateRemovedSchule(
        @NonNull ExternalSystemIdDTO externalSystemIdDTO,
        @NonNull ImportTyp importTyp
    ) {
        Schule schule = schuleService.getById(ID.of(externalSystemIdDTO.getInternalId(), Schule.class));
        SchuleExternal schuleExternalToDelete = schuleExternalRepo.getExternalSchuleForSchule(schule.getId());
        Validate.isTrue(
            Objects.requireNonNull(schuleExternalToDelete).getExternalId().equals(externalSystemIdDTO.getExternalId()),
            "ExternalId mismatch, expected: '%s', actual: '%s' for schule", externalSystemIdDTO.getExternalId(),
            schuleExternalToDelete.getExternalId(), schule.getId());
        if (schule.getExternalImportInfo() != null && schule.isAktivInExternalSystem()) {
            schule.getExternalImportInfo().setAktivInExternalSystem(false);
            schule.disableOptimisticLockingCheck();
        }
        schuleExternalRepo.remove(schuleExternalToDelete.getId());
        if (schule.getSchuleAktivStatus() == EntityAktivStatus.AKTIV) {
            schule.setSchuleAktivStatus(EntityAktivStatus.INAKTIV);
            schuleService.persist(schule);
            LOG.info(
                "{}: Deactivated Schule id='{}', name='{}', externalId='{}'",
                importTyp.toString().toUpperCase(),
                schule.getId(),
                schule.getSchulhaus(),
                schule.getExternalSystemIdOrNull()
            );
        }

        var result =
            ItemSyncResultJson.createDeactivateOkItem(externalSystemIdDTO, Schule.class, schule.getSchulhaus());
        LOG.info(
            "{}: Schule {} deactivated, internalId: '{}', externalId: '{}'",
            importTyp.toString().toUpperCase(),
            result.getFriendlyName(),
            result.getInternalId(),
            result.getExternalId()
        );
        return result;
    }
}
