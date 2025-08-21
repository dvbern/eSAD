package ch.dvbern.oss.hemed.esc.api.features.kindanlaesse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungImpfstatusKrankheit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungImpfstatusKrankheitService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungImpfungInternService;
import ch.dvbern.oss.hemed.esc.api.features.kindanlaesse.json.KindAnlassSummaryJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kindanlaesse.json.LastImpfungJson;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@JaxMapper
@RequiredArgsConstructor
public final class KindAnlaesseMapper {

    private final KindAnlaesseService kindAnlaesseService;
    private final UntersuchungImpfstatusKrankheitService untersuchungImpfstatusKrankheitService;
    private final UntersuchungImpfungInternService untersuchungImpfungInternService;

    @NonNull
    public List<KindAnlassSummaryJson> findAllForKind(@NonNull ID<Kind> kindID) {
        return kindAnlaesseService.findAllEinladungForKind(kindID).stream()
            .map(KindAnlaesseMapper::toKindAnlassSummaryJson)
            .toList();
    }

    @NonNull
    public static KindAnlassSummaryJson toKindAnlassSummaryJson(@NonNull Einladung einladung) {
        Anlass anlass = einladung.getAnlass();
        return new KindAnlassSummaryJson(
            einladung.getId(),
            anlass.getId(),
            einladung.getId(),
            anlass.getAnlassConfig().getAnlassTyp(),
            anlass.getDatum(),
            anlass.getTitel(),
            anlass.getSchuljahr(),
            einladung.getEinladungStatus(),
            anlass.getAnlassStatus()
        );
    }

    @NonNull
    public LastImpfungJson getLatestManuallyChangedImpfungByDossierId(@NonNull ID<Kind> kindId) {
        List<LastImpfungJson> results = new ArrayList<>();

        Optional<UntersuchungImpfungIntern> untersuchungImpfungInternOptional =
            untersuchungImpfungInternService.findMostRecentlyChangedByDossier(kindId);

        untersuchungImpfungInternOptional.ifPresent(untersuchungImpfungIntern -> {
            Einladung einladung = untersuchungImpfungIntern.getUntersuchungImpfungen().getUntersuchung().getEinladung();
            LocalDateTime dateTime = untersuchungImpfungIntern.getTimestampLastManualChange() != null
                ? untersuchungImpfungIntern.getTimestampLastManualChange().toLocalDateTime()
                : null;
            results.add(new LastImpfungJson(
                dateTime,
                einladung.getAnlass().getId(),
                einladung.getId()
            ));
        });

        Optional<UntersuchungImpfstatusKrankheit> untersuchungImpfstatusKrankheitOptional =
            untersuchungImpfstatusKrankheitService.findMostRecentlyChangedByDossier(kindId);

        untersuchungImpfstatusKrankheitOptional.ifPresent(untersuchungImpfstatusKrankheit -> {
            Einladung einladung =
                untersuchungImpfstatusKrankheit.getUntersuchungImpfungen().getUntersuchung().getEinladung();
            LocalDateTime dateTime = untersuchungImpfstatusKrankheit.getTimestampLastManualChange() != null
                ? untersuchungImpfstatusKrankheit.getTimestampLastManualChange().toLocalDateTime()
                : null;
            results.add(new LastImpfungJson(
                dateTime,
                einladung.getAnlass().getId(),
                einladung.getId()
            ));
        });

        return results.stream()
            .filter(lastImpfungJson -> lastImpfungJson.getTimestampLastManualChange() != null)
            .max(Comparator.comparing(LastImpfungJson::getTimestampLastManualChange))
            .orElse(new LastImpfungJson(null, null, null));
    }
}
