package ch.dvbern.oss.hemed.esc.api.shared.printer.kinderdossier;

import java.time.LocalDate;
import java.util.List;

import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKind;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class KinderdossierPrintDTO {
    Kind kind;
    List<KontaktPersonKind> kontaktPersonKindList;
    List<AnlassUntersuchungPrintDTO> anlassDTOList;
    List<Notiz> notizenList;
    LocalDate now;
    TenantEnum tenant;
    AppLanguages language;
    KinderdossierPrintVariant variant;
}
