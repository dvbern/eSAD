package ch.dvbern.oss.hemed.esc.api.features.anlass;

import java.util.ArrayList;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.AnlassJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.AnlassListJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.AnlassOptionJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.AnlassSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassStatus;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerMapper;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.SchuleMapper;
import ch.dvbern.oss.hemed.esc.api.features.schule.json.SchulInformationenJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.json.KlasseLehrpersonenJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.LehrpersonMapper;
import ch.dvbern.oss.hemed.esc.api.shared.aktivitaet.AktivitaetHistoryService;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.types.DateTimeRange;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@JaxMapper
@RequiredArgsConstructor
public final class AnlassMapper {

    private final AnlassService anlassService;
    private final AnlassConfigService anlassConfigService;
    private final BenutzerService benutzerService;
    private final LehrpersonMapper lehrpersonMapper;
    private final AktivitaetHistoryService aktivitaetHistoryService;

    @NonNull
    public static AnlassJson toJson(@NonNull Anlass anlass) {
        Benutzer arzt = anlass.getArzt();
        Benutzer mpa = anlass.getMpa();
        Benutzer weiterePerson = anlass.getWeiterePerson();
        DateTimeRange zeitVonBis = anlass.getZeitVonBis();
        return new AnlassJson(
            anlass.getId(),
            EntityInfoJson.fromEntity(anlass),
            new AnlassSaveJson(
                anlass.getAnlassConfig().getAnlassTyp(),
                anlass.getTitel(),
                anlass.getAnlassStatus(),
                anlass.getDatum(),
                anlass.getSchuljahr(),
                zeitVonBis != null ? zeitVonBis.getVon() : null,
                zeitVonBis != null ? zeitVonBis.getBis() : null,
                anlass.getAnlassArt(),
                arzt != null ? BenutzerMapper.toOptionJson(arzt) : null,
                anlass.getArztInfo(),
                mpa != null ? BenutzerMapper.toOptionJson(mpa) : null,
                anlass.getMpaInfo(),
                weiterePerson != null ? BenutzerMapper.toOptionJson(weiterePerson) : null,
                anlass.getWeiterePersonInfo(),
                anlass.getVersion()
            ),
            AnlassConfigMapper.toJson(anlass.getAnlassConfig())
        );
    }

    @NonNull
    public static AnlassOptionJson toOptionJson(@NonNull Anlass anlass) {
        return new AnlassOptionJson(
            anlass.getId(),
            anlass.getTitel()
        );
    }

    @NonNull
    private Anlass toEntityForCreate(@NonNull AnlassSaveJson saveJson) {
        AnlassConfig config =
            anlassConfigService.getByAnlassTypAndStichtag(saveJson.getAnlassTyp(), saveJson.getDatum());
        Benutzer arzt = saveJson.getArzt() != null ? benutzerService.getById(saveJson.getArzt().getId()) : null;
        Benutzer mpa = saveJson.getMpa() != null ? benutzerService.getById(saveJson.getMpa().getId()) : null;
        Benutzer weiterePerson =
            saveJson.getWeiterePerson() != null ? benutzerService.getById(saveJson.getWeiterePerson().getId()) : null;
        // Set Anlass Date to ensure the Date is correct on DateTimeRange
        DateTimeRange dateTimeRange = saveJson.getZeitVon() != null && saveJson.getZeitBis() != null ?
            new DateTimeRange(
                saveJson.getDatum().atTime(saveJson.getZeitVon().toLocalTime()),
                saveJson.getDatum().atTime(saveJson.getZeitBis().toLocalTime())) : null;
        return new Anlass(
            config,
            saveJson.getTitel(),
            saveJson.getAnlassStatus(),
            saveJson.getDatum(),
            saveJson.getSchuljahr(),
            dateTimeRange,
            saveJson.getAnlassArt(),
            arzt,
            saveJson.getArztInfo(),
            mpa,
            saveJson.getMpaInfo(),
            weiterePerson,
            saveJson.getWeiterePersonInfo()
        );
    }

    @NonNull
    private Anlass toEntity(@NonNull Anlass existingAnlass, @NonNull AnlassSaveJson saveJson) {
        // Wird nur gespeichert, wenn von und bis vorhanden sind
        DateTimeRange dateTimeRange = saveJson.getZeitVon() != null && saveJson.getZeitBis() != null
            ? new DateTimeRange(
            saveJson.getDatum().atTime(saveJson.getZeitVon().toLocalTime()),
            saveJson.getDatum().atTime(saveJson.getZeitBis().toLocalTime()))
            : null;

        // AnlassConfig kann nicht geändert werden
        existingAnlass.setTitel(saveJson.getTitel());
        existingAnlass.setAnlassStatus(saveJson.getAnlassStatus());
        existingAnlass.setDatum(saveJson.getDatum());
        existingAnlass.setSchuljahr(saveJson.getSchuljahr());
        existingAnlass.setZeitVonBis(dateTimeRange);
        existingAnlass.setAnlassArt(saveJson.getAnlassArt());
        Benutzer arzt = saveJson.getArzt() != null ? benutzerService.getById(saveJson.getArzt().getId()) : null;
        existingAnlass.setArzt(arzt);
        existingAnlass.setArztInfo(saveJson.getArztInfo());
        Benutzer mpa = saveJson.getMpa() != null ? benutzerService.getById(saveJson.getMpa().getId()) : null;
        existingAnlass.setMpa(mpa);
        existingAnlass.setMpaInfo(saveJson.getMpaInfo());
        Benutzer weiterePerson =
            saveJson.getWeiterePerson() != null ? benutzerService.getById(saveJson.getWeiterePerson().getId()) : null;
        existingAnlass.setWeiterePerson(weiterePerson);
        existingAnlass.setWeiterePersonInfo(saveJson.getWeiterePersonInfo());
        return existingAnlass;
    }

    @NonNull
    public List<AnlassJson> findAll() {
        return anlassService.findAll().stream()
            .map(AnlassMapper::toJson)
            .toList();
    }

    @NonNull
    public List<KlasseLehrpersonenJson> findKlassenLehrpersonenForAnlass(@NonNull ID<Anlass> anlassId) {
        List<Klasse> klassen = anlassService.findKlassenForAnlass(anlassId);
        return lehrpersonMapper.findLehrpersonenByKlassen(klassen);
    }

    @NonNull
    public AnlassJson getById(@NonNull ID<Anlass> anlassID) {
        Anlass anlass = anlassService.getById(anlassID);
        aktivitaetHistoryService.createAktivitaetAnlass(anlass);
        return AnlassMapper.toJson(anlass);
    }

    @NonNull
    public AnlassJson create(@NonNull AnlassSaveJson saveJson) {
        Anlass entity = toEntityForCreate(saveJson);
        return toJson(anlassService.persist(entity));
    }

    @NonNull
    public AnlassJson update(@NonNull ID<Anlass> anlassID, @NonNull AnlassSaveJson saveJson) {
        Anlass existingAnlass = anlassService.getById(anlassID);
        Anlass entity = toEntity(existingAnlass, saveJson);
        return toJson(anlassService.persistWithOptimisticLocking(entity, saveJson.getVersion()));
    }

    @NonNull
    public AnlassJson changeAnlassStatus(@NonNull ID<Anlass> anlassID, @NonNull AnlassStatus status) {
        Anlass entity = anlassService.changeAnlassStatus(anlassID, status);
        return toJson(anlassService.persist(entity));
    }

    public void remove(@NonNull ID<Anlass> anlassID) {
        anlassService.remove(anlassID);
    }

    @NonNull
    public AnlassListJson findAllForUser(@NonNull ID<Benutzer> benutzerID) {
        List<AnlassJson> list = anlassService.findAllForUserInFuture(benutzerID).stream()
            .map(AnlassMapper::toJson)
            .toList();
        return new AnlassListJson(list, anlassService.getCountAllforUserInFuture(benutzerID));
    }

    public @NonNull @NotNull List<SchulInformationenJson> findSchulInformationen(
        @NotNull @NonNull ID<Anlass> anlassId
    ) {
        List<Schule> schulen = new ArrayList<>();
        List<SchulInformationenJson> schulInformationenJsonList = new ArrayList<>();
        List<Klasse> klassen = anlassService.findKlassenForAnlass(anlassId);

        klassen.forEach(klasse -> {
            if (!schulen.contains(klasse.getSchule())) {
                schulen.add(klasse.getSchule());
            }
        });

        schulen.forEach(schule -> {
            List<Klasse> klassenBySchule =
                klassen.stream().filter(klasse -> klasse.getSchule().getId().equals(schule.getId())).toList();
            SchulInformationenJson json = new SchulInformationenJson(
                SchuleMapper.toOptionJson(schule),
                lehrpersonMapper.findLehrpersonenByKlassen(klassenBySchule)
            );
            schulInformationenJsonList.add(json);
        });

        return schulInformationenJsonList;
    }
}
