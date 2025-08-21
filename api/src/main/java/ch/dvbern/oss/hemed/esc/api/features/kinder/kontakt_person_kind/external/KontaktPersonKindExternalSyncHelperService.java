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
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ExternalImportInfoJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKindService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.types.KontaktPersonKindBeziehung;
import ch.dvbern.oss.hemed.esc.api.shared.escada.ExternalSystemIdDTO;
import ch.dvbern.oss.hemed.esc.api.shared.escada.ItemSyncResultJson;
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

    @NonNull
    public static KontaktPersonKind toEntityForCreate(
        @NonNull Set<Kind> kinder,
        @NonNull KontaktPersonKindExternal kontaktPersonKindExternal,
        @NonNull OffsetDateTime now
    ) {
        Geschlecht geschlecht = getGeschlecht(kontaktPersonKindExternal.getAdressierungAnrede());
        return new KontaktPersonKind(
            ExternalImportInfoJson.toEntityOrCreate(
                null,
                kontaktPersonKindExternal.getExternalId(),
                now),
            kinder,
            Objects.toString(kontaktPersonKindExternal.getAdressierungName(), "N/A"),
            Objects.toString(kontaktPersonKindExternal.adressierungVorname, "N/A"),
            geschlecht,
            getBeziehung(geschlecht),
            extractAdresse(kontaktPersonKindExternal),
            mapValidEmailAdressOrNull(kontaktPersonKindExternal.getAdressierungEmail()),
            mapFirstValidPhoneNumberOrNull(
                kontaktPersonKindExternal.getAdressierungNatel(),
                kontaktPersonKindExternal.getAdressierungTelefonPrivat(),
                kontaktPersonKindExternal.getAdressierungTelefonGeschaeft()),
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

    @NonNull
    private KontaktPersonKind updateKontaktPersonKindBasedOnExternal(
        @NonNull KontaktPersonKind kontaktPersonKind,
        @NonNull KontaktPersonKindExternal updatedExternal,
        @NonNull OffsetDateTime now
    ) {
        kontaktPersonKind.setName(Objects.toString(updatedExternal.getAdressierungName(), "N/A"));
        kontaktPersonKind.setVorname(Objects.toString(updatedExternal.getAdressierungVorname(), "N/A"));
        kontaktPersonKind.setGeschlecht(getGeschlecht(updatedExternal.getAdressierungAnrede()));
        kontaktPersonKind.setBeziehung(getBeziehung(kontaktPersonKind.getGeschlecht()));
        kontaktPersonKind.setAdresse(extractAdresse(updatedExternal));
        kontaktPersonKind.setEmail(mapValidEmailAdressOrNull(updatedExternal.getAdressierungEmail()));
        kontaktPersonKind.setTelefon(mapFirstValidPhoneNumberOrNull(
            updatedExternal.getAdressierungNatel(),
            updatedExternal.getAdressierungTelefonPrivat(),
            updatedExternal.getAdressierungTelefonGeschaeft()));

        kontaktPersonKind.setExternalImportInfo(ExternalImportInfoJson.toEntityOrCreate(
            kontaktPersonKind.getExternalImportInfo(),
            updatedExternal.getExternalId(),
            now));

        return kontaktPersonKind;
    }

    @NonNull
    @Transactional(TxType.REQUIRES_NEW)
    public ItemSyncResultJson<KontaktPersonKind> addNewKontaktPerson(
        @NonNull KontaktPersonKindExternal kontaktPersonKindExternalToAdd
    ) {
        // All parents must have at least one kind if ocming from escada
        Set<Kind> kinder = loadLinkedKinder(kontaktPersonKindExternalToAdd);
        Optional<KontaktPersonKind> preexistingKontaktpersonKindOpt =
            findPreexistingLehrperson(kontaktPersonKindExternalToAdd);

        // save the new parent
        KontaktPersonKind internalKpKindToStore = preexistingKontaktpersonKindOpt
            .map(existingKPKind -> updateKontaktPersonKindBasedOnExternal(
                existingKPKind,
                kontaktPersonKindExternalToAdd,
                appClock.offsetDateTime().now()
            ))
            .orElseGet(() -> toEntityForCreate(
                kinder,
                kontaktPersonKindExternalToAdd,
                appClock.offsetDateTime().now()
            ));
        internalKpKindToStore = kontaktPersonKindService.persist(internalKpKindToStore);

        // save KontaktPersonKind
        kontaktPersonKindExternalToAdd.setKontaktPersonKind(internalKpKindToStore);
        kontaktPersonKindExternalRepo.persist(kontaktPersonKindExternalToAdd);

        var result = ItemSyncResultJson.createAddOkItem(
            kontaktPersonKindExternalToAdd,
            internalKpKindToStore.getId(),
            internalKpKindToStore.getName()
        );
        LOG.info(
            "ESCADA: KontaktPersonKind {} added{}, internalId: '{}', externalId: '{}'",
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
        String[] escadaKindIds = StringUtils.split(kontaktPersonKindExternalToAdd.getSchuelerIds(), ",");
        Validate.isTrue(
            escadaKindIds != null && escadaKindIds.length > 0,
            "escada Schueler ID is required to assign KP to Kind");

        // read kinder from database
        Set<Kind> kinder = new HashSet<>();
        for (String escadaKindId : escadaKindIds) {
            kinder.add(kindService.getByExternalId(escadaKindId));
        }
        return kinder;
    }

    @NonNull
    @Transactional(TxType.REQUIRES_NEW)
    public ItemSyncResultJson<KontaktPersonKind> updateExistingKontaktPerson(
        @NonNull ExternalSystemIdDTO idDTO,
        @NonNull KontaktPersonKindExternal kontaktPersonKindExternal
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
            appClock.offsetDateTime().now()
        );

        // update linked Kinder
        Set<Kind> kinder = loadLinkedKinder(updatedExternal);
        existingKontaktPerson.getKinder().clear(); // remove all existing without changing the list reference
        existingKontaktPerson.getKinder().addAll(kinder);

        updatedKontaktPerson = kontaktPersonKindService.persist(updatedKontaktPerson);

        return ItemSyncResultJson.createUpdateOkItem(
            idDTO,
            KontaktPersonKind.class,
            updatedKontaktPerson.getName()
        );
    }

    @NonNull
    @Transactional(TxType.REQUIRES_NEW)
    public ItemSyncResultJson<KontaktPersonKind> deactivateRemovedKonkaktPersonen(
        @NonNull ExternalSystemIdDTO externalSystemIdDTO
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

        if (kontaktPerson.getExternalImportInfo() != null && kontaktPerson.getExternalImportInfo()
            .isAktivInExternalSystem()) {
            kontaktPerson.getExternalImportInfo().setAktivInExternalSystem(false);
        }

        kontaktPersonKindExternalRepo.remove(externalToDelete.getId());

        var result = ItemSyncResultJson.createDeactivateOkItem(
            externalSystemIdDTO,
            KontaktPersonKind.class,
            kontaktPerson.getName());

        LOG.info(
            "ESCADA: KontaktPersonKind {} deactivated, internalId: '{}', externalId: '{}'",
            result.getFriendlyName(),
            result.getInternalId(),
            result.getExternalId()
        );
        return result;

    }

    private static Geschlecht getGeschlecht(@Nullable String anrede) {
        if (StringUtils.isEmpty(anrede)
            || (!anrede.equalsIgnoreCase("Herr")
            && !anrede.equalsIgnoreCase("Frau"))
        ) {
            return Geschlecht.UNBEKANNT;
        }
        if (anrede.equalsIgnoreCase("Herr")) {
            return Geschlecht.MAENNLICH;
        }
        return Geschlecht.WEIBLICH;
    }

    private static KontaktPersonKindBeziehung getBeziehung(Geschlecht geschlecht) {
        if (geschlecht == Geschlecht.MAENNLICH) {
            return KontaktPersonKindBeziehung.VATER;
        }
        if (geschlecht == Geschlecht.WEIBLICH) {
            return KontaktPersonKindBeziehung.MUTTER;
        }
        return KontaktPersonKindBeziehung.ANDERE;
    }

    @Nullable
    private static EmailAddress mapValidEmailAdressOrNull(@Nullable String email) {
        if (StringUtils.isEmpty(email)) {
            return null;
        }
        try {
            return EmailAddress.fromString(email);
        } catch (Exception e) {
            LOG.debug(
                "ESCADA: Invalid email address '{}' when importing eltern",
                email
            );
            return null;
        }
    }

    @Nullable
    private static NormalizedPhoneNumber mapFirstValidPhoneNumberOrNull(
        @Nullable String natel,
        @Nullable String privat,
        @Nullable String geschaeft
    ) {
        return ObjectUtils.firstNonNull(
            mapValidPhoneNumberOrNull(natel),
            mapValidPhoneNumberOrNull(privat),
            mapValidPhoneNumberOrNull(geschaeft)
        );

    }

    @Nullable
    private static NormalizedPhoneNumber mapValidPhoneNumberOrNull(@Nullable String phone) {
        if (StringUtils.isEmpty(phone)) {
            return null;
        }
        try {
            return new NormalizedPhoneNumber(phone);
        } catch (Exception e) {
            LOG.debug(
                "ESCADA: Invalid phone number '{}' for kontakt person",
                phone
            );
            return null;
        }
    }

}
