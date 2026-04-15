package ch.dvbern.oss.hemed.esc.api.features.spss.reports;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AbstractCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AnlassArtCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AnlassDatumCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AnlassTypCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.CsvColumnConfig;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.GeburtsdatumCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.GeschlechtCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.IdNrCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.ImpfungChargeCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.ImpfungDatumCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.ImpfungErstellerCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.ImpfungImpfstoffKrankheitenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.ImpfungImpfstoffNameCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.ImpfungStatusCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.ImpfungVerabreicherCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.SystemIdCsvColumn;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CsvImpfstoffeReport {
    private final CsvColumnConfig config;

    public List<AbstractCsvColumn> getReport() {
        return List.of(
            // ANLASS
            new AnlassTypCsvColumn(config, null),
            new AnlassArtCsvColumn(config),
            new AnlassDatumCsvColumn(config),
            new SystemIdCsvColumn(config, null),
            new IdNrCsvColumn(config, null),
            new GeschlechtCsvColumn(config, null),
            new GeburtsdatumCsvColumn(config),

            // Impfung
            new ImpfungImpfstoffNameCsvColumn(config),
            new ImpfungImpfstoffKrankheitenCsvColumn(config),
            new ImpfungChargeCsvColumn(config),
            new ImpfungDatumCsvColumn(config),
            new ImpfungErstellerCsvColumn(config),
            new ImpfungVerabreicherCsvColumn(config),
            new ImpfungStatusCsvColumn(config)
        );
    }
}
