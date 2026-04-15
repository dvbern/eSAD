package ch.dvbern.oss.hemed.esc.api.shared.printer.kinderdossier;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.AnlassWithDateTimeJson;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class KinderdossierAnlassPrintDTO {
    private final Einladung einladung;
    private final List<UntersuchungImpfungIntern> impfungInternList;
    private final List<UntersuchungImpfstatusKrankheit> impfstatusList;
    private final List<AnlassWithDateTimeJson> folgeImpfAnlassList;
}
