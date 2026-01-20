package ch.dvbern.oss.hemed.esc.api.features.schule;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ExternalImportInfoJson;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerMapper;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.schule.json.SchuleJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.json.SchuleOptionJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.json.SchuleSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.KlasseService;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.klasseexternal.EscadaKlasseStufe;
import ch.dvbern.oss.hemed.esc.api.features.schule.quartier.QuartierMapper;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.ZustaendigePersonService;
import ch.dvbern.oss.hemed.esc.api.shared.person.AdresseMapper;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatusSaveJson;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@RequiredArgsConstructor
public final class SchuleMapper {

    private final SchuleService schuleService;
    private final BenutzerService benutzerService;
    private final QuartierMapper quartierMapper;
    private final ZustaendigePersonService zustaendigePersonService;
    private final KlasseService klasseService;

    @NonNull
    public static SchuleJson toJson(@NonNull Schule schule, @NonNull List<EscadaKlasseStufe> klassenstufen) {
        return new SchuleJson(
            schule.getId(),
            ExternalImportInfoJson.toJson(schule.getExternalImportInfo()),
            EntityInfoJson.fromEntity(schule),
            new SchuleSaveJson(
                schule.getSchulhaus(),
                schule.getSchulstandort(),
                schule.getSchulkreis(),
                QuartierMapper.toOptionJson(schule.getQuartier()),
                schule.getSchulhausnummer(),
                AdresseMapper.toJson(schule.getAdresse()),
                schule.getEmail(),
                schule.getTelefon(),
                schule.getBemerkungen(),
                BenutzerMapper.toOptionJson(schule.getZustaendigArzt()),
                BenutzerMapper.toOptionJson(schule.getZustaendigMPA()),
                schule.getSchuleAktivStatus(),
                schule.getVersion()
            ),
            klassenstufen
        );
    }

    @NonNull
    public static SchuleOptionJson toOptionJson(@NonNull Schule schule) {
        return new SchuleOptionJson(
            schule.getId(),
            schule.getSchulhaus(),
            schule.getSchuleAktivStatus()
        );
    }

    @NonNull
    private Schule toEntity(
        @NonNull Schule existingSchule,
        @NonNull SchuleSaveJson saveJson
    ) {
        existingSchule.setSchulhaus(saveJson.getSchulhaus());
        existingSchule.setSchulstandort(saveJson.getSchulstandort());
        existingSchule.setSchulkreis(saveJson.getSchulkreis());
        existingSchule.setSchulhausnummer(saveJson.getSchulhausnummer());
        existingSchule.setQuartier(quartierMapper.findByOptionJson(saveJson.getQuartier()));
        existingSchule.setAdresse(AdresseMapper.toEntity(saveJson.getAdresse()));
        existingSchule.setEmail(saveJson.getEmail());
        existingSchule.setTelefon(saveJson.getTelefon());
        existingSchule.setBemerkungen(saveJson.getBemerkungen());
        existingSchule.setZustaendigArzt(benutzerService.getBenutzerIfCorrectRole(
            saveJson.getZustaendigArzt(),
            FachRolle.SCHULAERZTIN));
        existingSchule.setZustaendigMPA(benutzerService.getBenutzerIfOneOfRolesIsCorrect(
            saveJson.getZustaendigMPA(),
            List.of(FachRolle.MPA, FachRolle.PFP)));
        existingSchule.setSchuleAktivStatus(saveJson.getSchuleAktivStatus());
        return existingSchule;
    }

    @NonNull
    private Schule toEntityForCreate(@NonNull SchuleSaveJson saveJson) {
        return new Schule(
            null, // Null because we are creating a new entity from our System, not importing
            saveJson.getSchulhaus(),
            saveJson.getSchulstandort(),
            saveJson.getSchulkreis(),
            quartierMapper.findByOptionJson(saveJson.getQuartier()),
            saveJson.getSchulhausnummer(),
            AdresseMapper.toEntity(saveJson.getAdresse()),
            saveJson.getEmail(),
            saveJson.getTelefon(),
            saveJson.getBemerkungen(),
            benutzerService.getBenutzerIfCorrectRole(saveJson.getZustaendigArzt(), FachRolle.SCHULAERZTIN),
            benutzerService.getBenutzerIfOneOfRolesIsCorrect(saveJson.getZustaendigMPA(), List.of(FachRolle.MPA, FachRolle.PFP)),
            EntityAktivStatus.AKTIV
        );
    }

    @Nullable
    public Schule toEntity(@Nullable SchuleOptionJson schuleOptionJson) {
        if (schuleOptionJson == null) {
            return null;
        }

        return this.schuleService.getById(schuleOptionJson.getId());
    }

    @NonNull
    public List<SchuleJson> findAll() {
        return schuleService.findAll().stream()
            .map((Schule schule) -> toJson(
                schule,
                klasseService.extractEscadaKlassenStufen(schule.getId())))
            .toList();
    }

    @NonNull
    public List<SchuleOptionJson> findAllOptions(boolean onlyActive) {
        List<Schule> schulen;
        if (onlyActive) {
            schulen = schuleService.findAllAktiv();
        } else {
            schulen = schuleService.findAll();
        }

        return schulen.stream()
            .map(SchuleMapper::toOptionJson)
            .toList();
    }

    @NonNull
    public SchuleJson getById(@NonNull ID<Schule> schuleId) {
        return toJson(
            schuleService.getById(schuleId),
            klasseService.extractEscadaKlassenStufen(schuleId));
    }

    @NonNull
    public SchuleJson create(@NonNull SchuleSaveJson saveJson) {
        Schule entity = schuleService.persist(toEntityForCreate(saveJson));
        return toJson(entity, klasseService.extractEscadaKlassenStufen(entity.getId()));
    }

    @NonNull
    public SchuleJson update(@NonNull ID<Schule> schuleId, @NonNull SchuleSaveJson saveJson) {
        Schule existingSchule = schuleService.getById(schuleId);
        zustaendigePersonService.resetZustaendigkeitenOnDossierIfChangedOnSchule(existingSchule, saveJson);
        Schule updatedSchule = toEntity(existingSchule, saveJson);
        Schule entity = schuleService.persistWithOptimisticLocking(updatedSchule, saveJson.getVersion());
        return toJson(entity, klasseService.extractEscadaKlassenStufen(entity.getId()));
    }

    public SchuleJson changeSchuleAktivStatus(
        @NonNull ID<Schule> schuleId,
        @NonNull EntityAktivStatusSaveJson saveJson
    ) {
        Schule schule = schuleService.getById(schuleId);
        if (schule.getExternalImportInfo() != null && schule.isAktivInExternalSystem()) {
            throw new AppValidationException(AppValidationMessage.escadaObjectsCannotBeDeactivated(schule));
        }
        schule.disableOptimisticLockingCheck();
        schule.setSchuleAktivStatus(saveJson.getEntityAktivStatus());

        return toJson(this.schuleService.persist(schule), klasseService.extractEscadaKlassenStufen(schuleId));
    }

    public void remove(@NonNull ID<Schule> schuleId) {
        Schule schule = schuleService.getById(schuleId);
        if (schule.getExternalImportInfo() != null && schule.isAktivInExternalSystem()) {
            throw new AppValidationException(AppValidationMessage.schuleToDeleteIsFromEscada(
                schule.getSchulhaus(),
                schuleId));
        }
        schuleService.remove(schuleId);
    }
}
