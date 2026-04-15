package ch.dvbern.oss.hemed.esc.api.features.anlass.impfung;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfungInternJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfungInternSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerMapper;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.Impfstoff;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.ImpfstoffCharge;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.ImpfstoffChargeService;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.ImpfstoffMapper;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.ImpfstoffService;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@RequiredArgsConstructor
public final class UntersuchungImpfungInternMapper {

    private final UntersuchungService untersuchungService;
    private final UntersuchungImpfungenService untersuchungImpfungenService;
    private final UntersuchungImpfungInternService untersuchungImpfungInternService;
    private final ImpfstoffService impfstoffService;
    private final ImpfstoffChargeService impfstoffChargeService;
    private final BenutzerService benutzerService;
    private final AppClock appClock;

    @Nullable
    public static UntersuchungImpfungInternJson toJson(@Nullable UntersuchungImpfungIntern impfung) {
        if (impfung == null) {
            return null;
        }
        return new UntersuchungImpfungInternJson(
            impfung.getId(),
            EntityInfoJson.fromEntity(impfung),
            new UntersuchungImpfungInternSaveJson(
                ImpfstoffMapper.toJson(impfung.getImpfstoff()),
                impfung.getCharge() != null ? impfung.getCharge().getName() : null,
                impfung.getDatumImpfung(),
                impfung.getUserDurchgefuehrt() != null ?
                    BenutzerMapper.toOptionJson(impfung.getUserDurchgefuehrt()) :
                    null,
                impfung.getImpfStatus(),
                impfung.getGrundNichtDurchfuehrung(),
                impfung.getKrankheitenImmunitaeten()
                    .stream()
                    .map(UntersuchungImpfungKrankheitImmunitaetMapper::toJson)
                    .toList(),
                impfung.getVersion()
            ),
            BenutzerMapper.toOptionJson(impfung.getUserErfasst()),
            impfung.getAnlassImpfungDurchgefuehrt() != null ? impfung.getAnlassImpfungDurchgefuehrt().getId() : null
        );
    }

    @NonNull
    private UntersuchungImpfungIntern toEntity(
        @NonNull ID<Einladung> einladungID,
        @Nullable UntersuchungImpfungIntern existingImpfungIntern,
        @NonNull UntersuchungImpfungInternSaveJson saveJson) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungID);
        UntersuchungImpfungen impfungen =
            untersuchungImpfungenService.getOrCreateUntersuchungImpfungen(untersuchung.getId());
        Impfstoff impfstoff = impfstoffService.getById(saveJson.getImpfstoff().getId());
        ImpfstoffCharge charge =
            impfstoffChargeService.getChargeOrCreateNewIfFeatureEnabled(saveJson.getCharge(), impfstoff);
        Benutzer userErfasst = benutzerService.getCurrentBenutzerOrThrowException();
        Benutzer userDurchgefuehrt = saveJson.getUserDurchgefuehrt() != null ?
            benutzerService.getById(saveJson.getUserDurchgefuehrt().getId()) :
            null;
        if (existingImpfungIntern == null) {
            existingImpfungIntern = new UntersuchungImpfungIntern(
                impfungen,
                impfstoff,
                charge,
                saveJson.getDatumImpfung(),
                userErfasst,
                userDurchgefuehrt,
                saveJson.getImpfStatus(),
                saveJson.getGrundNichtDurchfuehrung(),
                new HashSet<>(),
                // Wird separat behandelt
                untersuchung.getEinladung().getAnlass(),
                null,
                appClock.offsetDateTime().now());
        } else {
            if (!Objects.equals(existingImpfungIntern.getImpfstoff(), impfstoff)
                || !Objects.equals(existingImpfungIntern.getCharge(), charge)
                || !Objects.equals(existingImpfungIntern.getDatumImpfung(), saveJson.getDatumImpfung())
                || existingImpfungIntern.getImpfStatus() != saveJson.getImpfStatus()
                || !Objects.equals(
                existingImpfungIntern.getGrundNichtDurchfuehrung(),
                saveJson.getGrundNichtDurchfuehrung())
            ) {
                existingImpfungIntern.setTimestampLastManualChange(appClock.offsetDateTime().now());
            }
            existingImpfungIntern.setImpfstoff(impfstoff);
            existingImpfungIntern.setCharge(charge);
            existingImpfungIntern.setDatumImpfung(saveJson.getDatumImpfung());
            existingImpfungIntern.setUserDurchgefuehrt(userDurchgefuehrt);
            if (existingImpfungIntern.getImpfStatus() == ImpfStatus.GEPLANT
                && saveJson.getImpfStatus() == ImpfStatus.DURCHGEFUEHRT) {
                existingImpfungIntern.setAnlassImpfungDurchgefuehrt(untersuchung.getEinladung().getAnlass());
            }
            existingImpfungIntern.setImpfStatus(saveJson.getImpfStatus());
            existingImpfungIntern.setGrundNichtDurchfuehrung(saveJson.getGrundNichtDurchfuehrung());
            // KrankheitenImmunitaeten werden separat behandelt
        }
        return existingImpfungIntern;
    }

    @NonNull
    public List<UntersuchungImpfungInternJson> getByUntersuchungImpfungen(@NonNull UntersuchungImpfungen impfungen) {
        List<UntersuchungImpfungIntern> impfungenIntern = untersuchungImpfungInternService.findByUntersuchung(impfungen.getUntersuchung().getId());
        // Eigentlich sollten die Impfstatus beim Erstellen der Untersuchung erstellt werden. Bei schon bestehenden Untersuchungen erstellen wir sie hier noch.
        if (impfungenIntern.isEmpty()) {
            untersuchungService.createCompleteImpfungenInternBasedOnLastAnlass(impfungen);
            impfungenIntern = untersuchungImpfungInternService.findByUntersuchung(impfungen.getUntersuchung().getId());
        }
        return impfungenIntern.stream().
            map(UntersuchungImpfungInternMapper::toJson).
            toList();
    }

    @NonNull
    public List<UntersuchungImpfungInternJson> findByEinladung(@NonNull ID<Einladung> einladungId) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungId);
        // Sicherstellen, dass das UntersuchungImpfungen Objekt existiert
        UntersuchungImpfungen impfungen =
            untersuchungImpfungenService.getOrCreateUntersuchungImpfungen(untersuchung.getId());
        return getByUntersuchungImpfungen(impfungen);
    }

    @NonNull
    public UntersuchungImpfungInternJson create(
        @NonNull ID<Einladung> einladungId,
        @NonNull UntersuchungImpfungInternSaveJson saveJson) {
        return save(einladungId, null, saveJson);
    }

    @NonNull
    public UntersuchungImpfungInternJson update(
        @NonNull ID<Einladung> einladungId,
        @NonNull ID<UntersuchungImpfungIntern> impfungInternId,
        @NonNull UntersuchungImpfungInternSaveJson saveJson
    ) {
        UntersuchungImpfungIntern existingImpfung = untersuchungImpfungInternService.getById(impfungInternId);
        return save(einladungId, existingImpfung, saveJson);
    }

    @NonNull
    private UntersuchungImpfungInternJson save(
        @NonNull ID<Einladung> einladungId,
        @Nullable UntersuchungImpfungIntern existingImpfung,
        @NonNull UntersuchungImpfungInternSaveJson saveJson
    ) {
        UntersuchungImpfungIntern entity = toEntity(einladungId, existingImpfung, saveJson);
        replaceKrankheitImmunitaeten(entity, saveJson);
        return toJson(untersuchungImpfungInternService.persistWithOptimisticLocking(entity, saveJson.getVersion()));
    }

    private void replaceKrankheitImmunitaeten(
        @NonNull UntersuchungImpfungIntern impfung,
        @NonNull UntersuchungImpfungInternSaveJson saveJson
    ) {
        Set<UntersuchungImpfungKrankheitImmunitaet> desiredImmunitaeten = saveJson.getKrankheitenImmunitaeten()
            .stream()
            .map(untersuchungImpfungKrankheitImmunitaetJson -> UntersuchungImpfungKrankheitImmunitaetMapper.toEntity(
                impfung,
                untersuchungImpfungKrankheitImmunitaetJson))
            .collect(Collectors.toSet());
        impfung.getKrankheitenImmunitaeten().clear();
        impfung.getKrankheitenImmunitaeten().addAll(desiredImmunitaeten);
    }

    public void remove(
        @NonNull ID<Anlass> anlassId,
        @NonNull ID<UntersuchungImpfungIntern> impfungInternId
    ) {
        untersuchungImpfungInternService.remove(anlassId, impfungInternId);
    }
}
