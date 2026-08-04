package ch.dvbern.oss.hemed.esc.api.shared.printer.anonymer;

import java.time.LocalDate;
import java.util.List;

import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.printer.kinderdossier.AnlassUntersuchungPrintDTO;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class AnonymerEintragPrintDTO {
    String hash;
    List<AnlassUntersuchungPrintDTO> anlassDTOList;
    LocalDate now;
    TenantEnum tenant;
    AppLanguages language;
}
