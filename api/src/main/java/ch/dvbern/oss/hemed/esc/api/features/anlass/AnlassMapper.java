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

package ch.dvbern.oss.hemed.esc.api.features.anlass;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfigMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfigService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.AnlassJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.AnlassListJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.AnlassOptionJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.AnlassSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.AnlassWithDateTimeJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.AnlassWithDateTimeListJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.AnlassWithOpenSlotsListJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.AnlassWithOpenSlotsOptionJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerMapper;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.SchuleMapper;
import ch.dvbern.oss.hemed.esc.api.features.schule.json.SchulInformationenJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.LehrpersonMapper;
import ch.dvbern.oss.hemed.esc.api.shared.aktivitaet.AktivitaetHistoryService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.ZustaendigePersonService;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import ch.dvbern.oss.hemed.esc.api.shared.types.DateTimeRange;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@Slf4j
@JaxMapper
@RequiredArgsConstructor
public final class AnlassMapper {

    private final AnlassService anlassService;
    private final AnlassConfigService anlassConfigService;
    private final BenutzerService benutzerService;
    private final LehrpersonMapper lehrpersonMapper;
    private final AktivitaetHistoryService aktivitaetHistoryService;
    private final TenantSettingsService tenantSettingsService;
    private final ZustaendigePersonService zustaendigePersonService;
    private final TenantProducer tenantProducer;

    @NonNull
    public static AnlassJson toJson(@NonNull Anlass anlass, @NonNull TenantEnum tenant) {
        Benutzer arzt = anlass.getArzt();
        Benutzer mpa = anlass.getMpa();
        Benutzer weiterePerson = anlass.getWeiterePerson();
        DateTimeRange zeitVonBis = anlass.getZeitVonBis();
        return new AnlassJson(
            anlass.getId(),
            EntityInfoJson.fromEntity(anlass),
            new AnlassSaveJson(
                anlass.getAnlassConfig().getAnlassTyp(),
                AnlassConfigMapper.toOptionJson(anlass.getAnlassConfig()),
                anlass.getTitel(),
                anlass.getAnlassStatus(),
                anlass.getDatum(),
                anlass.getSchuljahr(),
                zeitVonBis != null ? zeitVonBis.getVon() : null,
                zeitVonBis != null ? zeitVonBis.getBis() : null,
                anlass.getAnlassArt(),
                arzt != null ? BenutzerMapper.toOptionJson(arzt, tenant) : null,
                anlass.getArztInfo(),
                mpa != null ? BenutzerMapper.toOptionJson(mpa, tenant) : null,
                anlass.getMpaInfo(),
                weiterePerson != null ? BenutzerMapper.toOptionJson(weiterePerson, tenant) : null,
                anlass.getWeiterePersonInfo(),
                anlass.getVersion()
            ),
            AnlassConfigMapper.toJson(anlass.getAnlassConfig(), anlass.getDatum())
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
        AnlassConfig config = anlassConfigService.getById(saveJson.getAnlassConfigOption().getId());
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
        // Arzt und MPA werden separat behandelt
        existingAnlass.setArztInfo(saveJson.getArztInfo());
        existingAnlass.setMpaInfo(saveJson.getMpaInfo());
        Benutzer weiterePerson =
            saveJson.getWeiterePerson() != null ? benutzerService.getById(saveJson.getWeiterePerson().getId()) : null;
        existingAnlass.setWeiterePerson(weiterePerson);
        existingAnlass.setWeiterePersonInfo(saveJson.getWeiterePersonInfo());
        return existingAnlass;
    }

    @NonNull
    public List<AnlassJson> findAllAnlaesseBerechtigt(@NonNull Predicate<Anlass> hasReadAuthorizationAnlass) {
        TenantEnum tenant = tenantProducer.produceTenant();
        return anlassService.findAll().stream()
            .filter(anlass -> !anlass.getAnlassConfig().getAnlassTyp().isAnonym())
            // some Roles can only read Anlässe of a certain AnlassTyp
            .filter(hasReadAuthorizationAnlass)
            .map(a -> AnlassMapper.toJson(a, tenant))
            .toList();
    }

    @NonNull
    public AnlassJson getById(@NonNull ID<Anlass> anlassID) {
        Anlass anlass = anlassService.getById(anlassID);
        aktivitaetHistoryService.createAktivitaetAnlass(anlass);
        return toJson(anlass, tenantProducer.produceTenant());
    }

    @NonNull
    public AnlassJson create(@NonNull AnlassSaveJson saveJson) {
        Anlass entity = toEntityForCreate(saveJson);
        return toJson(anlassService.persist(entity), tenantProducer.produceTenant());
    }

    @NonNull
    public AnlassJson update(@NonNull ID<Anlass> anlassID, @NonNull AnlassSaveJson saveJson) {
        Anlass existingAnlass = anlassService.getById(anlassID);
        Anlass entity = toEntity(existingAnlass, saveJson);
        Anlass updatedAnlass = anlassService.persistWithOptimisticLocking(entity, saveJson.getVersion());

        // https://intra.dvbern.ch/spaces/HEM/pages/284263663/Zust%C3%A4ndigkeiten+Archivierung
        // Change Zustaendigkeiten: Fall 4: Neue Verantwortliche auf Anlass
        zustaendigePersonService.verantwortlicherChangedOnAnlass(
            updatedAnlass,
            saveJson.getArzt() != null ? saveJson.getArzt().getId() : null,
            saveJson.getMpa() != null ? saveJson.getMpa().getId() : null);

        return toJson(updatedAnlass, tenantProducer.produceTenant());
    }

    @NonNull
    public AnlassJson changeAnlassStatus(@NonNull ID<Anlass> anlassID, @NonNull AnlassStatus status) {
        Anlass entity = anlassService.changeAnlassStatus(anlassID, status);
        return toJson(entity, tenantProducer.produceTenant());
    }

    public void remove(@NonNull ID<Anlass> anlassID) {
        anlassService.remove(anlassID);
    }

    @NonNull
    public AnlassListJson findAnlaesseForDashboard(
        @NonNull ID<Benutzer> benutzerID,
        @NonNull Predicate<Anlass> hasReadAuthorizationAnlass
    ) {
        List<AnlassJson> list = anlassService.findAllForUserInFuture(benutzerID).stream()
            .filter(anlass -> !anlass.getAnlassConfig().getAnlassTyp().isAnonym())
            .filter(hasReadAuthorizationAnlass)
            .map(a -> AnlassMapper.toJson(a, tenantProducer.produceTenant()))
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

    @NonNull
    public AnlassWithOpenSlotsListJson searchAnlassTermineWithAvailableSlots(
        @NonNull List<AnlassTyp> anlassTypen,
        @NonNull ID<Kind> kindId,
        @NonNull Predicate<AnlassWithOpenSlotsOptionJson> hasReadAuthorizationAnlass
    ) {
        int maxEinladungenPerAnlass = this.tenantSettingsService.getTenantSetting().getFolgeTermineMaxSlots();

        List<AnlassWithOpenSlotsOptionJson> options =
            anlassService.findFutureGruppenAnlaesseOfTypeHavingLessThanMaxEinladungen(
                    anlassTypen,
                    kindId,
                    maxEinladungenPerAnlass)
                .stream()
                .filter(hasReadAuthorizationAnlass)
                .toList();

        return new AnlassWithOpenSlotsListJson(options, maxEinladungenPerAnlass);
    }

    @NonNull
    public AnlassWithDateTimeListJson findScheduledAnlaesseOfTypeForKind(
        @NonNull List<AnlassTyp> anlassTypen,
        @NonNull ID<Kind> kindId,
        @NonNull List<ID<Anlass>> excludeAnlassIds,
        @NonNull Predicate<AnlassWithDateTimeJson> hasReadAuthorizationAnlass
    ) {
        List<AnlassWithDateTimeJson> kindAnlaesse =
            anlassService.findScheduledAnlaesseOfTypeForKind(anlassTypen, kindId, excludeAnlassIds)
                .stream()
                .filter(hasReadAuthorizationAnlass)
                .toList();

        return new AnlassWithDateTimeListJson(kindAnlaesse);
    }
}
