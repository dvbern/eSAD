package ch.dvbern.oss.hemed.esc.api.features.anlass.einladung;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.json.NaechsteStationJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheitService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungInternService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.kind.UntersuchungKindDatenMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.json.EinladungJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.json.EinladungListJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.json.EinladungSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.json.UntersuchungTabInfoJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.json.UntersuchungTabsSavedJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindMapper;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Nachkontrolle;
import ch.dvbern.oss.hemed.esc.api.features.kinder.NachkontrolleMapper;
import ch.dvbern.oss.hemed.esc.api.features.kinder.NachkontrolleService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.KindIdListJson;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizMapper;
import ch.dvbern.oss.hemed.esc.api.features.notizen.json.NotizJson;
import ch.dvbern.oss.hemed.esc.api.features.notizen.json.NotizSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.aktivitaet.AktivitaetHistoryService;
import ch.dvbern.oss.hemed.esc.api.shared.util.PathParamConsistancyUtil;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@JaxMapper
@RequiredArgsConstructor
public final class EinladungMapper {

    private final AnlassService anlassService;
    private final EinladungService einladungService;
    private final UntersuchungService untersuchungService;
    private final EinladungNotizMapper einladungNotizMapper;
    private final NachkontrolleService nachkontrolleService;
    private final AppClock appClock;
    private final AktivitaetHistoryService aktivitaetHistoryService;
    private final UntersuchungImpfstatusKrankheitService untersuchungImpfstatusKrankheitService;
    private final UntersuchungImpfungInternService untersuchungImpfungInternService;

    @NonNull
    public static EinladungJson toJson(
        @NonNull Einladung einladung,
        @Nullable Nachkontrolle nachkontrolle,
        @NonNull LocalDate now) {
        return new EinladungJson(
            einladung.getId(),
            EntityInfoJson.fromEntity(einladung),
            new EinladungSaveJson(
                einladung.getAnlass().getId(),
                einladung.getDossier().getKind().getId(),
                einladung.getEinladungStatus(),
                einladung.getGrundAbmeldung(),
                einladung.getArtAbmeldung(),
                einladung.getBemerkungAbmeldung(),
                einladung.getNotizen().stream().map(NotizMapper::toJson).toList(),
                einladung.getVersion(),
                einladung.getAbmeldungsNotiz().map(AbstractEntity::getVersion).orElse(-1L)
            ),
            KindMapper.toJson(einladung.getDossier().getKind(), now),
            NachkontrolleMapper.toJson(nachkontrolle),
            einladung.getUntersuchung() != null ?
                UntersuchungKindDatenMapper.toJson(einladung.getUntersuchung(), now) :
                null,
            new NaechsteStationJson(
                einladung.getNaechsteStation(),
                einladung.getNaechsteStationSeit()
            )
        );
    }

    @NonNull
    public UntersuchungTabInfoJson toTabInfoJson(@NonNull Einladung einladung) {
        return new UntersuchungTabInfoJson(
            einladung.getId(),
            einladung.getDossier().getKind().getNameVorname(),
            einladung.getDossier().getKind().getId()
        );
    }

    @NonNull
    public static UntersuchungTabsSavedJson toUntersuchungTabsSavedJson(
        @NonNull Untersuchung untersuchung,
        @NonNull List<UntersuchungImpfstatusKrankheit> impfstatus,
        @NonNull List<UntersuchungImpfungIntern> impfungenInternList
    ) {
        return new UntersuchungTabsSavedJson(
            untersuchung.getId(),
            untersuchung.getUntersuchungDurchfuehrung() != null,
            untersuchungImpfungenHasAnyValues(untersuchung.getUntersuchungImpfungen(), impfstatus, impfungenInternList),
            untersuchung.getUntersuchungSehenBeurteilung() != null,
            untersuchung.getUntersuchungHoerenAudiogramm() != null,
            untersuchung.getUntersuchungHoerenOtoskopie() != null,
            untersuchung.getUntersuchungGroesseGewicht() != null,
            untersuchung.getUntersuchungTriage() != null,
            untersuchung.getUntersuchungArztBeurteilung() != null,
            untersuchung.getUntersuchungMassnahmen() != null,
            untersuchung.getUntersuchungLaeusekontrolle() != null
        );
    }

    private static boolean untersuchungImpfungenHasAnyValues(
        @Nullable UntersuchungImpfungen untersuchungImpfungen,
        @NonNull List<UntersuchungImpfstatusKrankheit> impfstatus,
        @NonNull List<UntersuchungImpfungIntern> impfungenInternList
    ) {
        if (untersuchungImpfungen == null) {
            return false;
        }
        // Neu werden die Impfungen beim Erstellen der Untersuchung erstellt, deshalb zählt das Tab erst als bearbeitet wenn bei einem der Werte die Version > 0 ist.
        boolean hasImpfstatusNotInitial = impfstatus.stream().anyMatch(is -> is.getVersion() > 0);
        boolean hasImpfungInterNotInitial = impfungenInternList.stream().anyMatch(ii -> ii.getVersion() > 0);
        boolean hasRelevantCheckboxes = untersuchungImpfungen.isImpfausweisZweiPlusEinsSchema()
            || untersuchungImpfungen.isImpfausweisNichtVorhanden()
            || untersuchungImpfungen.isImpfausweisUnvollstaendig();
        boolean impfungMassnahmenPresent = untersuchungImpfungen.getUntersuchungImpfungenMassnahmen() != null;


        return hasImpfstatusNotInitial || hasImpfungInterNotInitial || hasRelevantCheckboxes || impfungMassnahmenPresent;
    }

    @NonNull
    private Einladung toEntity(
        @NonNull Einladung existing,
        @NonNull EinladungSaveJson saveJson
    ) {
        this.updateAbmeldungsNotiz(existing, saveJson);

        existing.setEinladungStatus(saveJson.getEinladungStatus());
        existing.setGrundAbmeldung(saveJson.getGrundAbmeldung());
        existing.setArtAbmeldung(saveJson.getArtAbmeldung());
        existing.setBemerkungAbmeldung(saveJson.getBemerkungAbmeldung());
        // Impfausweis wird separat gespeichert
        // Fragebogen wird separat gespeichert
        // AnmeldungBestätigung wird separat gespeichert

        return existing;
    }

    @NonNull
    public List<EinladungJson> findEinladungenByAnlass(
        @NonNull ID<Anlass> anlassId
    ) {
        LocalDate now = appClock.offsetDateTime().now().toLocalDate();
        return einladungService.findByAnlass(anlassId).stream()
            .map(einladung -> toJson(
                einladung,
                this.nachkontrolleService.getOrCreateByDossier(einladung.getDossier().getId()),
                now
            ))
            .toList();
    }

    @NonNull
    public EinladungJson getById(@NonNull ID<Einladung> einladungId) {
        Einladung einladung = this.einladungService.getById(einladungId);
        aktivitaetHistoryService.createAktivitaetKind(einladung.getDossier().getKind());
        Nachkontrolle nachkontrolle = this.nachkontrolleService.getOrCreateByDossier(einladung.getDossier().getId());
        LocalDate now = appClock.offsetDateTime().now().toLocalDate();
        return toJson(einladung, nachkontrolle, now);
    }

    @NonNull
    public UntersuchungTabsSavedJson getTabsSavedByEinladungId(
        @NonNull ID<Einladung> einladungId
    ) {
        Untersuchung untersuchung = this.untersuchungService.getOrCreateUntersuchung(einladungId);
        List<UntersuchungImpfstatusKrankheit> impfstatus =
            untersuchungImpfstatusKrankheitService.findByUntersuchung(untersuchung.getId());
        List<UntersuchungImpfungIntern> impfungenInternList =
            untersuchungImpfungInternService.findByUntersuchung(untersuchung.getId());
        return toUntersuchungTabsSavedJson(untersuchung, impfstatus, impfungenInternList);
    }

    @NonNull
    public List<EinladungJson> createEinladungenForAnlass(
        @NonNull ID<Anlass> anlassID,
        @NonNull KindIdListJson kindListJson
    ) {
        Anlass anlass = anlassService.getById(anlassID);
        LocalDate now = appClock.offsetDateTime().now().toLocalDate();
        return einladungService.createEinladungenForAnlass(anlass, kindListJson.getKinderIdList()).stream()
            .map(einladung -> toJson(
                einladung,
                this.nachkontrolleService.getOrCreateByDossier(einladung.getDossier().getId()),
                now
            ))
            .toList();
    }

    public void removeEinladungen(
        @NonNull EinladungListJson einladungListJson
    ) {
        List<ID<Einladung>> einladungIdList = einladungListJson.getEinladungList().stream()
            .map(EinladungJson::getId)
            .toList();
        einladungService.removeEinladungen(einladungIdList);
    }

    @NonNull
    public List<UntersuchungTabInfoJson> getUntersuchungTabInfos(
        @NonNull List<ID<Einladung>> einladungIds
    ) {
        return einladungService.getUntersuchungTabInfos(einladungIds).stream()
            .sorted(Comparator.comparing(o -> o.getDossier().getKind().getNameVorname()))
            .map(this::toTabInfoJson)
            .toList();
    }

    @NonNull
    public List<UntersuchungTabInfoJson> getHiddenUntersuchungTabInfos(
        @NonNull ID<Anlass> anlassId,
        @NonNull List<ID<Einladung>> einladungIds
    ) {
        return einladungService.getHiddenUntersuchungTabInfos(anlassId, einladungIds).stream()
            .sorted(Comparator.comparing(o -> o.getDossier().getKind().getNameVorname()))
            .map(this::toTabInfoJson)
            .toList();
    }

    @NonNull
    public EinladungJson updateEinladung(
        @NonNull ID<Einladung> einladungId,
        @NonNull EinladungSaveJson einladungSaveJson
    ) {
        Einladung existing = einladungService.getById(einladungId);
        einladungService.createUpdateOrDeleteAufgabeNachzuegler(
            einladungSaveJson,
            existing
        );
        Einladung updated = toEntity(existing, einladungSaveJson);
        Einladung persistedEinladung =
            einladungService.persistWithOptimisticLocking(updated, einladungSaveJson.getVersion());
        Nachkontrolle nachkontrolle = this.nachkontrolleService.getOrCreateByDossier(updated.getDossier().getId());
        LocalDate now = appClock.offsetDateTime().now().toLocalDate();
        return toJson(persistedEinladung, nachkontrolle, now);
    }

    @NonNull
    public NotizJson createEinladungNotiz(
        @NonNull ID<Einladung> einladungId,
        @NonNull NotizSaveJson createJson,
        @NonNull List<FileUpload> fileUploads
    ) {
        Notiz notiz = einladungNotizMapper.createEinladungNotiz(einladungId, createJson, fileUploads);
        return NotizMapper.toJson(notiz);
    }

    private void updateAbmeldungsNotiz(
        @NonNull Einladung einladung,
        @NonNull EinladungSaveJson einladungSaveJson
    ) {
        einladungNotizMapper.createOrUpdateAbmeldungsNotiz(einladung, einladungSaveJson);
    }

    public void untersuchungAbschliessen(@NonNull ID<Einladung> einladungId) {
        einladungService.untersuchungAbschliessen(einladungId);
    }

    public void requireConsistentPathParams(@NonNull ID<Einladung> einladungId, @NonNull ID<Anlass> expectedAnlassId) {
        ID<Anlass> actualAnlassId = einladungService.getByIdLockedQuery(einladungId).getAnlass().getId();
        PathParamConsistancyUtil.requireForeignIdsAreEqual(einladungId, actualAnlassId, expectedAnlassId);
    }

    public void requireConsistentPathParams(
        @NonNull List<ID<Einladung>> einladungenIds,
        @NonNull ID<Anlass> expectedAnlassId
    ) {
        Map<ID<Einladung>, ID<Anlass>> einladungenToAnlassMap = einladungService.getAnlassEinladungsMap(einladungenIds);
        einladungenIds.forEach(einladungId -> PathParamConsistancyUtil.requireForeignIdsAreEqual(
            einladungId,
            einladungenToAnlassMap.get(einladungId),
            expectedAnlassId));
    }
}
