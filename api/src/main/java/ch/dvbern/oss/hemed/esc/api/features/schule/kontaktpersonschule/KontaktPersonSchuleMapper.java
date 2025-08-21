package ch.dvbern.oss.hemed.esc.api.features.schule.kontaktpersonschule;

import java.util.List;
import java.util.Set;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.SchuleService;
import ch.dvbern.oss.hemed.esc.api.features.schule.kontaktpersonschule.json.KontaktPersonSchuleJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.kontaktpersonschule.json.KontaktPersonSchuleOptionJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.kontaktpersonschule.json.KontaktPersonSchuleSaveJson;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@RequiredArgsConstructor
public final class KontaktPersonSchuleMapper {

    private final KontaktPersonSchuleService kontaktPersonSchuleService;
    private final SchuleService schuleService;

    @NonNull
    public static KontaktPersonSchuleJson toJson(@NonNull KontaktPersonSchule kontaktPerson) {
        return new KontaktPersonSchuleJson(
            kontaktPerson.getId(),
            EntityInfoJson.fromEntity(kontaktPerson),
            new KontaktPersonSchuleSaveJson(
                kontaktPerson.getGeschlecht(),
                kontaktPerson.getFunktion(),
                kontaktPerson.getName(),
                kontaktPerson.getVorname(),
                kontaktPerson.getEmail(),
                kontaktPerson.getTelefon())
        );
    }

    @NonNull
    public static KontaktPersonSchule toEntity(
        @NonNull KontaktPersonSchule existingKontaktpersonSchule,
        @NonNull KontaktPersonSchuleSaveJson saveJson) {
        existingKontaktpersonSchule.setGeschlecht(saveJson.getGeschlecht());
        existingKontaktpersonSchule.setFunktion(saveJson.getFunktion());
        existingKontaktpersonSchule.setName(saveJson.getName());
        existingKontaktpersonSchule.setVorname(saveJson.getVorname());
        existingKontaktpersonSchule.setEmail(saveJson.getEmail());
        existingKontaktpersonSchule.setTelefon(saveJson.getTelefon());
        return existingKontaktpersonSchule;
    }

    @NonNull
    public static KontaktPersonSchuleOptionJson toOptionJson(@NonNull KontaktPersonSchule kontaktPerson) {
        return new KontaktPersonSchuleOptionJson(
            kontaktPerson.getId(),
            kontaktPerson.getGeschlecht(),
            kontaktPerson.getName(),
            kontaktPerson.getVorname(),
            kontaktPerson.getName() + ' ' + kontaktPerson.getVorname());
    }

    @NonNull
    public List<KontaktPersonSchuleOptionJson> findAllOptionsActive(@Nullable ID<Schule> schuleToExclude) {
        return kontaktPersonSchuleService.findAllOptionsActive(schuleToExclude)
            .stream()
            .map(KontaktPersonSchuleMapper::toOptionJson)
            .toList();
    }

    @NonNull
    public List<KontaktPersonSchuleJson> findAllAktivBySchule(@NonNull ID<Schule> schuleId) {
        return kontaktPersonSchuleService.findAktivBySchule(schuleId)
            .stream()
            .map(KontaktPersonSchuleMapper::toJson)
            .toList();
    }

    @NonNull
    public KontaktPersonSchuleJson getById(@NonNull ID<KontaktPersonSchule> kontaktPersonSchuleID) {
        return toJson(kontaktPersonSchuleService.getById(kontaktPersonSchuleID));
    }

    @NonNull
    public KontaktPersonSchuleJson create(@NonNull ID<Schule> schuleId, @NonNull KontaktPersonSchuleSaveJson saveJson) {
        Schule schule = schuleService.getById(schuleId);
        KontaktPersonSchule entity = new KontaktPersonSchule(
            Set.of(schule),
            saveJson.getGeschlecht(),
            saveJson.getFunktion(),
            saveJson.getName(),
            saveJson.getVorname(),
            saveJson.getEmail(),
            saveJson.getTelefon(),
            true);
        return toJson(kontaktPersonSchuleService.persist(entity));
    }

    @NonNull
    public KontaktPersonSchuleJson update(
        @NonNull ID<KontaktPersonSchule> kontaktPersonID,
        @NonNull KontaktPersonSchuleSaveJson saveJson
    ) {
        KontaktPersonSchule existingKontaktPersonSchule = kontaktPersonSchuleService.getById(kontaktPersonID);
        KontaktPersonSchule entity = toEntity(existingKontaktPersonSchule, saveJson);
        return toJson(kontaktPersonSchuleService.persist(entity));
    }

    public void delete(@NonNull ID<KontaktPersonSchule> kontaktPersonID) {
        KontaktPersonSchule kp = kontaktPersonSchuleService.getById(kontaktPersonID);
        kontaktPersonSchuleService.remove(kp.getId());
    }

    @NonNull
    public KontaktPersonSchuleJson addToSchule(
        @NonNull ID<Schule> schuleId,
        @NonNull ID<KontaktPersonSchule> kontaktPersonSchuleID
    ) {
        KontaktPersonSchule kp = kontaktPersonSchuleService.getById(kontaktPersonSchuleID);
        Schule schule = schuleService.getById(schuleId);
        kp.getSchulen().add(schule);
        return toJson(kontaktPersonSchuleService.persist(kp));
    }

    public void removeFromSchule(@NonNull ID<Schule> schuleId, @NonNull ID<KontaktPersonSchule> kontaktPersonID) {
        KontaktPersonSchule kp = kontaktPersonSchuleService.getById(kontaktPersonID);
        Schule schule = schuleService.getById(schuleId);
        kp.getSchulen().remove(schule);
        kontaktPersonSchuleService.persist(kp);
        if (kp.getSchulen().isEmpty()) {
            kontaktPersonSchuleService.remove(kp.getId());
        }
    }
}
