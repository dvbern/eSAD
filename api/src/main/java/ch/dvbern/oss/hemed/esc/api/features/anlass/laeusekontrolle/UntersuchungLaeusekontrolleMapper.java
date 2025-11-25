package ch.dvbern.oss.hemed.esc.api.features.anlass.laeusekontrolle;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.laeusekontrolle.json.UntersuchungLaeusekontrolleJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.laeusekontrolle.json.UntersuchungLaeusekontrolleSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.laeusekontrolle.types.LaeusekontrolleBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@RequiredArgsConstructor
public class UntersuchungLaeusekontrolleMapper {

    private final UntersuchungService untersuchungService;
    private final UntersuchungLaeusekontrolleService untersuchungLaeusekontrolleService;


    @Nullable
    public static UntersuchungLaeusekontrolleJson toJson(
        @Nullable UntersuchungLaeusekontrolle laeusekontrolle
    ) {
        if (laeusekontrolle == null) {
            return null;
        }
        return new UntersuchungLaeusekontrolleJson(
            laeusekontrolle.getId(),
            EntityInfoJson.fromEntity(laeusekontrolle),
            new UntersuchungLaeusekontrolleSaveJson(
                EnumSetUtil.toList(laeusekontrolle.getBeurteilung()),
                laeusekontrolle.getBemerkungen(),
                laeusekontrolle.getVersion()
            )
        );
    }

    @NonNull
    private UntersuchungLaeusekontrolle toEntity(
        @NonNull Untersuchung untersuchung,
        @Nullable UntersuchungLaeusekontrolle existingLaeusekontrolle,
        @NonNull UntersuchungLaeusekontrolleSaveJson saveJson
    ) {
        if (existingLaeusekontrolle == null) {
            existingLaeusekontrolle = UntersuchungLaeusekontrolle.forUntersuchung(untersuchung);
        }
        existingLaeusekontrolle.getBeurteilung().clear();
        if (saveJson.getBeurteilung() != null) {
            existingLaeusekontrolle.getBeurteilung().addAll(EnumSetUtil.toEnumSet(saveJson.getBeurteilung(), LaeusekontrolleBeurteilung.class));
        }
        existingLaeusekontrolle.setBemerkungen(saveJson.getBemerkungen());
        return existingLaeusekontrolle;
    }

    @Nullable
    public UntersuchungLaeusekontrolleJson getByEinladung(@NonNull ID<Einladung> einladungId) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungId);
        return toJson(untersuchung.getUntersuchungLaeusekontrolle());
    }

    @NonNull
    public UntersuchungLaeusekontrolleJson save(
        @NonNull ID<Einladung> einladungID,
        @NonNull UntersuchungLaeusekontrolleSaveJson saveJson
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungID);
        UntersuchungLaeusekontrolle sehen = untersuchung.getUntersuchungLaeusekontrolle();
        UntersuchungLaeusekontrolle entity = toEntity(untersuchung, sehen, saveJson);
        UntersuchungLaeusekontrolle
            persistedSehen =
            untersuchungLaeusekontrolleService.persistWithOptimisticLocking(entity, saveJson.getVersion());
        return toJson(persistedSehen);
    }
}
