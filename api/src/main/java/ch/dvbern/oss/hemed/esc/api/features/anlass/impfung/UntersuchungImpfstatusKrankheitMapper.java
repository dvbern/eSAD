package ch.dvbern.oss.hemed.esc.api.features.anlass.impfung;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfstatusKrankheitSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfstatusListSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@RequiredArgsConstructor
public final class UntersuchungImpfstatusKrankheitMapper {

    private final UntersuchungService untersuchungService;
    private final UntersuchungImpfungenService untersuchungImpfungenService;
    private final UntersuchungImpfstatusKrankheitService untersuchungImpfstatusKrankheitService;
    private final EinladungService einladungService;
    private final UntersuchungImpfungInternService untersuchungImpfungInternService;
    private final AppClock appClock;

    @NonNull
    private static List<UntersuchungImpfstatusKrankheit> completeListFromExistingForEachKrankheit(
        @NonNull UntersuchungImpfungen impfungen,
        @NonNull List<UntersuchungImpfstatusKrankheit> existingImpfstatusList,
        @NonNull List<UntersuchungImpfstatusKrankheit> impfstatusLastAnlass,
        @NonNull List<UntersuchungImpfungIntern> impfungenInternListLastAnlass
    ) {
        List<UntersuchungImpfstatusKrankheit> completeImpfstatusList = new ArrayList<>();
        // Crete new or copy existing

        List<Krankheit> uebernehmbareKrankheiten = List.of(
            Krankheit.MASERN,
            Krankheit.MUMPS,
            Krankheit.ROETELN,
            Krankheit.VARIZELLEN,
            Krankheit.HPV,
            Krankheit.HEPATITIS_B);

        for (Krankheit krankheit : Krankheit.values()) {
            if (!existingImpfstatusList.isEmpty()) {
                UntersuchungImpfstatusKrankheit existing = existingImpfstatusList.stream()
                    .filter(impfstatus -> impfstatus.getKrankheit() == krankheit)
                    .findFirst()
                    .orElse(UntersuchungImpfstatusKrankheit.forKrankheit(impfungen, krankheit));

                completeImpfstatusList.add(existing);
            } else {
                // Falls beim letzten Anlass der Impfschutz als genügend eingestuft worden ist, werden die Werte
                // übernommen. Ansonsten werden leere Einträge erstellt.
                UntersuchungImpfstatusKrankheit newImpfung =
                    UntersuchungImpfstatusKrankheit.forKrankheit(impfungen, krankheit);

                // initialized with version zero but not yet persisted
                // avoid creating the version zero when actually persisting
                newImpfung.setVersion(-1L);

                if (uebernehmbareKrankheiten.contains(krankheit)) {
                    setUebernehmbareImpfstatus(impfstatusLastAnlass, impfungenInternListLastAnlass, krankheit, newImpfung);
                }
                completeImpfstatusList.add(newImpfung);
            }
        }
        return completeImpfstatusList;
    }

    private static void setUebernehmbareImpfstatus(
        @NonNull List<UntersuchungImpfstatusKrankheit> impfstatusLastAnlass,
        @NonNull List<UntersuchungImpfungIntern> impfungenInternListLastAnlass,
        @NonNull Krankheit krankheit,
        @NonNull UntersuchungImpfstatusKrankheit newImpfung
    ) {
        Optional<UntersuchungImpfstatusKrankheit> oldImpfstatus = impfstatusLastAnlass.stream()
            .filter(impfstatus -> impfstatus.getKrankheit() == krankheit
                && impfstatus.isImpfschutzGegeben())
            .findFirst();

        if (oldImpfstatus.isPresent()) {
            newImpfung.setAnzahlImpfungen(oldImpfstatus.get().getAnzahlImpfungen());
            newImpfung.setImpfschutzGegeben(oldImpfstatus.get().isImpfschutzGegeben());
            newImpfung.setDatumLetzteImpfung(oldImpfstatus.get().getDatumLetzteImpfung());
        }

        // Falls in der Impfliste des letzten Anlasses bei einer Krankheit der Impfschutz als genügend
        // eingestuft worden ist, werden diese Werte übernommen.

        // Für die Krankheit werden sämtliche Impfungen des letzten Anlasses gesucht.
        Optional<UntersuchungImpfungIntern> impfungInternLastAnlassOptional = impfungenInternListLastAnlass.stream()
            .filter(impfungLastAnlass -> impfungLastAnlass.getImpfstoff().getKrankheiten().contains(krankheit)
                && isImpfungFromLastAnlass(impfungLastAnlass)
                && impfungLastAnlass.getKrankheitenImmunitaeten()
                .stream()
                .anyMatch(ki -> ki.getKrankheit() == krankheit && ki.isImmunitaetGenuegend()))
            .findFirst();

        if (impfungInternLastAnlassOptional.isPresent()) {
            int bisherigeAnzahlImpfungen = impfstatusLastAnlass.stream()
                .filter(impfstatus -> impfstatus.getKrankheit() == krankheit)
                .findFirst()
                .map(UntersuchungImpfstatusKrankheit::getAnzahlImpfungen)
                .orElse(0);

            newImpfung.setImpfschutzGegeben(true);
            newImpfung.setAnzahlImpfungen(bisherigeAnzahlImpfungen + 1);
            newImpfung.setDatumLetzteImpfung(impfungInternLastAnlassOptional.get().getDatumImpfung());
        }
    }

    private static boolean isImpfungFromLastAnlass(@NonNull UntersuchungImpfungIntern impfungLastAnlass) {
        return impfungLastAnlass.getAnlassImpfungDurchgefuehrt() != null
            && impfungLastAnlass.getUntersuchungImpfungen().getUntersuchung().getEinladung().getAnlass().getId()
            .equals(impfungLastAnlass.getAnlassImpfungDurchgefuehrt().getId());
    }

    @Nullable
    public static UntersuchungImpfstatusKrankheitSaveJson toJson(
        @Nullable UntersuchungImpfstatusKrankheit impfstatusKrankheit) {
        if (impfstatusKrankheit == null) {
            return null;
        }
        return new UntersuchungImpfstatusKrankheitSaveJson(
            impfstatusKrankheit.getKrankheit(),
            impfstatusKrankheit.getAnzahlImpfungen(),
            impfstatusKrankheit.getDatumLetzteImpfung(),
            impfstatusKrankheit.isImpfschutzGegeben(),
            impfstatusKrankheit.isImpfempfehlung(),
            impfstatusKrankheit.isImpfentscheid(),
            impfstatusKrankheit.getVersion()
        );
    }

    @NonNull
    private UntersuchungImpfstatusKrankheit toEntity(
        @NonNull ID<Einladung> einladungID,
        @Nullable UntersuchungImpfstatusKrankheit impfstatusKrankheit,
        @NonNull UntersuchungImpfstatusKrankheitSaveJson saveJson) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungID);
        UntersuchungImpfungen impfungen =
            untersuchungImpfungenService.getOrCreateUntersuchungImpfungen(untersuchung.getId());
        if (impfstatusKrankheit == null) {
            impfstatusKrankheit = new UntersuchungImpfstatusKrankheit(
                impfungen,
                saveJson.getKrankheit(),
                saveJson.getAnzahlImpfungen(),
                saveJson.getDatumLetzteImpfung(),
                saveJson.isImpfschutzGegeben(),
                saveJson.isImpfempfehlung(),
                saveJson.isImpfentscheid(),
                appClock.offsetDateTime().now());
        } else {
            if (!Objects.equals(impfstatusKrankheit.getAnzahlImpfungen(), saveJson.getAnzahlImpfungen())
            || !Objects.equals(impfstatusKrankheit.getDatumLetzteImpfung(), saveJson.getDatumLetzteImpfung())
            || !Objects.equals(impfstatusKrankheit.isImpfschutzGegeben(), saveJson.isImpfschutzGegeben())
            || !Objects.equals(impfstatusKrankheit.isImpfempfehlung(), saveJson.isImpfentscheid())
            || !Objects.equals(impfstatusKrankheit.isImpfentscheid(), saveJson.isImpfentscheid())
            ) {
                impfstatusKrankheit.setTimestampLastManualChange(appClock.offsetDateTime().now());
            }
            impfstatusKrankheit.setAnzahlImpfungen(saveJson.getAnzahlImpfungen());
            impfstatusKrankheit.setDatumLetzteImpfung(saveJson.getDatumLetzteImpfung());
            impfstatusKrankheit.setImpfschutzGegeben(saveJson.isImpfschutzGegeben());
            impfstatusKrankheit.setImpfempfehlung(saveJson.isImpfempfehlung());
            impfstatusKrankheit.setImpfentscheid(saveJson.isImpfentscheid());
        }
        return impfstatusKrankheit;
    }

    @NonNull
    public List<UntersuchungImpfstatusKrankheitSaveJson> getByUntersuchungImpfungen(
        @NonNull UntersuchungImpfungen impfungen
    ) {
        List<UntersuchungImpfstatusKrankheit> existingImpfstatus =
            untersuchungImpfstatusKrankheitService.findByUntersuchung(impfungen.getUntersuchung().getId());

        Optional<Einladung> lastEinladung =
            einladungService.findLastEinladungForKind(
                impfungen.getUntersuchung().getEinladung().getAnlass(),
                impfungen.getUntersuchung().getEinladung().getDossier().getKind().getId());

        List<UntersuchungImpfstatusKrankheit> impfstatusLastAnlass = Collections.emptyList();
        List<UntersuchungImpfungIntern> impfungenInternListLastAnlass = Collections.emptyList();
        if (lastEinladung.isPresent() && lastEinladung.get().getUntersuchung() != null) {
            impfstatusLastAnlass = untersuchungImpfstatusKrankheitService.findByUntersuchung(lastEinladung.get()
                .getUntersuchung()
                .getId());
            impfungenInternListLastAnlass =
                untersuchungImpfungInternService.findByUntersuchung(lastEinladung.get().getUntersuchung().getId());
        }

        List<UntersuchungImpfstatusKrankheit> completeImpfstatus = completeListFromExistingForEachKrankheit(
            impfungen,
            existingImpfstatus,
            impfstatusLastAnlass,
            impfungenInternListLastAnlass);
        return completeImpfstatus.stream().map(UntersuchungImpfstatusKrankheitMapper::toJson).toList();
    }

    @NonNull
    public List<UntersuchungImpfstatusKrankheitSaveJson> getByEinladung(@NonNull ID<Einladung> einladungId) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungId);
        UntersuchungImpfungen impfungen =
            untersuchungImpfungenService.getOrCreateUntersuchungImpfungen(untersuchung.getId());
        return getByUntersuchungImpfungen(impfungen);
    }

    public void persistAll(
        @NonNull ID<Einladung> einladungID,
        @NonNull UntersuchungImpfstatusListSaveJson saveJsonList
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungID);
        UntersuchungImpfungen impfungen =
            untersuchungImpfungenService.getOrCreateUntersuchungImpfungen(untersuchung.getId());
        for (UntersuchungImpfstatusKrankheitSaveJson statusJson : saveJsonList.getImpfstatusList()) {
            Optional<UntersuchungImpfstatusKrankheit> existing =
                untersuchungImpfstatusKrankheitService.findByUntersuchungAndKrankheit(
                    impfungen.getId(),
                    statusJson.getKrankheit());
            UntersuchungImpfstatusKrankheit entity = toEntity(einladungID, existing.orElse(null), statusJson);
            untersuchungImpfstatusKrankheitService.persistWithOptimisticLocking(entity, statusJson.getVersion());
        }
    }
}
