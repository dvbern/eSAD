package ch.dvbern.oss.hemed.esc.api.features.schule.klasse.klasseexternal;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ExternalImportInfoJson;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ImportTyp;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.SchuleService;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.KlasseService;
import ch.dvbern.oss.hemed.esc.api.features.schule.types.Klassenstufe;
import ch.dvbern.oss.hemed.esc.api.shared.externalsystem.ExternalSystemIdDTO;
import ch.dvbern.oss.hemed.esc.api.shared.externalsystem.ItemSyncResultJson;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
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
public class KlasseExternalSyncHelperService {

    private final KlasseExternalRepo klasseExternalRepo;
    private final KlasseService klasseService;
    private final SchuleService schuleService;
    private final AppClock appClock;

    @NonNull
    public static Klasse toEntityForCreate(
        @NonNull Schule schuleToConenct,
        @NonNull KlasseExternal klasseExternal,
        @NonNull OffsetDateTime now,
        @NonNull ImportTyp importTyp
    ) {
        String bezeichnung = Objects.toString(klasseExternal.getKlasseBezeichnung(), "N/A");
        return new Klasse(
            ExternalImportInfoJson.toEntityOrCreate(null, klasseExternal.getKlasseId(), now, importTyp),
            schuleToConenct,
            new ArrayList<>(),
            bezeichnung,
            mapKlassenstufe(klasseExternal.getKlasseStufe(), klasseExternal.getKlasseProgrammjahr(), importTyp),
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
        @Nullable String klasseProgrammjahr,
        @NonNull ImportTyp importTyp) {
        if (klasseStufe == null || klasseProgrammjahr == null) {
            throw new IllegalArgumentException("klasseStufe and klasseProgrammjahr must not be null");
        }
        int programmjahr = Integer.parseInt(klasseProgrammjahr);
        if (importTyp == ImportTyp.PUPIL) {
            PupilKlasseStufe pupilKlasseStufe = PupilKlasseStufe.fromString(klasseStufe);
            return Klassenstufe.fromPupilImport(pupilKlasseStufe, programmjahr);
        }
        EscadaKlasseStufe escadaKlasseStufe = EscadaKlasseStufe.fromString(klasseStufe);
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
        @NonNull OffsetDateTime now,
        @NonNull ImportTyp importTyp
    ) {
        klasse.setBezeichnung(Objects.toString(updatedKlasseExt.getKlasseBezeichnung(), "N/A"));
        klasse.setKlassenstufe(mapKlassenstufe(
            updatedKlasseExt.getKlasseStufe(),
            updatedKlasseExt.getKlasseProgrammjahr(),
            importTyp)
        );
        klasse.setKlasseSchulart(mapKlassentyp(updatedKlasseExt.getKlasseSchulart()));

        klasse.setExternalImportInfo(ExternalImportInfoJson.toEntityOrCreate(
            klasse.getExternalImportInfo(), updatedKlasseExt.getKlasseId(), now, importTyp));

        return klasse;
    }

    @Transactional(TxType.REQUIRES_NEW)
    @NonNull
    public ItemSyncResultJson<Klasse> updateExistingKlasse(
        @NonNull ExternalSystemIdDTO idDTO,
        @NonNull KlasseExternal remoteKlasseExternal,
        @NonNull ImportTyp importTyp
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
            appClock.offsetDateTime().now(),
            importTyp
        );
        ensureKlasseAktivStatus(updatedKlasse, importTyp);
        updatedKlasse.disableOptimisticLockingCheck();
        updatedKlasse = klasseService.persist(updatedKlasse);

        return ItemSyncResultJson.createUpdateOkItem(idDTO, Klasse.class, updatedKlasse.getBezeichnung());
    }

    @NonNull
    @Transactional(TxType.REQUIRES_NEW)
    public ItemSyncResultJson<Klasse> addNewKlasse(
        @NonNull KlasseExternal klasseExternalToAdd,
        @NonNull ImportTyp importTyp
    ) {
        Schule schuleToConnect = getSchuleByExternalIdOrDummy(klasseExternalToAdd, importTyp);
        Optional<Klasse> preexistingKlasseOpt = findPreexistingKlasse(klasseExternalToAdd);
        Klasse internalKlasseToStore = preexistingKlasseOpt
            .map(klasse -> {
                var updatedKlasse = updateKlasseBasedOnKlasseExternal(
                    klasse,
                    klasseExternalToAdd,
                    appClock.offsetDateTime().now(),
                    importTyp
                );
                ensureKlasseAktivStatus(updatedKlasse, importTyp);
                return updatedKlasse;
            })
            .orElseGet(() -> toEntityForCreate(
                schuleToConnect,
                klasseExternalToAdd,
                appClock.offsetDateTime().now(),
                importTyp
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
            "{}: Klasse {} added{}, internalId: '{}', externalId: '{}'",
            importTyp.toString().toUpperCase(),
            result.getFriendlyName(),
            preexistingKlasseOpt.isEmpty() ? "" : " (preexisting)",
            result.getInternalId(),
            result.getExternalId()
        );
        return result;
    }

    private static void ensureKlasseAktivStatus(
        @NonNull Klasse updatedKlasse,
        @NonNull ImportTyp importTyp
    ) {
        if (updatedKlasse.getKlasseAktivStatus() == EntityAktivStatus.INAKTIV) {
            LOG.info(
                "{}: Klasse with UUID {} and ExternalID {} was INAKTIV, resetting to AKTIV",
                importTyp.toString().toUpperCase(),
                updatedKlasse.getId().getUUID(),
                updatedKlasse.getExternalSystemIdOrNull());
            updatedKlasse.setKlasseAktivStatus(EntityAktivStatus.AKTIV);
        }
    }

    private Optional<Klasse> findPreexistingKlasse(@NonNull KlasseExternal klasseExternalToAdd) {
        return klasseService.findByExternalId(klasseExternalToAdd.getExternalId());
    }

    @NonNull
    private Schule getSchuleByExternalIdOrDummy(@NonNull KlasseExternal klasseExternal, @NonNull ImportTyp importTyp) {
        String escadaSchuleId = klasseExternal.getEscadaSchuleId();
        Schule schuleToConnect;
        if (StringUtils.isEmpty(escadaSchuleId)) {
            schuleToConnect =
                schuleService.getDummySchuleForUnassignedEscadaKlassen(klasseExternal.getSchulkreisBezeichnung());
            LOG.warn(
                "{}: Schule externalId is null for escadaKlasse {}, using dummy Schule {}",
                importTyp.toString().toUpperCase(),
                klasseExternal.getKlasseBezeichnung(),
                schuleToConnect.getSchulstandort());
        } else {
            LOG.debug(
                "{}: Schule externalId id should match an already existing Schule in the system with externalId {}",
                importTyp.toString().toUpperCase(),
                escadaSchuleId);
            schuleToConnect = schuleService.getByExternalId(escadaSchuleId);
        }
        return schuleToConnect;
    }

    @NonNull
    @Transactional(TxType.REQUIRES_NEW)
    public ItemSyncResultJson<Klasse> deactivateRemovedKlasse(@NonNull ExternalSystemIdDTO externalSystemIdDTO, @NonNull ImportTyp importTyp) {
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
            LOG.info(
                "{}: Changed Klasse to klasseAktivStatus=INAKTIV id='{}', name='{}', externalId='{}'",
                importTyp.toString().toUpperCase(),
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
            "{}: Klasse {} deactivated, internalId: '{}', externalId: '{}'",
            importTyp.toString().toUpperCase(),
            result.getFriendlyName(),
            result.getInternalId(),
            result.getExternalId()
        );
        return result;
    }
}
