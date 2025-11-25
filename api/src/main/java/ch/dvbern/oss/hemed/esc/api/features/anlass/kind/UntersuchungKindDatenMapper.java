package ch.dvbern.oss.hemed.esc.api.features.anlass.kind;

import java.time.LocalDate;
import java.util.Objects;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.i18n.datetime.DateUtil;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.kind.json.UntersuchungKindDatenJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.kind.json.UntersuchungKindDatenSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.types.Klassenstufe;
import ch.dvbern.oss.hemed.esc.api.shared.person.LandCode;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;


@JaxMapper
@RequiredArgsConstructor
public final class UntersuchungKindDatenMapper {

    private final UntersuchungService untersuchungService;
    private final UntersuchungKindDatenService untersuchungKindDatenService;
    private final AppClock appClock;

    @NonNull
    public static UntersuchungKindDatenJson toJson(
        @NonNull Untersuchung untersuchung,
        @NonNull LocalDate now) {
        // as long as the data was not saved in the ungersuchungDaten we dynamically create the json
        boolean dataAlreadyCopied = untersuchung.getUntersuchungKindDaten() != null;
        return dataAlreadyCopied
            ?
            UntersuchungKindDatenMapper.toJson(
                untersuchung.getUntersuchungKindDaten(),
                untersuchung.getEinladung(),
                now)
            :
            UntersuchungKindDatenMapper.toJson(
                untersuchung.getEinladung().getAnlass(),
                untersuchung.getEinladung().getDossier().getKind(),
                now);
    }

    @NonNull
    private static UntersuchungKindDatenJson toJson(
        @NonNull UntersuchungKindDaten daten,
        @NonNull Einladung einladung,
        @NonNull LocalDate now
    ) {
        return new UntersuchungKindDatenJson(
            daten.getId(),
            EntityInfoJson.fromEntity(daten),
            new UntersuchungKindDatenSaveJson(
                daten.getName(),
                daten.getVorname(),
                daten.getGeschlecht(),
                daten.getGeburtsdatum(),
                daten.getNationalitaet(),
                daten.getSchulhaus(),
                daten.getQuartier(),
                daten.getSchulkreis(),
                daten.getSchulhausnummer(),
                daten.getKlassenstufe(),
                daten.getKlassentyp(),
                daten.getKlasse(),
                daten.getVersion()
            ),
            einladung.getDossier().getKind().getEsadId(),
            DateUtil.calculateAlterDuodezimal(daten.getGeburtsdatum(), now),
            LandCode.getCode(daten.getNationalitaet()),
            daten.getKlasseId(),
            einladung.getAnlass().getDatum()
        );
    }

    @NonNull
    private static UntersuchungKindDatenJson toJson(
        @NonNull Anlass anlass,
        @NonNull Kind kind,
        @NonNull LocalDate now
    ) {
        Klasse klasse = kind.getKlasse();
        Schule schule = kind.extractSchule();
        String klassenname = null;
        String klasseId = null;
        String schulkreis = null;
        Klassenstufe klassenstufe = null;
        String klassentyp = null;
        String quartier = null;
        String schulhausnummer = null;
        String schulhaus = null;
        if (klasse != null) {
            klassenname = klasse.getBezeichnung();
            klasseId = klasse.getId().toString();
            klassentyp = klasse.getKlasseSchulart();
            klassenstufe = klasse.getKlassenstufe();
        }
        if (schule != null) {
            schulhaus = schule.getSchulhaus();
            schulkreis = schule.getSchulkreis();
            quartier = schule.getQuartier() != null ? schule.getQuartier().getQuartierName() : null;
            schulhausnummer = schule.getSchulhausnummer();
        }
        return new UntersuchungKindDatenJson(
            null,
            EntityInfoJson.fromEntity(kind),
            new UntersuchungKindDatenSaveJson(
                kind.getName(),
                kind.getVorname(),
                kind.getGeschlecht(),
                kind.getGeburtsdatum(),
                kind.getNationalitaet(),
                schulhaus,
                quartier,
                schulkreis,
                schulhausnummer,
                klassenstufe,
                klassentyp,
                klassenname,
                0L
            ),
            kind.getEsadId(),
            DateUtil.calculateAlterDuodezimal(kind.getGeburtsdatum(), now),
            LandCode.getCode(kind.getNationalitaet()),
            klasseId,
            anlass.getDatum()
        );
    }

    @NonNull
    private static UntersuchungKindDaten toEntity(
        @NonNull UntersuchungKindDaten kindDaten,
        @NonNull UntersuchungKindDatenSaveJson saveJson
    ) {
        kindDaten.setName(saveJson.getName());
        kindDaten.setVorname(saveJson.getVorname());
        kindDaten.setGeschlecht(saveJson.getGeschlecht());
        kindDaten.setGeburtsdatum(saveJson.getGeburtsdatum());
        kindDaten.setNationalitaet(saveJson.getNationalitaet());
        kindDaten.setSchulhaus(saveJson.getSchulhaus());
        kindDaten.setQuartier(saveJson.getQuartier());
        kindDaten.setSchulkreis(saveJson.getSchulkreis());
        kindDaten.setKlassenstufe(saveJson.getKlassenstufe());
        kindDaten.setKlassentyp(saveJson.getKlassentyp());
        if (!Objects.equals(kindDaten.getKlasse(), saveJson.getKlasse())) {
            kindDaten.setKlasseId(null);
        }
        kindDaten.setKlasse(saveJson.getKlasse());
        return kindDaten;
    }

    @NonNull
    public UntersuchungKindDatenJson getByEinladung(@NonNull ID<Einladung> einladungID) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungID);
        return UntersuchungKindDatenMapper.toJson(untersuchung, appClock.offsetDateTime().now().toLocalDate());
    }

    @NonNull
    public UntersuchungKindDatenJson update(
        @NonNull ID<Einladung> einladungID,
        @NonNull UntersuchungKindDatenSaveJson saveJson
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungID);
        UntersuchungKindDaten kindDaten = untersuchung.getUntersuchungKindDaten();

        // Kind Daten müssen zu diesem Zeitpunkt eigentlich immer vorhanden sein.
        if (kindDaten == null) {
            kindDaten = UntersuchungKindDaten.ofKind(untersuchung.getEinladung().getDossier().getKind());
        }
        UntersuchungKindDaten entity = toEntity(kindDaten, saveJson);

        return toJson(
            untersuchungKindDatenService.persistWithOptimisticLocking(entity, saveJson.getVersion()),
            untersuchung.getEinladung(),
            appClock.offsetDateTime().now().toLocalDate()
        );
    }
}
