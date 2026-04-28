package ch.dvbern.oss.hemed.esc.api.features.kindanlaesse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheitService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungInternService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.kindanlaesse.json.KindAnlassSummaryJson;
import ch.dvbern.oss.hemed.esc.api.features.kindanlaesse.json.LastImpfungJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@JaxMapper
@RequiredArgsConstructor
public final class KindAnlaesseMapper {

    private final KindAnlaesseService kindAnlaesseService;
    private final UntersuchungImpfstatusKrankheitService untersuchungImpfstatusKrankheitService;
    private final UntersuchungImpfungInternService untersuchungImpfungInternService;

    @NonNull
    public List<KindAnlassSummaryJson> findAllForKind(
        @NonNull ID<Kind> kindID,
        @NonNull Predicate<Anlass> hasReadAuthorizationAnlass
    ) {
        return kindAnlaesseService.findAllEinladungForKind(kindID).stream()
            // some Roles can only read Einladungen of a certain AnlassTyp
            .filter(einladung -> hasReadAuthorizationAnlass.test(einladung.getAnlass()))
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
            Untersuchung untersuchung = untersuchungImpfungIntern.getUntersuchungImpfungen().getUntersuchung();
            LocalDateTime dateTime = untersuchungImpfungIntern.getTimestampLastManualChange() != null
                ? untersuchungImpfungIntern.getTimestampLastManualChange().toLocalDateTime()
                : null;
            results.add(new LastImpfungJson(
                dateTime,
                untersuchung.getAnlass().getId(),
                untersuchung.getEinladungRequired().getId()
            ));
        });

        Optional<UntersuchungImpfstatusKrankheit> untersuchungImpfstatusKrankheitOptional =
            untersuchungImpfstatusKrankheitService.findMostRecentlyChangedByDossier(kindId);

        untersuchungImpfstatusKrankheitOptional.ifPresent(untersuchungImpfstatusKrankheit -> {
            Einladung einladung =
                untersuchungImpfstatusKrankheit.getUntersuchungImpfungen().getUntersuchung().getEinladungRequired();
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
