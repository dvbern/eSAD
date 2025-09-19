package ch.dvbern.oss.hemed.esc.api.features.schule.klasse;

import java.util.Collections;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ExternalImportInfoJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.SchuleMapper;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.json.KlasseJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.json.KlasseOptionJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.json.KlasseSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.LehrpersonMapper;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatusSaveJson;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@JaxMapper
@RequiredArgsConstructor
public final class KlasseMapper {

    private final KlasseService klasseService;
    private final SchuleMapper schuleMapper;

    @NonNull
    public static KlasseJson toJson(@NonNull Klasse klasse) {
        return new KlasseJson(
            klasse.getId(),
            ExternalImportInfoJson.toJson(klasse.getExternalImportInfo()),
            klasse.isManualOrAktivInExternalSystem(),
            EntityInfoJson.fromEntity(klasse),
            new KlasseSaveJson(
                SchuleMapper.toOptionJson(klasse.getSchule()),
                klasse.getBezeichnung(),
                klasse.getKlassenstufe(),
                klasse.getKlasseSchulart(),
                klasse.getKlasseAktivStatus(),
                klasse.getVersion()
            ),
            klasse.getLehrpersonen().stream()
                .map(LehrpersonMapper::toOptionJson)
                .toList()
        );
    }

    @NonNull
    public static KlasseOptionJson toOptionJson(@NonNull Klasse klasse) {
        return new KlasseOptionJson(
            klasse.getId(),
            klasse.getBezeichnung(),
            klasse.getKlasseAktivStatus()
        );
    }

    @NonNull
    private Klasse toEntity(@NonNull Klasse existingKlasse, @NonNull KlasseSaveJson saveJson) {
        if (existingKlasse.getExternalImportInfo() != null && existingKlasse.isAktivInExternalSystem()) {
            throw new AppValidationException(AppValidationMessage.escadaObjectsCannotBeUpdated(existingKlasse));
        }
        Schule schule = schuleMapper.toEntity(saveJson.getSchule());
        existingKlasse.setSchule(schule);
        existingKlasse.setBezeichnung(saveJson.getBezeichnung());
        existingKlasse.setKlassenstufe(saveJson.getKlassenstufe());
        existingKlasse.setKlasseSchulart(saveJson.getKlasseSchulart());
        existingKlasse.setKlasseAktivStatus(saveJson.getKlasseAktivStatus());
        return existingKlasse;
    }

    @NonNull
    private Klasse toEntityForCreate(@NonNull KlasseSaveJson saveJson) {
        return new Klasse(
            null, // Null because we are creating a new entity from our System, not importing
            schuleMapper.toEntity(saveJson.getSchule()),
            Collections.emptyList(),
            saveJson.getBezeichnung(),
            saveJson.getKlassenstufe(),
            saveJson.getKlasseSchulart(),
            EntityAktivStatus.AKTIV
        );
    }

    @NonNull
    public List<KlasseJson> findAll() {
        return klasseService.findAll().stream()
            .map(KlasseMapper::toJson)
            .toList();
    }

    @NonNull
    public List<KlasseJson> findBySchule(@NonNull ID<Schule> schuleID) {
        return klasseService.findBySchule(schuleID).stream()
            .map(KlasseMapper::toJson)
            .toList();
    }

    @NonNull
    public List<KlasseOptionJson> findOptionsBySchule(@NonNull ID<Schule> schuleId, boolean onlyActive) {
        List<Klasse> klassen;
        if (onlyActive) {
            klassen = klasseService.findAktivBySchule(schuleId);
        } else {
            klassen = klasseService.findBySchule(schuleId);
        }
        return klassen.stream()
            .map(KlasseMapper::toOptionJson)
            .toList();
    }

    @NonNull
    public KlasseJson getById(@NonNull ID<Klasse> klasseID) {
        return toJson(klasseService.getById(klasseID));
    }

    @NonNull
    public KlasseJson create(@NonNull KlasseSaveJson klasseJson) {
        Klasse entity = toEntityForCreate(klasseJson);
        return toJson(klasseService.persist(entity));
    }

    @NonNull
    public KlasseJson update(
        @NonNull ID<Klasse> klasseID,
        @NonNull KlasseSaveJson klasseJson
    ) {
        Klasse existingKlasse = klasseService.getById(klasseID);
        Klasse entity = toEntity(existingKlasse, klasseJson);
        return toJson(klasseService.persistWithOptimisticLocking(entity, klasseJson.getVersion()));
    }

    public KlasseJson changeKlasseAktivStatus(
        @NonNull ID<Klasse> klasseId,
        @NonNull EntityAktivStatusSaveJson saveJson) {
        Klasse klasse = klasseService.getById(klasseId);
        if (klasse.getExternalImportInfo() != null && klasse.isAktivInExternalSystem()) {
            throw new AppValidationException(AppValidationMessage.escadaObjectsCannotBeDeactivated(klasse));
        }
        klasse.setKlasseAktivStatus(saveJson.getEntityAktivStatus());
        klasse.disableOptimisticLockingCheck();
        return toJson(this.klasseService.persist(klasse));
    }

    public void remove(@NonNull ID<Klasse> klasseId) {
        Klasse klasse = klasseService.getById(klasseId);
        if (klasse.getExternalImportInfo() != null && klasse.isAktivInExternalSystem()) {
            throw new AppValidationException(AppValidationMessage.klasseToDeleteIsFromEscada(
                klasse.getBezeichnung(),
                klasseId));
        }
        klasseService.remove(klasseId);
    }
}
