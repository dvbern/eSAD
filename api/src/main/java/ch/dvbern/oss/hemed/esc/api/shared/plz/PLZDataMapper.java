package ch.dvbern.oss.hemed.esc.api.shared.plz;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.oss.hemed.esc.api.shared.plz.json.PLZDataJson;
import ch.dvbern.oss.hemed.esc.api.shared.plz.json.PLZDataListJson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Slf4j
@JaxMapper
@RequiredArgsConstructor
public class PLZDataMapper {
    private final PLZService plzService;

    public void importPlzKantonIntoDatabase() {
        this.plzService.importIntoDatabase();
    }

    @NonNull
    public PLZDataJson toJson(@NonNull PLZData plzData) {
        return new PLZDataJson(plzData.getPlz(), plzData.getOrtsBezeichnung(), plzData.getKanton());
    }

    @NonNull
    public PLZDataListJson findOrteForPLZ(@Nullable String plz) {
        return new PLZDataListJson(plzService.findOrteForPLZ(plz)
            .stream()
            .map(this::toJson)
            .toList());
    }
}
