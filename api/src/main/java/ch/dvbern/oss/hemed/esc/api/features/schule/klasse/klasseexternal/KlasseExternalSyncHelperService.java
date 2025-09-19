package ch.dvbern.oss.hemed.esc.api.features.schule.klasse.klasseexternal;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ExternalImportInfoJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.SchuleService;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.KlasseService;
import ch.dvbern.oss.hemed.esc.api.features.schule.types.Klassenstufe;
import ch.dvbern.oss.hemed.esc.api.shared.escada.ExternalSystemIdDTO;
import ch.dvbern.oss.hemed.esc.api.shared.escada.ItemSyncResultJson;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class KlasseExternalSyncHelperService {

    private final KlasseExternalRepo klasseExternalRepo;
    private final KlasseService klasseService;
    private final SchuleService schuleService;
    private final AppClock appClock;

    @NonNull
    public static Klasse toEntityForCreate(
        @NonNull Schule schuleToConenct,
        @NonNull KlasseExternal klasseExternal,
        @NonNull OffsetDateTime now
    ) {
        String bezeichnung = Objects.toString(klasseExternal.getKlasseBezeichnung(), "N/A");
        return new Klasse(
            ExternalImportInfoJson.toEntityOrCreate(null, klasseExternal.getKlasseId(), now),
            schuleToConenct,
            new ArrayList<>(),
            bezeichnung,
            mapKlassenstufe(klasseExternal.getKlasseStufe(), klasseExternal.getKlasseProgrammjahr()),
            Objects.toString(klasseExternal.getKlasseSchulart(), "N/A"),
            EntityAktivStatus.AKTIV
        );
    }

    @NonNull
    private static String mapKlassentyp(
        @Nullable String klasseSchulart
    ) {
        return Objects.toString(klasseSchulart, "N/A");
    }

    @NonNull
    private static Klassenstufe mapKlassenstufe(
        @Nullable String klasseStufe,
        @Nullable String klasseProgrammjahr) {
        if (klasseStufe == null || klasseProgrammjahr == null) {
            throw new IllegalArgumentException("klasseStufe and klasseProgrammjahr must not be null");
        }
        EscadaKlasseStufe escadaKlasseStufe = EscadaKlasseStufe.fromString(klasseStufe);
        int programmjahr = Integer.parseInt(klasseProgrammjahr);
        return Klassenstufe.fromEscadaImport(escadaKlasseStufe, programmjahr);

    }

    @NonNull
    public static KlasseExternal toEntityKlasseExternal(
        @NonNull KlasseExternal existingKlasseExternal,
        @NonNull KlasseExternal remoteKlasseExternal
    ) {
        existingKlasseExternal.setKlasseBezeichnung(remoteKlasseExternal.getKlasseBezeichnung());
        existingKlasseExternal.setKlasseStufe(remoteKlasseExternal.getKlasseStufe());
        existingKlasseExternal.setKlasseSchulart(remoteKlasseExternal.getKlasseSchulart());
        existingKlasseExternal.setKlasseProgrammjahr(remoteKlasseExternal.getKlasseProgrammjahr());
        return existingKlasseExternal;
    }

    @NonNull
    public static Klasse updateKlasseBasedOnKlasseExternal(
        @NonNull Klasse klasse,
        @NonNull KlasseExternal updatedKlasseExt,
        @NonNull OffsetDateTime now
    ) {
        klasse.setBezeichnung(Objects.toString(updatedKlasseExt.getKlasseBezeichnung(), "N/A"));
        klasse.setKlassenstufe(mapKlassenstufe(
            updatedKlasseExt.getKlasseStufe(),
            updatedKlasseExt.getKlasseProgrammjahr())
        );
        klasse.setKlasseSchulart(mapKlassentyp(updatedKlasseExt.getKlasseSchulart()));

        klasse.setExternalImportInfo(ExternalImportInfoJson.toEntityOrCreate(
            klasse.getExternalImportInfo(), updatedKlasseExt.getKlasseId(), now));

        return klasse;
    }

    @Transactional(TxType.REQUIRES_NEW)
    @NonNull
    public ItemSyncResultJson<Klasse> updateExistingKlasse(
        @NonNull ExternalSystemIdDTO idDTO,
        @NonNull KlasseExternal remoteKlasseExternal
    ) {
        Klasse existingKlasse = klasseService.getById(ID.of(idDTO.getInternalId(), Klasse.class));
        KlasseExternal klasseExternalToUpdate = klasseExternalRepo.getExternalKlasseForKlasse(existingKlasse.getId());

        Validate.isTrue(
            Objects.requireNonNull(klasseExternalToUpdate).getExternalId().equals(idDTO.getExternalId()),
            "ExternalId mismatch, expected: '%s', actual: '%s' for existingKlasse", idDTO.getExternalId(),
            klasseExternalToUpdate.getExternalId(), existingKlasse.getId());

        KlasseExternal updatedKlasseExt = KlasseExternalSyncHelperService.toEntityKlasseExternal(
            klasseExternalToUpdate,
            remoteKlasseExternal
        );
        klasseExternalRepo.persist(updatedKlasseExt);

        Klasse updatedKlasse = KlasseExternalSyncHelperService.updateKlasseBasedOnKlasseExternal(
            existingKlasse,
            updatedKlasseExt,
            appClock.offsetDateTime().now()
        );
        updatedKlasse.disableOptimisticLockingCheck();
        updatedKlasse = klasseService.persist(updatedKlasse);

        return ItemSyncResultJson.createUpdateOkItem(idDTO, Klasse.class, updatedKlasse.getBezeichnung());
    }

    @NonNull
    @Transactional(TxType.REQUIRES_NEW)
    public ItemSyncResultJson<Klasse> addNewKlasse(@NonNull KlasseExternal klasseExternalToAdd) {
        Schule schuleToConnect = getSchuleByExternalIdOrDummy(klasseExternalToAdd);
        Optional<Klasse> preexistingKlasseOpt = findPreexistingKlasse(klasseExternalToAdd);
        Klasse internalKlasseToStore = preexistingKlasseOpt
            .map(klasse -> updateKlasseBasedOnKlasseExternal(
                klasse,
                klasseExternalToAdd,
                appClock.offsetDateTime().now()
            ))
            .orElseGet(() -> toEntityForCreate(
                schuleToConnect,
                klasseExternalToAdd,
                appClock.offsetDateTime().now()
            ));
        internalKlasseToStore = klasseService.persist(internalKlasseToStore);
        klasseExternalToAdd.setKlasse(internalKlasseToStore);
        klasseExternalRepo.persist(klasseExternalToAdd);

        var result = ItemSyncResultJson.createAddOkItem(
            klasseExternalToAdd,
            internalKlasseToStore.getId(),
            internalKlasseToStore.getBezeichnung()
        );
        LOG.info(
            "ESCADA: Klasse {} added{}, internalId: '{}', externalId: '{}'",
            result.getFriendlyName(),
            preexistingKlasseOpt.isEmpty() ? "" : " (preexisting)",
            result.getInternalId(),
            result.getExternalId()
        );
        return result;
    }

    private Optional<Klasse> findPreexistingKlasse(@NonNull KlasseExternal klasseExternalToAdd) {
        return klasseService.findByExternalId(klasseExternalToAdd.getExternalId());
    }

    @NonNull
    private Schule getSchuleByExternalIdOrDummy(@NonNull KlasseExternal klasseExternal) {
        String escadaSchuleId = klasseExternal.getEscadaSchuleId();
        Schule schuleToConnect;
        if (StringUtils.isEmpty(escadaSchuleId)) {
            schuleToConnect =
                schuleService.getDummySchuleForUnassignedEscadaKlassen(klasseExternal.getSchulkreisBezeichnung());
            LOG.warn(
                "ESCADA: Schule externalId is null for escadaKlasse {}, using dummy Schule {}",
                klasseExternal.getKlasseBezeichnung(),
                schuleToConnect.getSchulstandort());
        } else {
            LOG.debug(
                "ESCADA: Schule externalId id should match an already existing Schule in the system with externalId {}",
                escadaSchuleId);
            schuleToConnect = schuleService.getByExternalId(escadaSchuleId);
        }
        return schuleToConnect;
    }

    @NonNull
    @Transactional(TxType.REQUIRES_NEW)
    public ItemSyncResultJson<Klasse> deactivateRemovedKlasse(@NonNull ExternalSystemIdDTO externalSystemIdDTO) {
        Klasse klasse = klasseService.getById(ID.of(externalSystemIdDTO.getInternalId(), Klasse.class));
        KlasseExternal klasseExternalToDelete = klasseExternalRepo.getExternalKlasseForKlasse(klasse.getId());

        Validate.isTrue(
            Objects.requireNonNull(klasseExternalToDelete).getExternalId()
                .equals(externalSystemIdDTO.getExternalId()),
            "ExternalId mismatch, expected: '%s', actual: '%s' for klasse",
            externalSystemIdDTO.getExternalId(),
            klasseExternalToDelete.getExternalId(),
            klasse.getId());

        if (klasse.getExternalImportInfo() != null && klasse.isAktivInExternalSystem()) {
            klasse.getExternalImportInfo().setAktivInExternalSystem(false);
            klasse.disableOptimisticLockingCheck();
        }
        klasseExternalRepo.remove(klasseExternalToDelete.getId());
        if (klasse.getKlasseAktivStatus() == EntityAktivStatus.AKTIV) {
            klasse.setKlasseAktivStatus(EntityAktivStatus.INAKTIV);
            klasseService.persist(klasse);
            LOG.info("ESCADA: Changed Klasse to klasseAktivStatus=INAKTIV id='{}', name='{}', externalId='{}'",
                klasse.getId(),
                klasse.getBezeichnung(),
                klasse.getExternalSystemIdOrNull()
            );
        }

        var result = ItemSyncResultJson.createDeactivateOkItem(
            externalSystemIdDTO,
            Klasse.class,
            klasse.getBezeichnung()
        );
        LOG.info(
            "ESCADA: Klasse {} deactivated, internalId: '{}', externalId: '{}'",
            result.getFriendlyName(),
            result.getInternalId(),
            result.getExternalId()
        );
        return result;
    }
}
