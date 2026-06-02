package ch.dvbern.oss.hemed.esc.api.features.anlass.einladung;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.json.EinladungSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.types.EinladungStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.types.NaechsteStation;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.DossierService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.Aufgabe;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.AufgabeService;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.AutoAufgabeService;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.notizen.AutoNotizService;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import ch.dvbern.oss.hemed.esc.api.shared.types.DateRange;
import ch.dvbern.oss.hemed.esc.api.shared.util.KindValidationUtil;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EinladungService {

    private final EinladungRepo einladungRepo;
    private final DossierService dossierService;
    private final UntersuchungService untersuchungService;
    private final AutoAufgabeService autoAufgabeService;
    private final AufgabeService aufgabeService;
    private final AutoNotizService autoNotizService;
    private final AnlassService anlassService;
    private final AppClock appClock;
    private final TenantSettingsService tenantSettingsService;
    private final ConfigService configService;

    @NonNull
    public Einladung persist(@NonNull Einladung einladung) {
        einladungRepo.persist(einladung);
        return einladung;
    }

    @NonNull
    public Einladung persistWithOptimisticLocking(@NonNull Einladung entity, @NonNull Long versionFromFrontend) {
        return einladungRepo.persistWithOptimisticLocking(entity, versionFromFrontend);
    }

    @NonNull
    public Einladung getById(@NonNull ID<Einladung> einladungID) {
        return einladungRepo.getByIdWithNotizen(einladungID);
    }

    @NonNull
    public Map<ID<?>, ID<Anlass>> getAnlassEinladungsMap(@NonNull List<? extends ID<?>> einladungIds) {
        return einladungRepo.getAnlassEinladungsMap(einladungIds);
    }

    public Einladung getByIdLockedQuery(@NonNull ID<Einladung> einladungId) {
        return einladungRepo.getByIdLockedQuery(einladungId);
    }

    @NonNull
    public List<Einladung> findByAnlass(@NonNull ID<Anlass> anlassID) {
        return einladungRepo.findByAnlass(anlassID);
    }

    public Optional<Einladung> findByAnlassUndKind(@NonNull ID<Anlass> anlassID, @NonNull ID<Kind> kindId) {
        return einladungRepo.findByAnlassUndKind(anlassID, kindId);
    }

    public void createUpdateOrDeleteAufgabeNachzuegler(EinladungSaveJson einladungSaveJson, Einladung existing) {
        if (einladungSaveJson.getEinladungStatus() != EinladungStatus.NACHZUEGLER
            && existing.getEinladungStatus() == EinladungStatus.NACHZUEGLER) {
            Optional<Aufgabe> nachzueglerAufgabeOptional = aufgabeService.findSingleAufgabeForKindWithLabel(
                existing.getDossier().getKind(),
                Label.ID_LABEL_NACHZUEGLER);
            nachzueglerAufgabeOptional.ifPresent(autoAufgabeService::aufgabeAbschliessen);
        }
        if (einladungSaveJson.getEinladungStatus() == EinladungStatus.NACHZUEGLER
            && existing.getEinladungStatus() != EinladungStatus.NACHZUEGLER) {
            autoAufgabeService.createOrUpdateAufgabeEinladungStatusNachzuegler(existing);
        }
    }

    @NonNull
    public List<Einladung> createEinladungenForAnlass(
        @NonNull Anlass anlass,
        @NonNull List<ID<Kind>> kindIdList
    ) {
        List<Einladung> createdEinladungen = new ArrayList<>();
        for (ID<Kind> kindId : kindIdList) {
            Dossier dossier = dossierService.getOrCreateByKind(kindId);
            Kind kind = dossier.getKind();
            KindValidationUtil.throwErrorIfKindIsInactive(
                kind,
                AppValidationMessage.cannotCreateEinladungForInactiveKind(
                    kind,
                    anlass.getId()));
            Optional<Einladung> einladungOptional = einladungRepo.findByAnlassUndKind(anlass.getId(), kindId);
            if (einladungOptional.isEmpty()) {
                Einladung einladung = persist(Einladung.create(anlass, dossier));
                einladung.disableOptimisticLockingCheck();
                Optional<Einladung>
                    urspruenglicheEinladungForNachzuegler =
                    findUrspruenglicheEinladungForNachzuegler(anlass, dossier.getKind().getId());
                if (urspruenglicheEinladungForNachzuegler.isPresent()) {
                    Einladung einladungNachzuegler = urspruenglicheEinladungForNachzuegler.get();
                    einladungNachzuegler.disableOptimisticLockingCheck();
                    autoNotizService.moveImpfausweisNotiz(einladungNachzuegler, einladung);
                    autoNotizService.moveFragebogenNotiz(einladungNachzuegler, einladung);
                    autoNotizService.moveAnmeldungBestaetigungNotiz(einladungNachzuegler, einladung);
                    autoNotizService.moveImpfeinverstaendnisNotiz(einladungNachzuegler, einladung);
                }
                createdEinladungen.add(einladung);
            }
        }
        return createdEinladungen;
    }

    public void removeEinladungen(
        @NonNull List<ID<Einladung>> einladungIdList
    ) {
        for (ID<Einladung> einladungID : einladungIdList) {
            removeEinladung(einladungID, false);
        }
    }

    public void removeEinladung(@NonNull ID<Einladung> einladungID, boolean deleteAnlassIfLastEinladung) {
        Einladung einladung = einladungRepo.getById(einladungID);
        if (einladung.getUntersuchung() != null) {
            untersuchungService.removeUntersuchung(einladung.getUntersuchung());
            einladung.disableOptimisticLockingCheck();
            einladung.setUntersuchung(null);
            einladungRepo.persist(einladung);
        }

        List<Einladung> einladungenAnlass = einladungRepo.findByAnlass(einladung.getAnlass().getId());
        // Wenn es die einzige Einladung in diesem Anlass ist, kann gleich der ganze Anlass gelöscht werden.
        if (einladungenAnlass.size() == 1 && deleteAnlassIfLastEinladung) {
            einladungRepo.remove(einladungID);
            anlassService.remove(einladung.getAnlass().getId());
        } else {
            einladungRepo.remove(einladungID);
        }
    }

    @NonNull
    public List<Einladung> getUntersuchungTabInfos(@NonNull List<ID<Einladung>> einladungIds) {
        return einladungRepo.getUntersuchungTabInfos(einladungIds);
    }

    @NonNull
    public List<Einladung> getHiddenUntersuchungTabInfos(
        @NonNull ID<Anlass> anlassId,
        @NonNull List<ID<Einladung>> einladungIds
    ) {
        List<Einladung> allEinladungen = einladungRepo.findByAnlass(anlassId);
        return allEinladungen.stream().filter(einladung -> !einladungIds.contains(einladung.getId())).toList();
    }

    @NonNull
    public Optional<Einladung> findUrspruenglicheEinladungForNachzuegler(
        @NonNull Anlass currentAnlass,
        @NonNull ID<Kind> kindID
    ) {
        return einladungRepo.findUrspruenglicheEinladungForNachzuegler(currentAnlass, kindID);
    }

    @NonNull
    public Optional<Einladung> findLastEinladungForKind(
        @NonNull Anlass currentAnlass,
        @NonNull ID<Kind> kindID
    ) {
        return einladungRepo.findLastEinladungForKind(currentAnlass, kindID);
    }

    @NonNull
    public List<Einladung> findAllByDossier(@NonNull ID<Dossier> dossierId) {
        return einladungRepo.findAllByDossier(dossierId);
    }

    @NonNull
    public List<ID<Einladung>> findAllForSpssReport(@NonNull List<AnlassTyp> anlassTypList) {
        return einladungRepo.findAllForSpssReport(anlassTypList);
    }

    @NonNull
    public List<ID<Einladung>> findAllForCsvReport(@NonNull DateRange zeitraum) {
        return einladungRepo.findAllForCsvReport(zeitraum);
    }

    @NonNull
    public List<ID<Einladung>> findAllForCsvImpfstoffeReport(@NonNull DateRange zeitraum) {
        return einladungRepo.findAllForCsvImpfstoffReport(zeitraum);
    }

    public void markAsSpssExported(@NonNull List<ID<Einladung>> einladungIdList) {
        einladungRepo.markAsSpssExported(einladungIdList);
    }

    public void untersuchungAbschliessen(@NotNull @NonNull ID<Einladung> einladungID) {
        // Untersuchungen abschliessen
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungID);
        untersuchungService.untersuchungAbschliessen(untersuchung);

        // Einladung auf FERTIG setzen
        Einladung einladung = getById(einladungID);
        einladung.setNaechsteStation(NaechsteStation.FERTIG);
        einladung.setNaechsteStationSeit(appClock.localDateTime().now());
        persist(einladung);
    }
}
