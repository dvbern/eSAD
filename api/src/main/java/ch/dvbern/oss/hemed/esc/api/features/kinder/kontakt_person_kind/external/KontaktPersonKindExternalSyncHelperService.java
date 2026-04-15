package ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.external;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ExternalImportInfo;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ExternalImportInfoJson;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ImportTyp;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKindService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.types.KontaktPersonKindBeziehung;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.externalsystem.ExternalSystemIdDTO;
import ch.dvbern.oss.hemed.esc.api.shared.externalsystem.ItemSyncResultJson;
import ch.dvbern.oss.hemed.esc.api.shared.person.Adresse;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.shared.person.Land;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Service
@Slf4j
@RequiredArgsConstructor
public class KontaktPersonKindExternalSyncHelperService {

    private final KontaktPersonKindExternalRepo kontaktPersonKindExternalRepo;
    private final KontaktPersonKindService kontaktPersonKindService;
    private final KindService kindService;
    private final AppClock appClock;
    private final ConfigService configService;

    @NonNull
    public static KontaktPersonKind toEntityForCreate(
        @NonNull Set<Kind> kinder,
        @NonNull KontaktPersonKindExternal kontaktPersonKindExternal,
        @NonNull OffsetDateTime now,
        @NonNull ImportTyp importTyp
    ) {
        Geschlecht geschlecht = getGeschlecht(kontaktPersonKindExternal.getAdressierungAnrede());
        return new KontaktPersonKind(
            ExternalImportInfoJson.toEntityOrCreate(
                null,
                kontaktPersonKindExternal.getExternalId(),
                now,
                importTyp
            ),
            kinder,
            Objects.toString(kontaktPersonKindExternal.getAdressierungName(), "N/A"),
            Objects.toString(kontaktPersonKindExternal.adressierungVorname, "N/A"),
            geschlecht,
            importTyp == ImportTyp.PUPIL ?
                mapBeziehungPupil(kontaktPersonKindExternal.getAdressierungBeziehung()) :
                mapBeziehungEscada(geschlecht),
            extractAdresse(kontaktPersonKindExternal),
            mapValidEmailAdressOrNull(kontaktPersonKindExternal.getAdressierungEmail(), importTyp),
            mapFirstValidPhoneNumberOrNull(
                kontaktPersonKindExternal.getAdressierungNatel(),
                kontaktPersonKindExternal.getAdressierungTelefonPrivat(),
                kontaktPersonKindExternal.getAdressierungTelefonGeschaeft(),
                importTyp),
            false
        );
    }

    private static @NonNull Adresse extractAdresse(@NonNull KontaktPersonKindExternal kontaktPersonKindExternal) {
        return new Adresse(
            kontaktPersonKindExternal.getAdressierungStrasse()
                + ' '
                + kontaktPersonKindExternal.getAdressierungHausnummer(),
            null,
            kontaktPersonKindExternal.getAdressierungPlz(),
            kontaktPersonKindExternal.getAdressierungOrt(),
            Land.SCHWEIZ);

    }

    private KontaktPersonKindExternal toEntityKontaktPersonKindExternal(
        @NonNull KontaktPersonKindExternal existingKontaktPersonKindExternal,
        @NonNull KontaktPersonKindExternal remoteKontaktPersonKindExternal
    ) {
        existingKontaktPersonKindExternal.setAdressierungAnrede(remoteKontaktPersonKindExternal.getAdressierungAnrede());
        existingKontaktPersonKindExternal.setAdressierungName(remoteKontaktPersonKindExternal.getAdressierungName());
        existingKontaktPersonKindExternal.setAdressierungVorname(remoteKontaktPersonKindExternal.getAdressierungVorname());
        existingKontaktPersonKindExternal.setAdressierungStrasse(remoteKontaktPersonKindExternal.getAdressierungStrasse());
        existingKontaktPersonKindExternal.setAdressierungHausnummer(remoteKontaktPersonKindExternal.getAdressierungHausnummer());
        existingKontaktPersonKindExternal.setAdressierungPlz(remoteKontaktPersonKindExternal.getAdressierungPlz());
        existingKontaktPersonKindExternal.setAdressierungOrt(remoteKontaktPersonKindExternal.getAdressierungOrt());
        existingKontaktPersonKindExternal.setAdressierungTelefonPrivat(remoteKontaktPersonKindExternal.getAdressierungTelefonPrivat());
        existingKontaktPersonKindExternal.setAdressierungTelefonGeschaeft(remoteKontaktPersonKindExternal.getAdressierungTelefonGeschaeft());
        existingKontaktPersonKindExternal.setAdressierungNatel(remoteKontaktPersonKindExternal.getAdressierungNatel());
        existingKontaktPersonKindExternal.setAdressierungEmail(remoteKontaktPersonKindExternal.getAdressierungEmail());
        existingKontaktPersonKindExternal.setSchuelerIds(remoteKontaktPersonKindExternal.getSchuelerIds());
        return existingKontaktPersonKindExternal;
    }

    private boolean isPupil(@Nullable ExternalImportInfo externalImportInfo) {
        return externalImportInfo != null && externalImportInfo.getImportTyp() == ImportTyp.PUPIL;
    }

    @NonNull
    private KontaktPersonKind updateKontaktPersonKindBasedOnExternal(
        @NonNull KontaktPersonKind kontaktPersonKind,
        @NonNull KontaktPersonKindExternal updatedExternal,
        @NonNull OffsetDateTime now,
        @NonNull ImportTyp importTyp
    ) {
        boolean isPupil = isPupil(kontaktPersonKind.getExternalImportInfo());

        kontaktPersonKind.setName(Objects.toString(updatedExternal.getAdressierungName(), "N/A"));
        kontaktPersonKind.setVorname(Objects.toString(updatedExternal.getAdressierungVorname(), "N/A"));
        kontaktPersonKind.setGeschlecht(getGeschlecht(updatedExternal.getAdressierungAnrede()));
        kontaktPersonKind.setBeziehung(isPupil ?
            mapBeziehungPupil(updatedExternal.getAdressierungBeziehung()) :
            mapBeziehungEscada(kontaktPersonKind.getGeschlecht()));
        kontaktPersonKind.setAdresse(extractAdresse(updatedExternal));
        kontaktPersonKind.setEmail(mapValidEmailAdressOrNull(updatedExternal.getAdressierungEmail(), importTyp));
        kontaktPersonKind.setTelefon(mapFirstValidPhoneNumberOrNull(
            updatedExternal.getAdressierungNatel(),
            updatedExternal.getAdressierungTelefonPrivat(),
            updatedExternal.getAdressierungTelefonGeschaeft(),
            importTyp));

        kontaktPersonKind.setExternalImportInfo(ExternalImportInfoJson.toEntityOrCreate(
            kontaktPersonKind.getExternalImportInfo(),
            updatedExternal.getExternalId(),
            now,
            importTyp
        ));

        return kontaktPersonKind;
    }

    @NonNull
    @Transactional(TxType.REQUIRES_NEW)
    public ItemSyncResultJson<KontaktPersonKind> addNewKontaktPerson(
        @NonNull KontaktPersonKindExternal kontaktPersonKindExternalToAdd,
        @NonNull ImportTyp importTyp
    ) {
        // All parents must have at least one kind if ocming from external system
        Set<Kind> kinder = loadLinkedKinder(kontaktPersonKindExternalToAdd);
        Optional<KontaktPersonKind> preexistingKontaktpersonKindOpt =
            findPreexistingLehrperson(kontaktPersonKindExternalToAdd);

        // save the new parent
        KontaktPersonKind internalKpKindToStore = preexistingKontaktpersonKindOpt
            .map(existingKPKind -> updateKontaktPersonKindBasedOnExternal(
                existingKPKind,
                kontaktPersonKindExternalToAdd,
                appClock.offsetDateTime().now(),
                importTyp
            ))
            .orElseGet(() -> toEntityForCreate(
                kinder,
                kontaktPersonKindExternalToAdd,
                appClock.offsetDateTime().now(),
                importTyp
            ));
        internalKpKindToStore = kontaktPersonKindService.persist(internalKpKindToStore);

        // save KontaktPersonKind
        kontaktPersonKindExternalToAdd.setKontaktPersonKind(internalKpKindToStore);
        kontaktPersonKindExternalRepo.persist(kontaktPersonKindExternalToAdd);

        var result = ItemSyncResultJson.createAddOkItem(
            kontaktPersonKindExternalToAdd,
            internalKpKindToStore.getId(),
            internalKpKindToStore.getFullNameIfAllowed(configService.isExternalImportRedactPersonalInfo())
        );
        LOG.info(
            "{}: KontaktPersonKind {} added{}, internalId: '{}', externalId: '{}'",
            importTyp,
            result.getFriendlyName(),
            preexistingKontaktpersonKindOpt.isEmpty() ? "" : " (preexisting)",
            result.getInternalId(),
            result.getExternalId()
        );
        return result;
    }

    @NonNull
    private Optional<KontaktPersonKind> findPreexistingLehrperson(
        @NonNull KontaktPersonKindExternal kontaktPersonKindExternalToAdd
    ) {
        return kontaktPersonKindService.findByExternalId(kontaktPersonKindExternalToAdd.getExternalId());
    }

    @NonNull
    private Set<Kind> loadLinkedKinder(@NonNull KontaktPersonKindExternal kontaktPersonKindExternalToAdd) {
        String[] externalKindIds = StringUtils.split(kontaktPersonKindExternalToAdd.getSchuelerIds(), ",");
        Validate.isTrue(
            externalKindIds != null && externalKindIds.length > 0,
            "ExternalSystem Schueler ID is required to assign KP to Kind");

        // read kinder from database
        Set<Kind> kinder = new HashSet<>();
        for (String kindIdFromExternalSystem : externalKindIds) {
            kinder.add(kindService.getByExternalId(kindIdFromExternalSystem));
        }
        return kinder;
    }

    @NonNull
    @Transactional(TxType.REQUIRES_NEW)
    public ItemSyncResultJson<KontaktPersonKind> updateExistingKontaktPerson(
        @NonNull ExternalSystemIdDTO idDTO,
        @NonNull KontaktPersonKindExternal kontaktPersonKindExternal,
        @NonNull ImportTyp importTyp
    ) {
        KontaktPersonKind existingKontaktPerson =
            kontaktPersonKindService.getById(ID.of(idDTO.getInternalId(), KontaktPersonKind.class));
        KontaktPersonKindExternal externalToUpdate =
            kontaktPersonKindExternalRepo.getExternalForKontaktPerson(existingKontaktPerson.getId());

        Validate.isTrue(
            Objects.requireNonNull(externalToUpdate).getExternalId().equals(idDTO.getExternalId()),
            "ExternalId mismatch, expected: '%s', actual: '%s' for existingKontaktPerson",
            idDTO.getExternalId(),
            externalToUpdate.getExternalId(),
            existingKontaktPerson.getId());

        KontaktPersonKindExternal updatedExternal = toEntityKontaktPersonKindExternal(
            externalToUpdate,
            kontaktPersonKindExternal
        );
        kontaktPersonKindExternalRepo.persist(updatedExternal);

        KontaktPersonKind updatedKontaktPerson = updateKontaktPersonKindBasedOnExternal(
            existingKontaktPerson,
            updatedExternal,
            appClock.offsetDateTime().now(),
            importTyp
        );

        updatedKontaktPerson.disableOptimisticLockingCheck();

        // update linked Kinder
        Set<Kind> kinder = loadLinkedKinder(updatedExternal);
        existingKontaktPerson.getKinder().clear(); // remove all existing without changing the list reference
        existingKontaktPerson.getKinder().addAll(kinder);

        updatedKontaktPerson = kontaktPersonKindService.persist(updatedKontaktPerson);

        return ItemSyncResultJson.createUpdateOkItem(
            idDTO,
            KontaktPersonKind.class,
            updatedKontaktPerson.getFullNameIfAllowed(configService.isExternalImportRedactPersonalInfo())
        );
    }

    @NonNull
    @Transactional(TxType.REQUIRES_NEW)
    public ItemSyncResultJson<KontaktPersonKind> deactivateRemovedKonkaktPersonen(
        @NonNull ExternalSystemIdDTO externalSystemIdDTO,
        @NonNull ImportTyp importTyp
    ) {
        KontaktPersonKind kontaktPerson = kontaktPersonKindService.getById(
            ID.of(externalSystemIdDTO.getInternalId(), KontaktPersonKind.class));
        KontaktPersonKindExternal externalToDelete =
            kontaktPersonKindExternalRepo.getExternalForKontaktPerson(kontaktPerson.getId());

        Validate.isTrue(
            Objects.requireNonNull(externalToDelete).getExternalId()
                .equals(externalSystemIdDTO.getExternalId()),
            "ExternalId mismatch, expected: '%s', actual: '%s' for KontaktPerson-Kind",
            externalSystemIdDTO.getExternalId(),
            externalToDelete.getExternalId(),
            kontaktPerson.getId());

        if (kontaktPerson.getExternalImportInfo() != null && kontaktPerson.isAktivInExternalSystem()) {
            kontaktPerson.getExternalImportInfo().setAktivInExternalSystem(false);
            kontaktPerson.disableOptimisticLockingCheck();
        }

        kontaktPersonKindExternalRepo.remove(externalToDelete.getId());

        var result = ItemSyncResultJson.createDeactivateOkItem(
            externalSystemIdDTO,
            KontaktPersonKind.class,
            kontaktPerson.getFullNameIfAllowed(configService.isExternalImportRedactPersonalInfo()));

        LOG.info(
            "{}: KontaktPersonKind {} deactivated, internalId: '{}', externalId: '{}'",
            importTyp,
            result.getFriendlyName(),
            result.getInternalId(),
            result.getExternalId()
        );
        return result;

    }

    private static Geschlecht getGeschlecht(@Nullable String anrede) {
        if (StringUtils.isEmpty(anrede)) {
            return Geschlecht.UNBEKANNT;
        }
        if (anrede.equalsIgnoreCase("Herr")) {
            return Geschlecht.MAENNLICH;
        }
        if (anrede.equalsIgnoreCase("Frau")) {
            return Geschlecht.WEIBLICH;
        }
        return Geschlecht.UNBEKANNT;
    }

    private static KontaktPersonKindBeziehung mapBeziehungPupil(@Nullable String beziehung) {
        if ("Vater".equals(beziehung)) {
            return KontaktPersonKindBeziehung.VATER;
        }
        if ("Mutter".equals(beziehung)) {
            return KontaktPersonKindBeziehung.MUTTER;
        }
        if ("Vormundschaft".equals(beziehung)) {
            return KontaktPersonKindBeziehung.VORMUNDSCHAFT;
        }
        return KontaktPersonKindBeziehung.ANDERE;
    }

    private static KontaktPersonKindBeziehung mapBeziehungEscada(Geschlecht geschlecht) {
        if (geschlecht == Geschlecht.MAENNLICH) {
            return KontaktPersonKindBeziehung.VATER;
        }
        if (geschlecht == Geschlecht.WEIBLICH) {
            return KontaktPersonKindBeziehung.MUTTER;
        }
        return KontaktPersonKindBeziehung.ANDERE;
    }

    @Nullable
    private static EmailAddress mapValidEmailAdressOrNull(
        @Nullable String email,
        @NonNull ImportTyp importTyp
    ) {
        if (StringUtils.isEmpty(email)) {
            return null;
        }
        try {
            return EmailAddress.fromString(email);
        } catch (Exception e) {
            LOG.debug(
                "{}: Invalid email address '{}' when importing eltern",
                importTyp,
                email
            );
            return null;
        }
    }

    @Nullable
    private static NormalizedPhoneNumber mapFirstValidPhoneNumberOrNull(
        @Nullable String natel,
        @Nullable String privat,
        @Nullable String geschaeft,
        @NonNull ImportTyp importTyp
    ) {
        return ObjectUtils.firstNonNull(
            mapValidPhoneNumberOrNull(natel, importTyp),
            mapValidPhoneNumberOrNull(privat, importTyp),
            mapValidPhoneNumberOrNull(geschaeft, importTyp)
        );

    }

    @Nullable
    private static NormalizedPhoneNumber mapValidPhoneNumberOrNull(
        @Nullable String phone,
        @NonNull ImportTyp importTyp
    ) {
        if (StringUtils.isEmpty(phone)) {
            return null;
        }
        try {
            return new NormalizedPhoneNumber(phone);
        } catch (Exception e) {
            LOG.debug(
                "{}: Invalid phone number '{}' for kontakt person",
                importTyp,
                phone
            );
            return null;
        }
    }

}
