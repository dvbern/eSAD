package ch.dvbern.oss.hemed.esc.api.features.anlass.impfung;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.HasUntersuchungIDWrapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfstatusKrankheitSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfstatusListSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfungInternJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfungInternListJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfungenChecklisteSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfungenDataJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfungenDoppelkontrolleSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfungenImpfausweisSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfungenJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfungenSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmenMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerMapper;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.BenutzerOptionJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@JaxMapper
@RequiredArgsConstructor
public final class UntersuchungImpfungenMapper {

    private final UntersuchungService untersuchungService;
    private final UntersuchungImpfungenService untersuchungImpfungenService;

    private final UntersuchungImpfstatusKrankheitMapper untersuchungImpfstatusKrankheitMapper;
    private final UntersuchungImpfungInternMapper untersuchungImpfungInternMapper;
    private final VisumMapper visumMapper;
    private final TenantProducer tenantProducer;
    private final AppClock appClock;

    @Nullable
    public static UntersuchungImpfungenJson toJson(@Nullable UntersuchungImpfungen impfungen, @NonNull TenantEnum tenant) {
        if (impfungen == null) {
            return null;
        }
        return new UntersuchungImpfungenJson(
            impfungen.getId(),
            EntityInfoJson.fromEntity(impfungen),
            new UntersuchungImpfungenSaveJson(
                new UntersuchungImpfungenImpfausweisSaveJson(
                    impfungen.isImpfausweisUnvollstaendig(),
                    impfungen.isImpfausweisNichtVorhanden(),
                    impfungen.isImpfausweisZweiPlusEinsSchema(),
                    impfungen.getImpfausweisStatusFlagstimestampMutiert() != null
                ),
                new UntersuchungImpfungenChecklisteSaveJson(
                    impfungen.isCheckEinwilligungVorhanden(),
                    impfungen.isCheckKontraindikationenAbgeklaert(),
                    impfungen.isCheckDoppelkontrolleImpfentscheidDurchgefuehrt(),
                    impfungen.getBemerkungen()
                ),
                new UntersuchungImpfungenDoppelkontrolleSaveJson(
                    impfungen.getVisum1() != null
                        ? BenutzerMapper.toOptionJson(impfungen.getVisum1().getPerson(), tenant)
                        : null,
                    impfungen.getVisum1() != null
                        ? impfungen.getVisum1().getDatumVisiert()
                        : null,
                    impfungen.getVisum2() != null
                        ? BenutzerMapper.toOptionJson(impfungen.getVisum2().getPerson(), tenant)
                        : null,
                    impfungen.getVisum2() != null
                        ? impfungen.getVisum2().getDatumVisiert()
                        : null
                ),
                impfungen.getVersion()
            )
        );
    }

    @NonNull
    private UntersuchungImpfungen toEntity(
        @NonNull Untersuchung untersuchung,
        @Nullable UntersuchungImpfungen existingImpfungen,
        @NonNull UntersuchungImpfungenSaveJson saveJson
    ) {
        if (existingImpfungen == null) {
            existingImpfungen = new UntersuchungImpfungen();
            existingImpfungen.setUntersuchung(untersuchung);
            untersuchung.setUntersuchungImpfungen(existingImpfungen);
        }
        Visum visum1 = visumMapper.toEntity(
            existingImpfungen.getVisum1(),
            saveJson.getDoppelkontrolle().getVisumPerson1(),
            saveJson.getDoppelkontrolle().getVisumDatum1());
        Visum visum2 = visumMapper.toEntity(
            existingImpfungen.getVisum2(),
            saveJson.getDoppelkontrolle().getVisumPerson2(),
            saveJson.getDoppelkontrolle().getVisumDatum2());
        existingImpfungen.setImpfausweisUnvollstaendig(saveJson.getImpfausweis().isImpfausweisUnvollstaendig());
        existingImpfungen.setImpfausweisNichtVorhanden(saveJson.getImpfausweis().isImpfausweisNichtVorhanden());
        if (saveJson.getImpfausweis().isImpfausweisStatusMutiert()){
            existingImpfungen.setImpfausweisStatusFlagstimestampMutiert(appClock.offsetDateTime().now());
        }
        existingImpfungen.setImpfausweisZweiPlusEinsSchema(saveJson.getImpfausweis().isImpfausweisZweiPlusEinsSchema());
        existingImpfungen.setCheckEinwilligungVorhanden(saveJson.getCheckliste().isCheckEinwilligungVorhanden());
        existingImpfungen.setCheckKontraindikationenAbgeklaert(saveJson.getCheckliste().isCheckKontraindikationenAbgeklaert());
        existingImpfungen.setCheckDoppelkontrolleImpfentscheidDurchgefuehrt(saveJson.getCheckliste().isCheckDoppelkontrolleImpfentscheidDurchgefuehrt());
        existingImpfungen.setBemerkungen(saveJson.getCheckliste().getBemerkungen());
        existingImpfungen.setVisum1(visum1);
        existingImpfungen.setVisum2(visum2);
        return existingImpfungen;
    }

    @NonNull
    public UntersuchungImpfungenDataJson getByEinladung(
        @NonNull HasUntersuchungIDWrapper wrappedId,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(wrappedId);
        hasAuthorizationUntersuchung.accept(untersuchung);
        UntersuchungImpfungen impfungen =
            untersuchungImpfungenService.getOrCreateUntersuchungImpfungen(untersuchung.getId());

        List<UntersuchungImpfstatusKrankheitSaveJson> impfstatusJsonList = untersuchungImpfstatusKrankheitMapper.getByUntersuchungImpfungen(impfungen, !wrappedId.isAnonymerEintrag());
        List<UntersuchungImpfungInternJson> impfungenInternList = List.of();
        if(!wrappedId.isAnonymerEintrag()){
            impfungenInternList = untersuchungImpfungInternMapper.findByEinladung(wrappedId);
        }

        return new UntersuchungImpfungenDataJson(
            toJson(impfungen, tenantProducer.produceTenant()),
            UntersuchungImpfungenMassnahmenMapper.toJson(impfungen.getUntersuchungImpfungenMassnahmen()),
            new UntersuchungImpfstatusListSaveJson(impfstatusJsonList),
            new UntersuchungImpfungInternListJson(impfungenInternList),
            UntersuchungImpfungenDataMinimalMapper.toJson(impfungen.getUntersuchungImpfungenDataMinimal()),
            UntersuchungImpfungenNurUnvollstaendigeMapper.toJson(impfungen.getUntersuchungImpfungenNurUnvollstaendige()));
    }

    @NonNull
    public UntersuchungImpfungenJson save(
        @NonNull HasUntersuchungIDWrapper wrappedId,
        @NonNull UntersuchungImpfungenSaveJson saveJson,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(wrappedId);
        hasAuthorizationUntersuchung.accept(untersuchung);
        UntersuchungImpfungen existingUntersuchungImpfung = untersuchung.getUntersuchungImpfungen();
        UntersuchungImpfungen entity = toEntity(untersuchung, existingUntersuchungImpfung, saveJson);
        return toJson(untersuchungImpfungenService.persistWithOptimisticLocking(entity, saveJson.getVersion()), tenantProducer.produceTenant());
    }

    @NonNull
    public UntersuchungImpfungenJson visierenDoppelkontrolleVisum1(
        @NonNull ID<Einladung> einladungID,
        @NonNull UntersuchungImpfungenSaveJson saveJson,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungID);
        hasAuthorizationUntersuchung.accept(untersuchung);
        UntersuchungImpfungen untersuchungImpfung = untersuchung.getUntersuchungImpfungen();
        Objects.requireNonNull(untersuchungImpfung);
        return toJson(untersuchungImpfungenService.visierenDoppelkontrolleVisum1(
            untersuchungImpfung,
            saveJson.getVersion()), tenantProducer.produceTenant());
    }

    @NonNull
    public UntersuchungImpfungenJson anfordernDoppelkontrolleVisum2(
        @NonNull ID<Einladung> einladungID,
        @NonNull BenutzerOptionJson benutzerJson,
        @NonNull UntersuchungImpfungenSaveJson saveJson,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungID);
        hasAuthorizationUntersuchung.accept(untersuchung);
        UntersuchungImpfungen untersuchungImpfung = untersuchung.getUntersuchungImpfungen();
        Objects.requireNonNull(untersuchungImpfung);
        return toJson(untersuchungImpfungenService.anfordernDoppelkontrolleVisum2(
            untersuchungImpfung,
            saveJson.getVersion(),
            benutzerJson.getId()), tenantProducer.produceTenant());
    }

    @NonNull
    public UntersuchungImpfungenJson visierenDoppelkontrolleVisum2(
        @NonNull ID<Einladung> einladungID,
        @NonNull UntersuchungImpfungenSaveJson saveJson,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungID);
        hasAuthorizationUntersuchung.accept(untersuchung);
        UntersuchungImpfungen untersuchungImpfung = untersuchung.getUntersuchungImpfungen();
        Objects.requireNonNull(untersuchungImpfung);
        return toJson(untersuchungImpfungenService.visierenDoppelkontrolleVisum2(
            untersuchungImpfung,
            saveJson.getVersion()), tenantProducer.produceTenant());
    }

    @NonNull
    public UntersuchungImpfungenJson resetDoppelkontrolle(
        @NonNull ID<Einladung> einladungID,
        @NonNull UntersuchungImpfungenSaveJson saveJson,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungID);
        hasAuthorizationUntersuchung.accept(untersuchung);
        UntersuchungImpfungen untersuchungImpfung = untersuchung.getUntersuchungImpfungen();
        Objects.requireNonNull(untersuchungImpfung);
        return toJson(untersuchungImpfungenService.resetDoppelkontrolle(
            untersuchungImpfung,
            saveJson.getVersion()), tenantProducer.produceTenant());
    }
}
