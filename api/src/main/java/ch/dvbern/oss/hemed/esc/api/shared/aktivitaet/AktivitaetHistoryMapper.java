package ch.dvbern.oss.hemed.esc.api.shared.aktivitaet;

import java.time.LocalDate;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassMapper;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindMapper;
import ch.dvbern.oss.hemed.esc.api.shared.aktivitaet.json.AnlassAktivitaetHistoryJson;
import ch.dvbern.oss.hemed.esc.api.shared.aktivitaet.json.KindAktivitaetHistoryJson;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@JaxMapper
@RequiredArgsConstructor
public class AktivitaetHistoryMapper {
    private final AktivitaetHistoryService aktivitaetHistoryService;
    private final AppClock appClock;

    @NonNull
    public static KindAktivitaetHistoryJson toKindAktivitaetJson(
        @NonNull KindAktivitaetHistory kindAktivitaetHistory,
        @NonNull LocalDate now) {
        return new KindAktivitaetHistoryJson(
            kindAktivitaetHistory.getId(),
            EntityInfoJson.fromEntity(kindAktivitaetHistory),
            KindMapper.toJson(kindAktivitaetHistory.getKind(), now)
        );
    }

    @NonNull
    public static AnlassAktivitaetHistoryJson toAnlassAktivitaetJson(
        @NonNull AnlassAktivitaetHistory anlassAktivitaetHistory) {
        return new AnlassAktivitaetHistoryJson(
            anlassAktivitaetHistory.getId(),
            EntityInfoJson.fromEntity(anlassAktivitaetHistory),
            AnlassMapper.toJson(anlassAktivitaetHistory.getAnlass())
        );
    }

    @NonNull
    public List<KindAktivitaetHistoryJson> findLetzteKinder(@NonNull ID<Benutzer> benutzerId) {
        LocalDate now = appClock.offsetDateTime().now().toLocalDate();
        return aktivitaetHistoryService.findAllKindAktivitaetenForBenutzer(benutzerId)
            .stream()
            .map(aktivitaet -> AktivitaetHistoryMapper.toKindAktivitaetJson(aktivitaet, now))
            .toList();
    }

    @NonNull
    public List<AnlassAktivitaetHistoryJson> findLetzteAnlaesse(@NonNull ID<Benutzer> benutzerId) {
        return aktivitaetHistoryService.findAllAnlassAktivitaetenForBenutzer(benutzerId)
            .stream()
            .map(AktivitaetHistoryMapper::toAnlassAktivitaetJson)
            .toList();
    }
}
