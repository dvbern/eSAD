package ch.dvbern.oss.hemed.esc.api.shared.plz;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.StopWatch;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Service
@RequiredArgsConstructor
@Slf4j
public class PLZService {

    private final PLZDataRepo plzDataRepo;
    private final PLZImportService plzImportService;

    public void importIntoDatabase() {
        LOG.info("ESC-INFO: Import of PLZs was triggered");
        StopWatch stopWatch = StopWatch.createStarted();
        this.plzDataRepo.dropAll();
        List<PLZData> plzData = plzImportService.importDefaultPlzDataFromCSV();
        Set<PLZData> uniquePLZs = new HashSet<>(plzData);
        Validate.isTrue(uniquePLZs.size() == plzData.size());

        plzDataRepo.createAll(plzData);

        LOG.info("ESC-INFO: Import of PLZs done, took {}s", stopWatch.getTime(TimeUnit.SECONDS));
    }

    @NonNull
    public List<PLZData> findOrteForPLZ(@Nullable String plz) {
        if (plz == null) {
            return Collections.emptyList();
        }
        List<PLZData> orteForPLZ = plzDataRepo.findOrteForPLZ(plz);
        if (!orteForPLZ.isEmpty()) {
            return orteForPLZ;
        }
        LOG.info("ESC-INFO: Could not find any imported PLZ Data, triggering import...");
        importIntoDatabase();
        return plzDataRepo.findOrteForPLZ(plz);
    }
}

