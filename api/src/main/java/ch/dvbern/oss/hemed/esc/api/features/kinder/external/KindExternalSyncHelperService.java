package ch.dvbern.oss.hemed.esc.api.features.kinder.external;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.dvbstarter.shared.types.sozialversicherungsnummer.SozialversicherungsNummer;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ExternalImportInfo;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ExternalImportInfoJson;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ImportTyp;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.DossierService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindService;
import ch.dvbern.oss.hemed.esc.api.features.schule.DummySchulenIds;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.KlasseService;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.ZustaendigePersonService;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.externalsystem.ExternalSystemIdDTO;
import ch.dvbern.oss.hemed.esc.api.shared.externalsystem.ItemSyncResultJson;
import ch.dvbern.oss.hemed.esc.api.shared.person.Adresse;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.shared.person.Land;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import ch.dvbern.oss.hemed.esc.api.shared.util.HashValueGeneratorUtil;
import ch.dvbern.oss.hemed.esc.api.shared.util.VerantwortlicherSchuleChangedDTO;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus.isInaktiv;

@Service
@Slf4j
@RequiredArgsConstructor
public class KindExternalSyncHelperService {

    private final KindExternalRepo kindExternalRepo;
    private final KindService kindService;
    private final KlasseService klasseService;
    private final DossierService dossierService;
    private final AppClock appClock;
    private final ConfigService configService;
    private final ZustaendigePersonService zustaendigePersonService;

    @NonNull
    public static Kind toEntityForCreate(
        @NonNull KindExternal kindExternal,
        @NonNull Klasse klasseToConnect,
        @NonNull OffsetDateTime now,
        @NonNull ImportTyp importTyp
    ) {
        String name = Objects.toString(kindExternal.getSchuelerName(), "N/A");
        String vorname = Objects.toString(kindExternal.getSchuelerVorname(), "N/A");
        Geschlecht geschlecht = mapGeschlechtForSchueler(kindExternal.getSchuelerGeschlecht());
        LocalDate geburtsdatum = mapGeburtsdatum(kindExternal.getSchuelerGeburtsdatum(), importTyp);
        Adresse schuelerAdresse = extractAdresse(kindExternal);
        return new Kind(
            new ExternalImportInfo(
                kindExternal.getSchuelerId(),
                true,
                now,
                now,
                importTyp
            ),
            null,
            "SetInListener", // will be set in AbstractEntityListener
            mapValidSvnrOrNull(kindExternal, importTyp),
            name,
            vorname,
            "TODO", // will be set in AbstractEntityListener
            "TODO", // will be set in AbstractEntityListener
            geschlecht,
            geburtsdatum,
            mapNationalitaetNameToCode(kindExternal.getSchuelerStaatcodeISO()),
            kindExternal.getSchuelerErstsprache(),
            kindExternal.getWohntyp(),
            schuelerAdresse,
            mapValidEmailAdressOrNull(kindExternal, importTyp),
            mapValidPhoneNumberOrNull(kindExternal, importTyp),
            klasseToConnect.getSchule(),
            klasseToConnect,
            EntityAktivStatus.AKTIV
        );
    }

    public static @Nullable SozialversicherungsNummer mapValidSvnrOrNull(
        @NonNull KindExternal kindExternal,
        @NonNull ImportTyp importTyp
    ) {
        if (StringUtils.isEmpty(kindExternal.getSchuelerSozialversicherung())) {
            return null;
        }
        try {
            return new SozialversicherungsNummer(kindExternal.getSchuelerSozialversicherung());
        } catch (Exception e) {
            LOG.debug(
                "{}: Invalid Sozialversicherungsnummer '{}' for Kind with externalId: '{}'",
                importTyp, kindExternal.getSchuelerSozialversicherung(), kindExternal.getExternalId());
            return null;
        }
    }

    @Nullable
    public static EmailAddress mapValidEmailAdressOrNull(
        @NonNull KindExternal kindExternal,
        @NonNull ImportTyp importTyp
    ) {
        if (StringUtils.isEmpty(kindExternal.getSchuelerEMail())) {
            return null;
        }
        try {
            return EmailAddress.fromString(kindExternal.getSchuelerEMail());
        } catch (Exception e) {
            LOG.debug(
                "{}: Invalid email address '{}' for Kind with externalId: '{}'",
                importTyp,
                kindExternal.getSchuelerEMail(),
                kindExternal.getExternalId()
            );
            return null;
        }
    }

    @Nullable
    public static NormalizedPhoneNumber mapValidPhoneNumberOrNull(
        @NonNull KindExternal kindExternal,
        @NonNull ImportTyp importTyp
    ) {
        if (StringUtils.isEmpty(kindExternal.getSchuelerMobile())) {
            return null;
        }
        try {
            return new NormalizedPhoneNumber(kindExternal.getSchuelerMobile());
        } catch (Exception e) {
            LOG.debug(
                "{}: Invalid phone number '{}' for Kind with externalId: '{}'",
                importTyp,
                kindExternal.getSchuelerMobile(),
                kindExternal.getExternalId()
            );
            return null;
        }
    }

    @NonNull
    public static Adresse extractAdresse(@NonNull KindExternal kindExternal) {
        String adresseZusatz = null;
        adresseZusatz =
            StringUtils.joinWith(" ", kindExternal.getSchuelerAdresse1(), kindExternal.getSchuelerAdresse2());
        adresseZusatz = StringUtils.trimToNull(adresseZusatz);

        return new Adresse(
            kindExternal.getSchuelerAdresse(),
            adresseZusatz,
            kindExternal.getSchuelerPLZ(),
            kindExternal.getSchuelerOrt(),
            Land.SCHWEIZ
        );
    }

    @Nullable
    public static Land mapNationalitaetNameToCode(@Nullable String schuelerStaatcodeISO) {
        if (schuelerStaatcodeISO == null) {
            return null;
        }
        return Land.getByEscadaIsoCode(schuelerStaatcodeISO);
    }

    @NonNull
    public static LocalDate mapGeburtsdatum(@Nullable String schuelerGeburtsdatum, @NonNull ImportTyp importTyp) {
        // geburtsdatum is passed like 2015-05-03T00:00:00
        if (schuelerGeburtsdatum == null) {
            throw new IllegalArgumentException("Geburtsdatum is null");
        }

        // pupil uses a localized date format
        if (importTyp == ImportTyp.PUPIL) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            return LocalDate.parse(schuelerGeburtsdatum, formatter);
        }

        // use iso date to parse the date
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime localDateTime = LocalDateTime.parse(schuelerGeburtsdatum, formatter);
        return localDateTime.toLocalDate();
    }

    @NonNull
    public static Geschlecht mapGeschlechtForSchueler(@Nullable String schuelerGeschlecht) {
        if ("1".equals(schuelerGeschlecht)) {
            return Geschlecht.MAENNLICH;
        }
        if ("2".equals(schuelerGeschlecht)) {
            return Geschlecht.WEIBLICH;
        }
        return Geschlecht.UNBEKANNT;

    }

    @NonNull
    public static KindExternal toEntityKindExternal(
        @NonNull KindExternal existingKindExternal,
        @NonNull KindExternal remoteKindExternal
    ) {
        existingKindExternal.setWohntyp(remoteKindExternal.getWohntyp());
        existingKindExternal.setSchuelerName(remoteKindExternal.getSchuelerName());
        existingKindExternal.setSchuelerVorname(remoteKindExternal.getSchuelerVorname());
        existingKindExternal.setSchuelerGeburtsdatum(remoteKindExternal.getSchuelerGeburtsdatum());
        existingKindExternal.setSchuelerGeschlecht(remoteKindExternal.getSchuelerGeschlecht());
        existingKindExternal.setSchuelerSozialversicherung(remoteKindExternal.getSchuelerSozialversicherung());
        existingKindExternal.setSchuelerAdresse(remoteKindExternal.getSchuelerAdresse());
        existingKindExternal.setSchuelerAdresse1(remoteKindExternal.getSchuelerAdresse1());
        existingKindExternal.setSchuelerAdresse2(remoteKindExternal.getSchuelerAdresse2());
        existingKindExternal.setSchuelerPLZ(remoteKindExternal.getSchuelerPLZ());
        existingKindExternal.setSchuelerOrt(remoteKindExternal.getSchuelerOrt());
        existingKindExternal.setSchuelerMobile(remoteKindExternal.getSchuelerMobile());
        existingKindExternal.setSchuelerEMail(remoteKindExternal.getSchuelerEMail());
        existingKindExternal.setSchuelerNationalitaet(remoteKindExternal.getSchuelerNationalitaet());
        existingKindExternal.setSchuelerStaatcodeISO(remoteKindExternal.getSchuelerStaatcodeISO());
        existingKindExternal.setSchuelerSprache(remoteKindExternal.getSchuelerSprache());
        existingKindExternal.setSchuelerErstsprache(remoteKindExternal.getSchuelerErstsprache());
        existingKindExternal.setSchulhausId(remoteKindExternal.getSchulhausId());
        existingKindExternal.setEscadaKlasseId(remoteKindExternal.getEscadaKlasseId());
        return existingKindExternal;
    }

    @NonNull
    public static Kind updateKindBasedOnKindExternal(
        @NonNull Kind kind,
        @NonNull KindExternal remoteKindExternal,
        @NonNull KindExternal existingKindExternal,
        @NonNull OffsetDateTime now,
        @NonNull ImportTyp importTyp
    ) {
        // only update the fields that were not changed locally already
        kind.setSozialversicherungsNummer(determineValueToStore(
            kind.getSozialversicherungsNummer(),
            mapValidSvnrOrNull(remoteKindExternal, importTyp),
            mapValidSvnrOrNull(existingKindExternal, importTyp),
            importTyp)
        );

        kind.setName(
            Objects.toString(
                determineValueToStore(
                    kind.getName(),
                    remoteKindExternal.getSchuelerName(),
                    existingKindExternal.getSchuelerName(),
                    importTyp
                ), "N/A")
        );

        kind.setVorname(
            Objects.toString(
                determineValueToStore(
                    kind.getVorname(),
                    remoteKindExternal.getSchuelerVorname(),
                    existingKindExternal.getSchuelerVorname(),
                    importTyp
                ), "N/A")
        );

        kind.setGeschlecht(
            Objects.requireNonNull(
                determineValueToStore(
                    kind.getGeschlecht(),
                    mapGeschlechtForSchueler(remoteKindExternal.getSchuelerGeschlecht()),
                    mapGeschlechtForSchueler(existingKindExternal.getSchuelerGeschlecht()),
                    importTyp))
        );

        kind.setGeburtsdatum(
            Objects.requireNonNull(determineValueToStore(
                kind.getGeburtsdatum(),
                mapGeburtsdatum(remoteKindExternal.getSchuelerGeburtsdatum(), importTyp),
                mapGeburtsdatum(existingKindExternal.getSchuelerGeburtsdatum(), importTyp),
                importTyp))
        );

        kind.setNationalitaet(determineValueToStore(
            kind.getNationalitaet(),
            mapNationalitaetNameToCode(remoteKindExternal.getSchuelerStaatcodeISO()),
            mapNationalitaetNameToCode(existingKindExternal.getSchuelerStaatcodeISO()),
            importTyp)
        );

        kind.setErstsprache(determineValueToStore(
            kind.getErstsprache(),
            remoteKindExternal.getSchuelerErstsprache(),
            existingKindExternal.getSchuelerErstsprache(),
            importTyp)
        );

        kind.setWohntyp(determineValueToStore(
            kind.getWohntyp(),
            remoteKindExternal.getWohntyp(),
            existingKindExternal.getWohntyp(),
            importTyp)
        );

        kind.setAdresse(determineAdresseUpdate(
                kind.getAdresse(),
                remoteKindExternal,
                existingKindExternal,
                importTyp
            )
        );

        kind.setEmail(determineValueToStore(
            kind.getEmail(),
            mapValidEmailAdressOrNull(remoteKindExternal, importTyp),
            mapValidEmailAdressOrNull(existingKindExternal, importTyp),
            importTyp)
        );

        kind.setTelefon(determineValueToStore(
            kind.getTelefon(),
            mapValidPhoneNumberOrNull(remoteKindExternal, importTyp),
            mapValidPhoneNumberOrNull(existingKindExternal, importTyp),
            importTyp)
        );

        kind.setExternalImportInfo(ExternalImportInfoJson.toEntityOrCreate(
            kind.getExternalImportInfo(),
            remoteKindExternal.getExternalId(),
            now,
            importTyp
        ));

        return kind;
    }

    public static Adresse determineAdresseUpdate(
        @Nullable Adresse adresesInDb,
        @NonNull KindExternal remoteKindExternal,
        @NonNull KindExternal existingKindExternal,
        @NonNull ImportTyp importTyp
    ) {
        Adresse remoteAdresseExternal = extractAdresse(remoteKindExternal);
        Adresse existingAdresseExternal = extractAdresse(existingKindExternal);

        if (adresesInDb == null) {
            LOG.info("{}: Adresse not found in DB, updating with remote address", importTyp);
            return remoteAdresseExternal;
        }

        adresesInDb.setStrasseUndNr(
            determineValueToStore(
                adresesInDb.getStrasseUndNr(),
                remoteAdresseExternal.getStrasseUndNr(),
                existingAdresseExternal.getStrasseUndNr(),
                importTyp
            )
        );

        adresesInDb.setAdresseZusatz(
            determineValueToStore(
                adresesInDb.getAdresseZusatz(),
                remoteAdresseExternal.getAdresseZusatz(),
                existingAdresseExternal.getAdresseZusatz(),
                importTyp
            )
        );

        adresesInDb.setPlz(
            determineValueToStore(
                adresesInDb.getPlz(),
                remoteAdresseExternal.getPlz(),
                existingAdresseExternal.getPlz(),
                importTyp
            )
        );

        adresesInDb.setOrt(
            determineValueToStore(
                adresesInDb.getOrt(),
                remoteAdresseExternal.getOrt(),
                existingAdresseExternal.getOrt(),
                importTyp
            )
        );

        adresesInDb.setLand(
            determineValueToStore(
                adresesInDb.getLand(),
                remoteAdresseExternal.getLand(),
                existingAdresseExternal.getLand(),
                importTyp
            )
        );

        return adresesInDb;
    }

    /**
     * Falls bei Import...
     * ... EDOKID Feld leer & Escada Feld neu --> EDOKID Feld mit Escada überschreiben
     * ... EDOKID Feld gleich ist wie Escada & Escada neu geändert hat --> EDOKID Feld mit Escada überschreiben
     * ... EDOKID Feld anders als Escada Feld --> Escada nicht übernehmen resp. EDOKID Feld nicht anpassen
     */
    @Nullable
    private static <T> T determineValueToStore(
        @Nullable T currentEntityValue,
        @Nullable T remoteExternalUpdateValue,
        @Nullable T existingExternalValue,
        @NonNull ImportTyp importTyp
    ) {

        if (currentEntityValue == null
            || (currentEntityValue instanceof String s && StringUtils.isEmpty(s))) {
            return remoteExternalUpdateValue;
        }
        // If current value is equal to the initial escada value, update it
        if (Objects.equals(currentEntityValue, existingExternalValue)) {
            LOG.trace(
                "{}: Field updated, local value: '{}', remote value: '{}'",
                importTyp,
                existingExternalValue,
                remoteExternalUpdateValue);
            return remoteExternalUpdateValue;
        }
        LOG.debug(
            "{}: Field not updated, current value {} was different than escada value: '{}', remote value: '{}'",
            importTyp,
            currentEntityValue,
            existingExternalValue,
            remoteExternalUpdateValue);
        return currentEntityValue;
    }

    @Transactional(TxType.REQUIRES_NEW)
    @NonNull
    public ItemSyncResultJson<Kind> updateExistingKind(
        @NonNull ExternalSystemIdDTO idDTO,
        @NonNull KindExternal remoteKindExternal,
        @NonNull ImportTyp importTyp
    ) {
        Kind existingKind = kindService.getById(ID.of(idDTO.getInternalId(), Kind.class));
        KindExternal existingkindExternal = kindExternalRepo.getExternalKindForKind(existingKind.getId());

        Validate.isTrue(
            Objects.requireNonNull(existingkindExternal).getExternalId().equals(idDTO.getExternalId()),
            "ExternalId mismatch, expected: '%s', actual: '%s' for existingKind",
            idDTO.getExternalId(),
            existingkindExternal.getExternalId(),
            existingKind.getId());

        Kind updatedKind = updateKindBasedOnKindExternal(
            existingKind,
            remoteKindExternal,
            existingkindExternal,
            appClock.offsetDateTime().now(),
            importTyp
        );
        updatedKind.disableOptimisticLockingCheck();

        KindExternal updatedKindExt = toEntityKindExternal(existingkindExternal, remoteKindExternal);
        ensureNoManualKindWithSameVnrExisting(updatedKindExt, existingkindExternal.getExternalId(), importTyp);
        kindExternalRepo.persist(updatedKindExt);

        String escadaKlasseId =
            Objects.requireNonNull(updatedKindExt.getEscadaKlasseId(), "KlasseId from escada must be set");

        // update klasse if it has changed
        Klasse klasseToConnect = klasseService.getByExternalId(escadaKlasseId);
        Optional<Dossier> dossier = dossierService.findByKind(updatedKind.getId());
        updateSchuleAndKlasse(updatedKind, klasseToConnect, importTyp, dossier.orElse(null));
        ensureKindAktiv(updatedKind, importTyp);

        updatedKind = kindService.persistWithoutDuplicateCheck(updatedKind);

        return ItemSyncResultJson.createUpdateOkItem(
            idDTO,
            Kind.class,
            updatedKind.getFullNameIfAllowed(configService.isExternalImportRedactPersonalInfo()));
    }

    @NonNull
    @Transactional(TxType.REQUIRES_NEW)
    public ItemSyncResultJson<Kind> addNewKind(@NonNull KindExternal kindExternalToAdd, @NonNull ImportTyp importTyp) {

        String escadaKlasseId = Objects.requireNonNull(
            kindExternalToAdd.getEscadaKlasseId(),
            "KlasseId must be set for Kind with externalId: " + kindExternalToAdd.getExternalId());

        Klasse klasseToConnect = klasseService.getByExternalId(escadaKlasseId);
        // see ESC-577, the Kind could have existed in escada a long time ago and might reappear now as new bcs we
        // have no external obj
        Optional<Kind> preexistingKindOpt = findExistingKindWithSameId(kindExternalToAdd);
        ensureNoManualKindWithSameVnrExisting(
            kindExternalToAdd,
            preexistingKindOpt.map(Kind::getExternalSystemIdOrNull).orElse(null),
            importTyp);

        Kind internalKind;
        // normally we would only update the Kind if the values were unchanged versus the 'old' values, here we will
        // NEVER update them
        internalKind =
            preexistingKindOpt
                .map(kind -> {
                    var kindUpdated = updateKindBasedOnKindExternal(
                        kind,
                        kindExternalToAdd,
                        kindExternalToAdd,
                        appClock.offsetDateTime().now(),
                        importTyp
                    );
                    kindUpdated.disableOptimisticLockingCheck();
                    Optional<Dossier> dossier = dossierService.findByKind(kindUpdated.getId());
                    updateSchuleAndKlasse(kindUpdated, klasseToConnect, importTyp, dossier.orElse(null));
                    ensureKindAktiv(kindUpdated, importTyp);
                    return kindUpdated;
                })
                .orElseGet(() -> toEntityForCreate(
                    kindExternalToAdd,
                    klasseToConnect,
                    appClock.offsetDateTime().now(),
                    importTyp
                ));

        internalKind.disableOptimisticLockingCheck();
        internalKind = kindService.persistWithoutDuplicateCheck(internalKind);
        kindExternalToAdd.setKind(internalKind);
        kindExternalRepo.persist(kindExternalToAdd);

        var result = ItemSyncResultJson.createAddOkItem(
            kindExternalToAdd,
            internalKind.getId(),
            internalKind.getFullNameIfAllowed(configService.isExternalImportRedactPersonalInfo())
        );
        LOG.info(
            "{}: Kind {} added{}, internalId: '{}', externalId: '{}'",
            importTyp,
            result.getFriendlyName(),
            preexistingKindOpt.isEmpty() ? "" : " (preexisting)",
            result.getInternalId(),
            result.getExternalId()
        );
        return result;
    }

    private void updateSchuleAndKlasse(
        @NonNull Kind updatedKind,
        @NonNull Klasse klasseToConnect,
        @NonNull ImportTyp importTyp,
        @Nullable Dossier dossierOrNull
    ) {
        if (!Objects.equals(klasseToConnect, updatedKind.getKlasse())) {
            LOG.info(
                "{}: Klasse of Kind {} has changed, updating Klasse from {} to {}",
                importTyp,
                updatedKind.getExternalSystemIdOrNull(), updatedKind.getKlasse(), klasseToConnect);
            Schule oldSchule = updatedKind.getSchule();
            updatedKind.setKlasse(klasseToConnect); // klasse might have changed
            updatedKind.setSchule(klasseToConnect.getSchule());
            if (dossierOrNull != null) {
                // if the kind had a dossier and the schule/klasse changes we have to reset the Zustaendigkeit
                // https://intra.dvbern.ch/spaces/HEM/pages/284263663/Zust%C3%A4ndigkeiten+Archivierung
                // Change Zustaendigkeiten: Fall 6, 8: Neue Klasse/Schule auf Kind (externe Schnittstelle)
                VerantwortlicherSchuleChangedDTO args =
                    VerantwortlicherSchuleChangedDTO.of(oldSchule, klasseToConnect.getSchule());
                zustaendigePersonService.schuleChangedOnDossier(dossierOrNull, args);
            }
        }
    }

    private static void ensureKindAktiv(@NonNull Kind updatedKind, @NonNull ImportTyp importTyp) {
        if (isInaktiv(updatedKind.getKindAktivStatus())) {
            LOG.info(
                "{}: Kind with UUID {} and ExternalID {} was INAKTIV, resetting to AKTIV",
                importTyp,
                updatedKind.getId().getUUID(),
                updatedKind.getExternalSystemIdOrNull());
            updatedKind.setKindAktivStatus(EntityAktivStatus.AKTIV);
        }
    }

    private void ensureNoManualKindWithSameVnrExisting(
        @NonNull KindExternal kindExternalToAdd,
        @Nullable @IgnoreForIdClassTest String externalSchuelerId,
        @NonNull ImportTyp importTyp
    ) {
        SozialversicherungsNummer vnr = mapValidSvnrOrNull(kindExternalToAdd, importTyp);
        if (vnr != null) {
            kindService.ensureNoManualKindWithSameVnrExisting(vnr, externalSchuelerId);
        }
    }

    @NonNull
    private Optional<Kind> findExistingKindWithSameId(@NonNull KindExternal kindExternalToAdd) {
        return kindService.findByExternalId(kindExternalToAdd.getSchuelerId());
    }

    @NonNull
    @Transactional(TxType.REQUIRES_NEW)
    public ItemSyncResultJson<Kind> deactivateRemovedKind(
        @NonNull ExternalSystemIdDTO externalSystemIdDTO,
        @NonNull ImportTyp importTyp) {

        Kind kind = kindService.getById(ID.of(externalSystemIdDTO.getInternalId(), Kind.class));
        KindExternal kindExternalToDelete = kindExternalRepo.getExternalKindForKind(kind.getId());

        Validate.isTrue(
            Objects.requireNonNull(kindExternalToDelete).getExternalId()
                .equals(externalSystemIdDTO.getExternalId()),
            "ExternalId mismatch, expected: '%s', actual: '%s' for kind",
            externalSystemIdDTO.getExternalId(),
            kindExternalToDelete.getExternalId(),
            kind.getId());

        if (kind.getExternalImportInfo() != null && kind.isAktivInExternalSystem()) {
            kind.getExternalImportInfo().setAktivInExternalSystem(false);
            // If the Kind to deactivate has an Escada-Schule/Klasse we remove it
            if (kind.getKlasse() != null && kind.getKlasse().getExternalSystemIdOrNull() != null) {
                kind.setKlasse(null);
            }
            if (kind.getSchule() != null
                && (
                kind.getSchule().getExternalSystemIdOrNull() != null
                    || DummySchulenIds.isDummySchule(kind.getSchule().getId())
            )
            ) {
                // https://intra.dvbern.ch/spaces/HEM/pages/284263663/Zust%C3%A4ndigkeiten+Archivierung
                // Kind wird ueber Escada inaktiviert und Schule entfernt -> wir archivieren nichts
                kind.setSchule(null);
            }
            kind.disableOptimisticLockingCheck();
        }

        kindExternalRepo.remove(kindExternalToDelete.getId());

        var result = ItemSyncResultJson.createDeactivateOkItem(
            externalSystemIdDTO,
            Kind.class,
            kind.getFullNameIfAllowed(configService.isExternalImportRedactPersonalInfo())
        );
        LOG.info(
            "{}: Kind {} deactivated, internalId: '{}', externalId: '{}'",
            importTyp,
            result.getFriendlyName(),
            result.getInternalId(),
            result.getExternalId()
        );
        return result;
    }
}
