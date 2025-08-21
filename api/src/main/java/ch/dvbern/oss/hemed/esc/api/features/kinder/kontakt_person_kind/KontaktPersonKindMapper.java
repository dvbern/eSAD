package ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind;

import java.util.List;
import java.util.Set;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ExternalImportInfoJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.json.KontaktPersonKindJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.json.KontaktPersonKindOptionJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.json.KontaktPersonKindSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.person.AdresseMapper;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@RequiredArgsConstructor
public final class KontaktPersonKindMapper {

    private final KontaktPersonKindService kontaktPersonKindService;
    private final KindService kindService;

    @NonNull
    public static KontaktPersonKindJson toJson(@NonNull KontaktPersonKind kontaktPerson) {
        return new KontaktPersonKindJson(
            kontaktPerson.getId(),
            ExternalImportInfoJson.toJson(kontaktPerson.getExternalImportInfo()),
            kontaktPerson.isManualOrAktivInExternalSystem(),
            EntityInfoJson.fromEntity(kontaktPerson),
            new KontaktPersonKindSaveJson(
                kontaktPerson.getName(),
                kontaktPerson.getVorname(),
                kontaktPerson.getGeschlecht(),
                kontaktPerson.getBeziehung(),
                AdresseMapper.toJson(kontaktPerson.getAdresse()),
                kontaktPerson.getEmail(),
                kontaktPerson.getTelefon(),
                kontaktPerson.isKorrespondenz())
        );
    }

    @NonNull
    public static KontaktPersonKindOptionJson toOptionJson(@NonNull KontaktPersonKind kontaktPersonKind) {
        return new KontaktPersonKindOptionJson(
            kontaktPersonKind.getId(),
            kontaktPersonKind.getGeschlecht(),
            kontaktPersonKind.getName(),
            kontaktPersonKind.getVorname(),
            kontaktPersonKind.getNameVorname());
    }

    @NonNull
    public static KontaktPersonKind toEntity(
        @NonNull KontaktPersonKind existingKontaktPersonKind,
        @NonNull KontaktPersonKindSaveJson saveJson
    ) {
        existingKontaktPersonKind.setName(saveJson.getName());
        existingKontaktPersonKind.setVorname(saveJson.getVorname());
        existingKontaktPersonKind.setGeschlecht(saveJson.getGeschlecht());
        existingKontaktPersonKind.setBeziehung(saveJson.getBeziehung());
        existingKontaktPersonKind.setAdresse(AdresseMapper.toEntity(saveJson.getAdresse()));
        existingKontaktPersonKind.setEmail(saveJson.getEmail());
        existingKontaktPersonKind.setTelefon(saveJson.getTelefon());
        existingKontaktPersonKind.setKorrespondenz(saveJson.isKorrespondenz());
        return existingKontaktPersonKind;
    }

    @NonNull
    public List<KontaktPersonKindJson> findAktivByKind(@NonNull ID<Kind> kindID) {
        return kontaktPersonKindService.findAktivByKind(kindID).stream()
            .map(KontaktPersonKindMapper::toJson)
            .toList();
    }

    @NonNull
    public List<KontaktPersonKindOptionJson> findAllOptionsByKind(@Nullable ID<Kind> kindToExclude) {
        return kontaktPersonKindService.findAllAktivOptions(kindToExclude)
            .stream()
            .map(KontaktPersonKindMapper::toOptionJson)
            .toList();
    }

    @NonNull
    public KontaktPersonKindJson getById(@NonNull ID<KontaktPersonKind> id) {
        return toJson(kontaktPersonKindService.getById(id));
    }

    @NonNull
    public KontaktPersonKindJson create(@NonNull ID<Kind> kindID, @NonNull KontaktPersonKindSaveJson saveJson) {
        Kind kind = kindService.getById(kindID);
        KontaktPersonKind kontaktPersonKind = new KontaktPersonKind(
            null, // Null because we are creating a new entity from our System, not importing
            Set.of(kind),
            saveJson.getName(),
            saveJson.getVorname(),
            saveJson.getGeschlecht(),
            saveJson.getBeziehung(),
            AdresseMapper.toEntity(saveJson.getAdresse()),
            saveJson.getEmail(),
            saveJson.getTelefon(),
            saveJson.isKorrespondenz()
        );
        return toJson(kontaktPersonKindService.persist(kontaktPersonKind));
    }

    @NonNull
    public KontaktPersonKindJson update(
        @NonNull ID<KontaktPersonKind> kontaktPersonKindID,
        @NonNull KontaktPersonKindSaveJson saveJson
    ) {
        KontaktPersonKind existingKP = kontaktPersonKindService.getById(kontaktPersonKindID);
        KontaktPersonKind entity = toEntity(existingKP, saveJson);
        return toJson(kontaktPersonKindService.persist(entity));
    }

    public void delete(@NonNull ID<KontaktPersonKind> kontaktPersonKindID) {
        kontaktPersonKindService.remove(kontaktPersonKindID);
    }

    @NonNull
    public KontaktPersonKindJson addToKind(
        @NonNull ID<Kind> kindID,
        @NonNull ID<KontaktPersonKind> kontaktPersonKindID
    ) {
        return toJson(kontaktPersonKindService.addToKind(kindID, kontaktPersonKindID));
    }

    public void removeFromKind(@NonNull ID<Kind> kindID, @NonNull ID<KontaktPersonKind> kontaktPersonKindID) {
        kontaktPersonKindService.removeFromKind(kindID, kontaktPersonKindID);
    }
}
