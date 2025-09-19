package ch.dvbern.oss.hemed.esc.api.features.anlass;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungTriageJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungTriageSaveJson;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@RequiredArgsConstructor
public final class UntersuchungTriageMapper {

    private final EinladungService einladungService;
    private final UntersuchungService untersuchungService;
    private final UntersuchungTriageService untersuchungTriageService;
    private final AppClock appClock;

    @Nullable
    public static UntersuchungTriageJson toJson(@Nullable UntersuchungTriage triage) {
        if (triage == null) {
            return null;
        }
        return new UntersuchungTriageJson(
            triage.getId(),
            EntityInfoJson.fromEntity(triage),
            new UntersuchungTriageSaveJson(
                triage.isTriageSu(),
                triage.getTriageSuBemerkungen(),
                triage.isTriageMpa(),
                triage.getTriageMpaBemerkungen(),
                triage.isBefundSu(),
                triage.getBefundSuBemerkungen(),
                triage.isWunschSchueler(),
                triage.getWunschSchuelerBemerkungen(),
                triage.isVorinformationLehrperson(),
                triage.getVorinformationLehrpersonBemerkungen(),
                triage.getUntersuchung().getEinladung().getNaechsteStation(),
                triage.getVersion()
            )
        );
    }

    @NonNull
    public static UntersuchungTriage toEntity(
        @NonNull Untersuchung untersuchung,
        @Nullable UntersuchungTriage triage,
        @NonNull UntersuchungTriageSaveJson saveJson
    ) {
        if (triage == null) {
            triage = UntersuchungTriage.forUntersuchung(untersuchung);
        }
        triage.setTriageSu(saveJson.isTriageSu());
        triage.setTriageSuBemerkungen(saveJson.isTriageSu()
            ? saveJson.getTriageSuBemerkungen()
            : null);
        triage.setTriageMpa(saveJson.isTriageMpa());
        triage.setTriageMpaBemerkungen(saveJson.isTriageMpa()
            ? saveJson.getTriageMpaBemerkungen()
            : null);
        triage.setBefundSu(saveJson.isBefundSu());
        triage.setBefundSuBemerkungen(saveJson.isBefundSu()
            ? saveJson.getBefundSuBemerkungen()
            : null);
        triage.setWunschSchueler(saveJson.isWunschSchueler());
        triage.setWunschSchuelerBemerkungen(saveJson.isWunschSchueler()
            ? saveJson.getWunschSchuelerBemerkungen()
            : null);
        triage.setVorinformationLehrperson(saveJson.isVorinformationLehrperson());
        triage.setVorinformationLehrpersonBemerkungen(saveJson.isVorinformationLehrperson()
            ? saveJson.getVorinformationLehrpersonBemerkungen()
            : null);
        return triage;
    }

    @Nullable
    public UntersuchungTriageJson getByEinladung(@NonNull ID<Einladung> einladungId) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungId);
        UntersuchungTriage untersuchungTriage =
            untersuchungTriageService.getUntersuchungTriage(untersuchung.getId());
        return untersuchungTriage != null ? toJson(untersuchungTriage) : null;
    }

    @NonNull
    public UntersuchungTriageJson save(
        @NonNull ID<Einladung> einladungID,
        @NonNull UntersuchungTriageSaveJson saveJson
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungID);
        Einladung einladungToUpdate = untersuchung.getEinladung();
        if (einladungToUpdate.getNaechsteStation() != saveJson.getNaechsteStation()) {
            einladungToUpdate.setNaechsteStation(saveJson.getNaechsteStation());
            einladungToUpdate.setNaechsteStationSeit(appClock.localDateTime().now());
            einladungService.persist(einladungToUpdate);
        }
        UntersuchungTriage triage = untersuchung.getUntersuchungTriage();
        UntersuchungTriage entity = toEntity(untersuchung, triage, saveJson);

        return toJson(untersuchungTriageService.persistWithOptimisticLocking(entity, saveJson.getVersion()));
    }
}
