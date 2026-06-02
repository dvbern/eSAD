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
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.ZustaendigePersonService;
import ch.dvbern.oss.hemed.esc.api.shared.person.AdresseMapper;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatusSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.util.VerantwortlicherSchuleChangedDTO;
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
    private final TenantProducer tenantProducer;

    @NonNull
    public static SchuleJson toJson(@NonNull Schule schule, @NonNull List<EscadaKlasseStufe> klassenstufen, @NonNull TenantEnum tenant) {
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
                BenutzerMapper.toOptionJson(schule.getZustaendigArzt(), tenant),
                BenutzerMapper.toOptionJson(schule.getZustaendigMPA(), tenant),
                schule.getSchuleAktivStatus(),
                schule.getVersion()
            ),
            klassenstufen
        );
    }

    @NonNull
    public static SchuleOptionJson toOptionJson(@NonNull Schule schule) {
        Benutzer zustaendigArzt = schule.getZustaendigArzt();
        return new SchuleOptionJson(
            schule.getId(),
            zustaendigArzt != null && zustaendigArzt.getSingleOrganisation() != null
                ? zustaendigArzt.getSingleOrganisation().getId()
                : null,
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
            FachRolle.fachrollenHilfsperson()));
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
            benutzerService.getBenutzerIfOneOfRolesIsCorrect(
                saveJson.getZustaendigMPA(),
                FachRolle.fachrollenHilfsperson()),
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
        TenantEnum tenant = tenantProducer.produceTenant();
        return schuleService.findAll().stream()
            .map((Schule schule) -> toJson(
                schule,
                klasseService.extractEscadaKlassenStufen(schule.getId()),
                tenant))
            .toList();
    }

    @NonNull
    public List<SchuleOptionJson> findAllOptions() {
        List<Schule> schulen = schuleService.findAll();
        return schulen.stream()
            .map(SchuleMapper::toOptionJson)
            .toList();
    }

    @NonNull
    public SchuleJson getById(@NonNull ID<Schule> schuleId) {
        return toJson(
            schuleService.getById(schuleId),
            klasseService.extractEscadaKlassenStufen(schuleId),
            tenantProducer.produceTenant());
    }

    @NonNull
    public SchuleJson create(@NonNull SchuleSaveJson saveJson) {
        Schule entity = schuleService.persist(toEntityForCreate(saveJson));
        return toJson(entity, klasseService.extractEscadaKlassenStufen(entity.getId()), tenantProducer.produceTenant());
    }

    @NonNull
    public SchuleJson update(@NonNull ID<Schule> schuleId, @NonNull SchuleSaveJson saveJson) {
        Schule existingSchule = schuleService.getById(schuleId);
        // https://intra.dvbern.ch/spaces/HEM/pages/284263663/Zust%C3%A4ndigkeiten+Archivierung
        // Change Zustaendigkeiten: Fall 2: Neuer Arzt auf Schule (asynchron)
        VerantwortlicherSchuleChangedDTO args = VerantwortlicherSchuleChangedDTO.of(existingSchule, saveJson);
        zustaendigePersonService.verantwortlicherChangedOnSchuleAsync(existingSchule, args);
        Schule updatedSchule = toEntity(existingSchule, saveJson);
        Schule entity = schuleService.persistWithOptimisticLocking(updatedSchule, saveJson.getVersion());
        return toJson(entity, klasseService.extractEscadaKlassenStufen(entity.getId()), tenantProducer.produceTenant());
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

        return toJson(this.schuleService.persist(schule), klasseService.extractEscadaKlassenStufen(schuleId), tenantProducer.produceTenant());
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
