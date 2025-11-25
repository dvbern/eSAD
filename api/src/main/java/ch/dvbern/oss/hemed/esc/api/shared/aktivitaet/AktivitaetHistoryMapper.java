/*
 *
 * Copyright (C) 2025 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
import ch.dvbern.oss.hemed.esc.api.shared.auth.AuthorizerService;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@JaxMapper
@RequiredArgsConstructor
public class AktivitaetHistoryMapper {
    private final AktivitaetHistoryService aktivitaetHistoryService;
    private final AppClock appClock;
    private final AuthorizerService authorizerService;

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
    public AnlassAktivitaetHistoryJson toAnlassAktivitaetJson(
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
            .filter(aktivitaet -> authorizerService.hasReadAuthorizationAnlassListe(aktivitaet.getAnlass()))
            .map(this::toAnlassAktivitaetJson)
            .toList();
    }
}
