package ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ExternalImportInfoJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.KlasseMapper;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.KlasseService;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.json.KlasseLehrpersonenJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.json.KlasseOptionJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.json.LehrpersonJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.json.LehrpersonOptionJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.json.LehrpersonSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.util.PathParamConsistancyUtil;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@RequiredArgsConstructor
public final class LehrpersonMapper {

    private final LehrpersonService lehrpersonService;
    private final KlasseService klasseService;

    @NonNull
    public static LehrpersonJson toJson(@NonNull Lehrperson lehrperson) {
        return new LehrpersonJson(
            lehrperson.getId(),
            ExternalImportInfoJson.toJson(lehrperson.getExternalImportInfo()),
            lehrperson.isManualOrAktivInExternalSystem(),
            EntityInfoJson.fromEntity(lehrperson),
            new LehrpersonSaveJson(
                lehrperson.getGeschlecht(),
                lehrperson.getName(),
                lehrperson.getVorname(),
                lehrperson.getEmail(),
                lehrperson.getTelefon(),
                lehrperson.getVersion()
            )
        );
    }

    @NonNull
    public static Lehrperson toEntity(
        @NonNull Lehrperson existingLehrperson,
        @NonNull LehrpersonSaveJson saveJson
    ) {
        existingLehrperson.setGeschlecht(saveJson.getGeschlecht());
        existingLehrperson.setName(saveJson.getName());
        existingLehrperson.setVorname(saveJson.getVorname());
        existingLehrperson.setEmail(saveJson.getEmail());
        existingLehrperson.setTelefon(saveJson.getTelefon());
        return existingLehrperson;
    }

    @NonNull
    public static LehrpersonOptionJson toOptionJson(@NonNull Lehrperson lehrperson) {
        return new LehrpersonOptionJson(
            lehrperson.getId(),
            lehrperson.getGeschlecht(),
            lehrperson.getName(),
            lehrperson.getVorname(),
            lehrperson.getName() + ' ' + lehrperson.getVorname(),
            lehrperson.isManualOrAktivInExternalSystem());
    }

    @NonNull
    public List<LehrpersonOptionJson> findAllOptionsActive(@Nullable ID<Klasse> klasseToExclude) {
        return lehrpersonService.findAllOptionsActive(klasseToExclude)
            .stream()
            .map(LehrpersonMapper::toOptionJson)
            .toList();
    }

    @NonNull
    public List<LehrpersonJson> findAllAktivByKlasse(@NonNull ID<Klasse> klasseID) {
        return lehrpersonService.findAktivByKlasse(klasseID)
            .stream()
            .map(LehrpersonMapper::toJson)
            .toList();
    }

    @NonNull
    public List<KlasseLehrpersonenJson> findLehrpersonenByKlassen(@NonNull List<Klasse> klassen) {
        List<KlasseLehrpersonenJson> list = new ArrayList<>();
        klassen.forEach(klasse -> {
            KlasseOptionJson optionJson = KlasseMapper.toOptionJson(klasse);
            List<LehrpersonOptionJson> kpsByKlasse =
                lehrpersonService.findByKlasse(klasse.getId())
                    .stream()
                    .map(LehrpersonMapper::toOptionJson)
                    .toList();
            list.add(new KlasseLehrpersonenJson(optionJson, kpsByKlasse));
        });
        return list;
    }

    @NonNull
    public LehrpersonJson getById(@NonNull ID<Lehrperson> lehrpersonID) {
        return toJson(lehrpersonService.getById(lehrpersonID));
    }

    @NonNull
    public LehrpersonJson create(@NonNull ID<Klasse> klasseId, @NonNull LehrpersonSaveJson saveJson) {
        Klasse klasse = klasseService.getById(klasseId);
        Lehrperson entity = new Lehrperson(
            null, // Null because we are creating a new entity from our System, not importing
            Set.of(klasse),
            saveJson.getGeschlecht(),
            saveJson.getName(),
            saveJson.getVorname(),
            saveJson.getEmail(),
            saveJson.getTelefon()
        );
        entity = lehrpersonService.persist(entity);
        klasseService.addLehrpersonToKlasse(klasse, entity);
        return toJson(entity);
    }

    @NonNull
    public LehrpersonJson update(
        @NonNull ID<Lehrperson> lehrpersonID,
        @NonNull LehrpersonSaveJson saveJson
    ) {
        Lehrperson existingLehrperson = getLehrpersonAndEnsureNotExternalAktiv(lehrpersonID);
        Lehrperson entity = toEntity(existingLehrperson, saveJson);
        return toJson(lehrpersonService.persistWithOptimisticLocking(entity, saveJson.getVersion()));
    }

    public void delete(@NonNull ID<Lehrperson> lehrpersonID) {
        Lehrperson lehrpers = getLehrpersonAndEnsureNotExternalAktiv(lehrpersonID);
        klasseService.findByLehrperson(lehrpers.getId())
            .forEach(klasse -> {
                lehrpers.getKlassen().remove(klasse);
                klasseService.removeLehrpersonFromKlasse(klasse, lehrpers);
            });
        lehrpersonService.remove(lehrpers.getId());
    }

    public LehrpersonJson addToKlasse(
        @NonNull ID<Klasse> klasseId,
        @NonNull ID<Lehrperson> lehrpersonID
    ) {
        Lehrperson lehrperson = getLehrpersonAndEnsureNotExternalAktiv(lehrpersonID);
        Klasse klasse = klasseService.getById(klasseId);
        klasseService.addLehrpersonToKlasse(klasse, lehrperson);
        lehrperson.getKlassen().add(klasse);
        lehrperson.disableOptimisticLockingCheck();
        return toJson(lehrpersonService.persist(lehrperson));
    }

    public void removeFromKlasse(@NonNull ID<Klasse> klasseId, @NonNull ID<Lehrperson> lehrpersonID) {
        Lehrperson lehrperson = getLehrpersonAndEnsureNotExternalAktiv(lehrpersonID);
        Klasse klasse = klasseService.getById(klasseId);
        klasseService.removeLehrpersonFromKlasse(klasse, lehrperson);
        lehrperson.getKlassen().remove(klasse);
        lehrperson.disableOptimisticLockingCheck();
        lehrpersonService.persist(lehrperson);
        // Loeschen, falls es die letzte Verknuepfung war
        if (lehrperson.getKlassen().isEmpty()) {
            lehrpersonService.remove(lehrperson.getId());
        }
    }

    @NonNull
    private Lehrperson getLehrpersonAndEnsureNotExternalAktiv(
        @NonNull ID<Lehrperson> lehrpersonID
    ) {
        Lehrperson lehrperson = lehrpersonService.getById(lehrpersonID);
        if (lehrperson.isAktivInExternalSystem()) {
            throw new AppValidationException(AppValidationMessage.escadaObjectsCannotBeUpdated(lehrperson));
        }
        return lehrperson;
    }

	public void requireConsistentPathParams(@NonNull ID<Lehrperson> lehrpersonID, @NonNull ID<Klasse> expectedKlasseId) {
        PathParamConsistancyUtil.requireContainsForeignId(
            lehrpersonID,
            lehrpersonService.getById(lehrpersonID).getKlassen().stream().map(Klasse::getId).toList(),
            expectedKlasseId
        );
    }
}
