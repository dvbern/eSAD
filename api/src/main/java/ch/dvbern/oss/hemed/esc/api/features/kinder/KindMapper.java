package ch.dvbern.oss.hemed.esc.api.features.kinder;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.KindDuplikatException;
import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.i18n.datetime.DateUtil;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ExternalImportInfoJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.edokid_id.EdokidId;
import ch.dvbern.oss.hemed.esc.api.features.kinder.edokid_id.EdokidIdGeneratorService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.EsadIdJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.KindJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.KindOptionJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.KindSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.KindSaveResultJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.KindSucheOptionsJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.types.KindSaveModeDuplikat;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.SchuleMapper;
import ch.dvbern.oss.hemed.esc.api.features.schule.SchuleService;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.KlasseMapper;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.KlasseService;
import ch.dvbern.oss.hemed.esc.api.features.schule.types.Klassenstufe;
import ch.dvbern.oss.hemed.esc.api.shared.person.AdresseMapper;
import ch.dvbern.oss.hemed.esc.api.shared.person.LandCode;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatusSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.util.KindValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@RequiredArgsConstructor
@Slf4j
public final class KindMapper {

    private final KindService kindService;
    private final KlasseService klasseService;
    private final EdokidIdGeneratorService edokidIdGeneratorService;
    private final SchuleService schuleService;
    private final DossierService dossierService;
    private final AppClock appClock;

    @NonNull
    public static KindJson toJson(
        @NonNull Kind kind,
        @NonNull LocalDate now
    ) {
        Klasse klasse = null;
        Klassenstufe klassenstufe = null;
        if (kind.getKlasse() != null) {
            klasse = kind.getKlasse();
            klassenstufe = kind.getKlasse().getKlassenstufe();
        }
        Schule schuleToSend = kind.extractSchule();
        return new KindJson(
            kind.getId(),
            kind.getEsadId(),
            ExternalImportInfoJson.toJson(kind.getExternalImportInfo()),
            EntityInfoJson.fromEntity(kind),
            new KindSaveJson(
                kind.getSozialversicherungsNummer(),
                kind.getName(),
                kind.getVorname(),
                kind.getGeschlecht(),
                kind.getGeburtsdatum(),
                DateUtil.calculateAlterDuodezimal(kind.getGeburtsdatum(), now),
                kind.getNationalitaet(),
                kind.getErstsprache(),
                kind.getWohntyp(),
                AdresseMapper.toJson(kind.getAdresse()),
                kind.getEmail(),
                kind.getTelefon(),
                schuleToSend != null ? SchuleMapper.toOptionJson(schuleToSend) : null,
                klasse != null ? KlasseMapper.toOptionJson(klasse) : null,
                klassenstufe,
                kind.getKindAktivStatus(),
                kind.getVersion()
            ),
            LandCode.getCode(kind.getNationalitaet())
        );
    }

    @NonNull
    public static KindOptionJson toOptionJson(@NonNull Kind kind) {
        return new KindOptionJson(
            kind.getId(),
            kind.getName(),
            kind.getVorname()
        );
    }

    /**
     * creates a result object that contains either the stored Kind or the list of found duplicates. In that case
     * the Kind will not have been saved. Only one of kind or kindDuplikatIds will be set
     */
    @NonNull
    public static KindSaveResultJson toSaveResultJson(
        @Nullable Kind kind,
        @Nullable List<ID<Kind>> kindDuplikatIds,
        @NonNull LocalDate now
    ) {
        if (kind != null) {
            return new KindSaveResultJson(null, toJson(kind, now));
        }
        return new KindSaveResultJson(kindDuplikatIds, null);
    }

    @NonNull
    private Kind toEntityForCreate(@NonNull KindSaveJson saveJson) {
        Klasse klasse = saveJson.getKlasse() != null ? klasseService.getById(saveJson.getKlasse().getId()) : null;
        Schule schule = saveJson.getSchule() != null ? schuleService.getById(saveJson.getSchule().getId()) : null;
        return new Kind(
            null, // Null because we are creating a new entity from our System, not importing
            null,
            saveJson.getSozialversicherungsNummer(),
            saveJson.getName(),
            saveJson.getVorname(),
            "TODO", // will be set in AbstractEntityListener
            "TODO", // will be set in AbstractEntityListener
            saveJson.getGeschlecht(),
            saveJson.getGeburtsdatum(),
            saveJson.getNationalitaet(),
            saveJson.getErstsprache(),
            saveJson.getWohntyp(),
            AdresseMapper.toEntity(saveJson.getAdresse()),
            saveJson.getEmail(),
            saveJson.getTelefon(),
            mapSchuleFromKlasseOrSchule(klasse, schule),
            klasse,
            saveJson.getKindAktivStatus()
        );
    }

    @Nullable
    private static Schule mapSchuleFromKlasseOrSchule(@Nullable Klasse klasse, @Nullable Schule schule) {
        if (klasse != null) {
            if (!Objects.equals(klasse.getSchule(), schule) && schule != null) {
                LOG.warn(
                    "Passed Schule={} does not match Schule in Klasse={}, will use Schule from Klasse",
                    schule,
                    klasse.getSchule());
            }
            return klasse.getSchule();
        }
        return schule;
    }

    @NonNull
    private Kind toEntity(@NonNull Kind existingKind, @NonNull KindSaveJson saveJson) {
        existingKind.setSozialversicherungsNummer(saveJson.getSozialversicherungsNummer());
        existingKind.setVorname(saveJson.getVorname());
        existingKind.setName(saveJson.getName());
        existingKind.setGeschlecht(saveJson.getGeschlecht());
        existingKind.setGeburtsdatum(saveJson.getGeburtsdatum());
        existingKind.setNationalitaet(saveJson.getNationalitaet());
        existingKind.setErstsprache(saveJson.getErstsprache());
        existingKind.setWohntyp(saveJson.getWohntyp());
        existingKind.setAdresse(AdresseMapper.toEntity(saveJson.getAdresse()));
        existingKind.setEmail(saveJson.getEmail());
        existingKind.setTelefon(saveJson.getTelefon());
        existingKind.setKindAktivStatus(saveJson.getKindAktivStatus());
        // Schule und Klasse werden separat gesetzt, damit die Optimistic Locking Checks nicht fehlschlagen
        return existingKind;
    }

    @NonNull
    public KindJson generateEsadIdForKind(@NonNull ID<Kind> kindId) {
        Kind kind = kindService.getById(kindId);
        KindValidationUtil.throwErrorIfKindIsInactive(kind, AppValidationMessage.cannotEditInactiveKind(kind));
        kind.disableOptimisticLockingCheck();
        Schule schule = kind.extractSchule();
        EdokidId edokidId = edokidIdGeneratorService.generateNextEdokidId(schule);
        kind.setEsadId(edokidId.toFormattedString());
        kind = kindService.persistWithoutDuplicateCheck(kind);
        LocalDate now = appClock.offsetDateTime().now().toLocalDate();
        return toJson(kind, now);
    }

    @NonNull
    public List<KindJson> findAll() {
        LocalDate now = appClock.offsetDateTime().now().toLocalDate();
        return kindService.findAll().stream()
            .map(each -> KindMapper.toJson(each, now))
            .toList();
    }

    @NonNull
    public List<KindJson> findByOptions(@NonNull KindSucheOptionsJson options) {
        // Es muss entweder Schule oder Klasse gesetzt sein
        Validate.isTrue(
            options.getSchuleId() != null || options.getKlasseId() != null,
            "Es muss entweder Schule oder Klasse gesetzt sein");
        List<Kind> result;
        // Wenn Klasse gesetzt ist, dann nach Klasse suchen
        if (options.getKlasseId() != null) {
            result = kindService.findByKlasse(options.getKlasseId());
        } else {
            // Sonst nach schule suchen
            result = kindService.findBySchule(options.getSchuleId());
        }
        LocalDate now = appClock.offsetDateTime().now().toLocalDate();
        return result.stream()
            .map(each -> KindMapper.toJson(each, now))
            .toList();
    }

    @NonNull
    public KindJson getById(@NonNull ID<Kind> kindId) {
        LocalDate now = appClock.offsetDateTime().now().toLocalDate();
        return KindMapper.toJson(kindService.getById(kindId), now);
    }

    @NonNull
    public KindSaveResultJson create(@NonNull KindSaveJson kindJson, @NonNull KindSaveModeDuplikat kindSaveMode) {
        Kind entity = toEntityForCreate(kindJson);
        return saveKind(kindSaveMode, entity, kindJson.getVersion());
    }

    @NonNull
    public KindSaveResultJson update(
        @NonNull ID<Kind> kindID,
        @NonNull KindSaveJson kindJson,
        @NonNull KindSaveModeDuplikat kindSaveMode
    ) {
        Kind existingKind = kindService.getById(kindID);
        KindValidationUtil.throwErrorIfKindIsInactive(existingKind, AppValidationMessage.cannotEditInactiveKind(existingKind));
        Kind entity = toEntity(existingKind, kindJson);
        KindSaveResultJson resultJson = saveKind(kindSaveMode, entity, kindJson.getVersion());
        if (resultJson.getKindJson() == null) {
            // Wenn es ein Duplikat ist, dann wird das KindJson nicht gesetzt. Wir brechen hier ab
            return resultJson;
        }
        updateSchuleAndKlasse(kindJson, entity);
        return saveKind(KindSaveModeDuplikat.NO_CHECK, entity, null);
    }

    public void changeKindAktivStatus(@NonNull ID<Kind> kindId, @NonNull EntityAktivStatusSaveJson saveJson) {
        Kind kind = kindService.getById(kindId);
        kind.disableOptimisticLockingCheck();
        kind.setKindAktivStatus(saveJson.getEntityAktivStatus());
        this.kindService.persistWithoutDuplicateCheck(kind);
    }

    @NonNull
    private KindSaveResultJson saveKind(
        @NonNull KindSaveModeDuplikat kindSaveMode,
        @NonNull Kind entity,
        @Nullable Long versionFromFrontend
    ) {
        LocalDate now = appClock.offsetDateTime().now().toLocalDate();
        if (kindSaveMode == KindSaveModeDuplikat.NO_CHECK) {
            Kind persistedKind = kindService.persistWithoutDuplicateCheck(entity, versionFromFrontend);
            return toSaveResultJson(persistedKind, null, now);
        }
        try {
            Kind savedKind = kindService.persistWithDuplicateCheck(entity, versionFromFrontend, kindSaveMode);
            return toSaveResultJson(savedKind, null, now);
        } catch (KindDuplikatException e) {
            return toSaveResultJson(null, e.getKindDuplikatIds(), now);
        }
    }

    public void remove(@NonNull ID<Kind> kindID) {
        kindService.remove(kindID);
    }

    @NonNull
    public KindJson setEsadIdForKind(@NonNull ID<Kind> kindId, @NonNull EsadIdJson esadIdJson) {
        Kind kind = kindService.getById(kindId);
        KindValidationUtil.throwErrorIfKindIsInactive(
            kind,
            AppValidationMessage.cannotEditInactiveKind(kind)
        );
        kind.disableOptimisticLockingCheck();
        kind.setEsadId(esadIdJson.getEsadId());
        LocalDate now = appClock.offsetDateTime().now().toLocalDate();
        return toJson(kindService.persistWithoutDuplicateCheck(kind), now);
    }

    private void updateSchuleAndKlasse(@NonNull KindSaveJson kindJson, @NonNull Kind entity) {
        entity.disableOptimisticLockingCheck();
        Klasse newklasse = kindJson.getKlasse() != null ? klasseService.getById(kindJson.getKlasse().getId()) : null;
        entity.setKlasse(newklasse);
        Schule newSchule = kindJson.getSchule() != null ? schuleService.getById(kindJson.getSchule().getId()) : null;
        Schule newSchuleToSet = mapSchuleFromKlasseOrSchule(newklasse, newSchule);
        resetArztMpaIfSchuleChanged(entity.getId(), entity.getSchule(), newSchuleToSet);
        entity.setSchule(newSchuleToSet);
    }

    private void resetArztMpaIfSchuleChanged(
        @NonNull ID<Kind> kindID,
        @Nullable Schule oldSchule,
        @Nullable Schule newSchule
    ) {
        if (newSchule == null) {
            return;
        }
        Optional<Dossier> dossierOptional = dossierService.findByKind(kindID);
        if ((!newSchule.equals(oldSchule)) && dossierOptional.isPresent()) {
            dossierOptional.get().setZustaendigMPA(null);
            dossierOptional.get().setZustaendigArzt(null);
        }
    }
}
