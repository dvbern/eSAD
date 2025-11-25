package ch.dvbern.oss.hemed.esc.api.features.anlass.durchfuehrung;

import java.time.LocalDate;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.durchfuehrung.json.UntersuchungDurchfuehrungDataJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.durchfuehrung.json.UntersuchungDurchfuehrungDataSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.durchfuehrung.json.UntersuchungDurchfuehrungJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.durchfuehrung.json.UntersuchungDurchfuehrungSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.kind.json.UntersuchungKindDatenJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.kind.UntersuchungKindDatenMapper;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerMapper;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@RequiredArgsConstructor
public final class UntersuchungDurchfuehrungMapper {

    private final BenutzerService benutzerService;
    private final UntersuchungService untersuchungService;
    private final UntersuchungDurchfuehrungService untersuchungDurchfuehrungService;
    private final UntersuchungKindDatenMapper untersuchungKindDatenMapper;

    private final AppClock appClock;

    @Nullable
    public static UntersuchungDurchfuehrungJson toJson(@Nullable UntersuchungDurchfuehrung durchfuehrung) {
        if (durchfuehrung == null) {
            return null;
        }
        Benutzer arzt = durchfuehrung.getArzt();
        Benutzer mpa = durchfuehrung.getMpa();
        return new UntersuchungDurchfuehrungJson(
            durchfuehrung.getId(),
            EntityInfoJson.fromEntity(durchfuehrung),
            new UntersuchungDurchfuehrungSaveJson(
                arzt != null ? BenutzerMapper.toOptionJson(arzt) : null,
                mpa != null ? BenutzerMapper.toOptionJson(mpa) : null,
                durchfuehrung.isNachkontrolle(),
                durchfuehrung.isNachzuegler(),
                durchfuehrung.isNeueintritt(),
                durchfuehrung.getBemerkungen(),
                durchfuehrung.getVersion()
            )
        );
    }

    @NonNull
    public static UntersuchungDurchfuehrungDataJson toDataJson(
        @NonNull Untersuchung currentUntersuchung,
        @Nullable UntersuchungDurchfuehrung existingDurchfuehrung,
        @NonNull UntersuchungDurchfuehrung defaultsForNextUntersuchung,
        @NonNull LocalDate now
    ) {
        UntersuchungDurchfuehrung existingDurchfuehrungOrDefaults = existingDurchfuehrung != null
            ? existingDurchfuehrung
            : defaultsForNextUntersuchung;
        Benutzer arzt = existingDurchfuehrungOrDefaults.getArzt();
        Benutzer mpa = existingDurchfuehrungOrDefaults.getMpa();

        return new UntersuchungDurchfuehrungDataJson(
            new UntersuchungDurchfuehrungSaveJson(
                arzt != null ? BenutzerMapper.toOptionJson(arzt) : null,
                mpa != null ? BenutzerMapper.toOptionJson(mpa) : null,
                existingDurchfuehrungOrDefaults.isNachkontrolle(),
                existingDurchfuehrungOrDefaults.isNachzuegler(),
                existingDurchfuehrungOrDefaults.isNeueintritt(),
                existingDurchfuehrungOrDefaults.getBemerkungen(),
                existingDurchfuehrungOrDefaults.getVersion()
            ),
            existingDurchfuehrungOrDefaults.getTimestampErstellt() != null ? toJson(existingDurchfuehrung) : null,
            UntersuchungKindDatenMapper.toJson(currentUntersuchung, now)
        );
    }

    @NonNull
    private UntersuchungDurchfuehrung toEntity(
        @NonNull Untersuchung untersuchung,
        @Nullable UntersuchungDurchfuehrung durchfuehrung,
        @NonNull UntersuchungDurchfuehrungSaveJson saveJson
    ) {
        Benutzer arzt = saveJson.getArzt() != null ? benutzerService.getById(saveJson.getArzt().getId()) : null;
        Benutzer mpa = saveJson.getMpa() != null ? benutzerService.getById(saveJson.getMpa().getId()) : null;
        if (durchfuehrung == null) {
            durchfuehrung = UntersuchungDurchfuehrung.forUntersuchung(untersuchung);
        }
        durchfuehrung.setArzt(arzt);
        durchfuehrung.setMpa(mpa);
        durchfuehrung.setNachkontrolle(saveJson.isNachkontrolle());
        durchfuehrung.setNachzuegler(saveJson.isNachzuegler());
        durchfuehrung.setNeueintritt(saveJson.isNeueintritt());
        durchfuehrung.setBemerkungen(saveJson.getBemerkungen());
        return durchfuehrung;
    }

    @NonNull
    public UntersuchungDurchfuehrungDataJson getByEinladung(@NonNull ID<Einladung> einladungId) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungId);
        UntersuchungDurchfuehrung untersuchungDurchfuehrung = untersuchung.getUntersuchungDurchfuehrung();
        UntersuchungDurchfuehrung defaultsForNextUntersuchung =
            untersuchungDurchfuehrungService.getDefaultUntersuchungDurchfuehrung(untersuchung);
        return toDataJson(
            untersuchung,
            untersuchungDurchfuehrung,
            defaultsForNextUntersuchung,
            appClock.offsetDateTime().now().toLocalDate());
    }

    @NonNull
    public UntersuchungDurchfuehrungJson save(
        @NonNull ID<Einladung> einladungID,
        @NonNull UntersuchungDurchfuehrungSaveJson saveJson
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungID);
        UntersuchungDurchfuehrung durchfuehrung = untersuchung.getUntersuchungDurchfuehrung();
        UntersuchungDurchfuehrung entity = toEntity(untersuchung, durchfuehrung, saveJson);
        return toJson(untersuchungDurchfuehrungService.persistWithOptimisticLocking(entity, saveJson.getVersion()));
    }

    @NonNull
    public UntersuchungDurchfuehrungDataJson saveUntersuchungDurchfuehrungAbgeschlossen(
        @NonNull ID<Einladung> einladungID,
        @NonNull UntersuchungDurchfuehrungDataSaveJson saveJson
    ) {
        UntersuchungDurchfuehrungJson durchfuehrungJson = save(einladungID, saveJson.getUntersuchungDurchfuehrung());
        UntersuchungKindDatenJson kindDatenJson =
            untersuchungKindDatenMapper.update(einladungID, saveJson.getKindDaten());

        return new UntersuchungDurchfuehrungDataJson(
            durchfuehrungJson.getData(),
            durchfuehrungJson,
            kindDatenJson
        );
    }
}
