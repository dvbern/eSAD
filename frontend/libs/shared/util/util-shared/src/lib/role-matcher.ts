/*
 *
 * Copyright (C) 2025 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {Berechtigung} from '@dvb/data-access-backend';

export type RoleMatcher = (roles: Berechtigung[]) => boolean;

export type AtLeastOneBerechtigung = [Berechtigung, ...Berechtigung[]];

export function withAnyBerechtigung(): RoleMatcher {
  return userBerechtigungen => userBerechtigungen.length > 0;
}

export function withAllOfBerechtigungen(...berechtigungen: AtLeastOneBerechtigung): RoleMatcher {
  return userBerechtigungen => {
    const allowed = berechtigungen.every(recht => userBerechtigungen.includes(recht));
    if (!allowed) {
      console.debug(`requires all of the following roles: '${berechtigungen}' but user has: '${userBerechtigungen}'`);
    }
    return allowed;
  };
}

export function withOneOfBerechtigungen(...berechtigungen: AtLeastOneBerechtigung): RoleMatcher {
  return userBerechtigungen => {
    const allowed = berechtigungen.some(recht => userBerechtigungen.includes(recht));
    if (!allowed) {
      console.debug(`requires one of the following roles: ${berechtigungen} but user has: '${userBerechtigungen}'`);
    }
    return allowed;
  };
}

export function withOneOfMatchers(...matchers: [RoleMatcher, ...RoleMatcher[]]): RoleMatcher {
  return (roles: Berechtigung[]) => matchers.some(matcher => matcher(roles));
}

// -- specific role mapper helper functions ---------
export function hasLaeuseAnlassBerechtigungen(): RoleMatcher {
  return withOneOfBerechtigungen(Berechtigung.ANLASSPLANUNG_LESEN_LAEUSE, Berechtigung.ANLASSPLANUNG_SCHREIBEN_LAEUSE);
}

export function hasDashboardBerechtigungen(): RoleMatcher {
  return withOneOfBerechtigungen(
      Berechtigung.ANLASSLISTE_LESEN,
      Berechtigung.ANLASSLISTE_LESEN_LAEUSE,
      Berechtigung.AUFGABEN_LESEN,
      Berechtigung.KINDER_LESEN_NICHT_MEDIZINISCH
  );
}

export function hasAlleAnlaesseBerechtigungen(): RoleMatcher {
  return withOneOfBerechtigungen(Berechtigung.ANLASSPLANUNG_LESEN, Berechtigung.ANLASSPLANUNG_SCHREIBEN);
}

export function hasAnlassCreateOrEditForNotAbgeschlossenBerechtigung(): RoleMatcher {
  return withOneOfMatchers(
    withAllOfBerechtigungen(Berechtigung.ANLASSPLANUNG_LESEN, Berechtigung.ANLASSPLANUNG_SCHREIBEN),
    withAllOfBerechtigungen(Berechtigung.ANLASSPLANUNG_LESEN_LAEUSE, Berechtigung.ANLASSPLANUNG_SCHREIBEN_LAEUSE),
  );
}

export function hasAnlassViewBerechtigung(): RoleMatcher {
  return withOneOfBerechtigungen(Berechtigung.ANLASSPLANUNG_LESEN, Berechtigung.ANLASSPLANUNG_LESEN_LAEUSE, Berechtigung.ANLASSLISTE_LESEN, Berechtigung.ANLASSLISTE_LESEN_LAEUSE);
}

export function hasAnlassEditBerechtigungForAbgeschlossen(): RoleMatcher {
  return withAllOfBerechtigungen(Berechtigung.VERANDERUNGEN_NACH_ABSCHLUSS);
}

export function hasKindEditBerechtigung(): RoleMatcher {
  return withAllOfBerechtigungen(
    Berechtigung.KINDER_LESEN_NICHT_MEDIZINISCH,
    Berechtigung.KINDER_SCHREIBEN_NICHT_MEDIZINISCH
  );
}

export function hasKinderMedizinischeDatenViewBerechtigung(): RoleMatcher {
  return withOneOfBerechtigungen(Berechtigung.KINDER_LESEN_MEDIZINISCH);
}

export function hasKinderMedizinischeDatenEditBerechtigung(): RoleMatcher {
  return withAllOfBerechtigungen(Berechtigung.KINDER_LESEN_MEDIZINISCH, Berechtigung.KINDER_SCHREIBEN_MEDIZINISCH);
}

export function hasDownloadDossierBerechtigung(): RoleMatcher {
  return withAllOfBerechtigungen(
    Berechtigung.KINDER_LESEN_MEDIZINISCH,
    Berechtigung.KINDER_LESEN_NICHT_MEDIZINISCH,
    Berechtigung.KOMPLETTER_EXPORT);
}

export function hasExportAnlassPlanungBerechtigung(): RoleMatcher {
  return withOneOfBerechtigungen(
    Berechtigung.ANLASSPLANUNG_SCHREIBEN,
    Berechtigung.ANLASSPLANUNG_SCHREIBEN_LAEUSE,
    Berechtigung.KOMPLETTER_EXPORT);
}

export function hasAllNotizenEditBerechtigung(): RoleMatcher {
  return withOneOfBerechtigungen(Berechtigung.KINDER_ALLE_NOTIZEN_SCHREIBEN);
}

export function hasNotizEditBerechtigung(): RoleMatcher {
  return withOneOfBerechtigungen(
    Berechtigung.KINDER_ALLE_NOTIZEN_SCHREIBEN,
    Berechtigung.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_SCHREIBEN,
  );
}

export function hasBenutzerverwaltungEditBerechtigungen(): RoleMatcher {
  return withAllOfBerechtigungen(Berechtigung.BENUTZER_BERECHTIGUNGEN_LESEN, Berechtigung.BENUTZER_BERECHTIGUNGEN_SCHREIBEN);
}

export function hasSchuleViewBerechtigung(): RoleMatcher {
  return withOneOfBerechtigungen(Berechtigung.SCHULE_LESEN);
}

export function hasBenutzerOptionReadBerechtigung(): RoleMatcher {
  return withOneOfBerechtigungen(Berechtigung.BENUTZER_OPTIONS_LESEN);
}

export function hasSchuleEditBerechtigung(): RoleMatcher {
  return withOneOfBerechtigungen(Berechtigung.SCHULE_SCHREIBEN);
}

export function hasKlassenViewBerechtigung(): RoleMatcher {
  return withOneOfBerechtigungen(Berechtigung.KLASSEN_LESEN);
}

export function hasKlassenEditBerechtigung(): RoleMatcher {
  return withOneOfBerechtigungen(Berechtigung.KLASSEN_SCHREIBEN);
}

export function hasInfomaterialViewBerechtigung(): RoleMatcher {
  return withOneOfBerechtigungen(Berechtigung.INFOMATERIAL_LESEN);
}

export function hasInfomaterialEditBerechtigung(): RoleMatcher {
  return withOneOfBerechtigungen(Berechtigung.INFOMATERIAL_SCHREIBEN);
}

export function hasLabelEditBerechtigung(): RoleMatcher {
  return withAllOfBerechtigungen(Berechtigung.LABELS_LESEN, Berechtigung.LABELS_SCHREIBEN);
}

export function hasImpfstoffeViewBerechtigung(): RoleMatcher {
  return withAllOfBerechtigungen(Berechtigung.IMPFSTOFFE_LESEN);
}

export function hasImpfstoffEditBerechtigung(): RoleMatcher {
  return withAllOfBerechtigungen(Berechtigung.IMPFSTOFFE_LESEN, Berechtigung.IMPFSTOFFE_SCHREIBEN);
}

export function hasBenutzerViewBerechtigung(): RoleMatcher {
  return withOneOfBerechtigungen(Berechtigung.BENUTZER_BERECHTIGUNGEN_LESEN);
}

export function hasAerztePageViewBerechtigung(): RoleMatcher {
  return withOneOfMatchers(
    withOneOfBerechtigungen(Berechtigung.AERZTE_LESEN)
  );
}

export function hasAerztePageEditBerechtigung(): RoleMatcher {
  return withAllOfBerechtigungen(Berechtigung.AERZTE_LESEN, Berechtigung.AERZTE_SCHREIBEN);
}

export function hasLabelPageViewBerechtigung(): RoleMatcher {
  return withOneOfBerechtigungen(Berechtigung.LABELS_ADMIN_LESEN);
}

export function hasImpfstoffePageViewBerechtigung(): RoleMatcher {
  return withOneOfBerechtigungen(Berechtigung.IMPFSTOFFE_LESEN);
}

export function hasBriefvorlagenViewBerechtigung(): RoleMatcher {
  return withOneOfBerechtigungen(Berechtigung.VORLAGEN_ADMIN_LESEN);
}

export function hasBriefvorlageEditBerechtigung(): RoleMatcher {
  return withAllOfBerechtigungen(Berechtigung.VORLAGEN_LESEN, Berechtigung.VORLAGEN_SCHREIBEN);
}

export function hasReportViewBerechtigung(): RoleMatcher {
  return withOneOfBerechtigungen(
    Berechtigung.INDIVIDUELLE_REPORTS,
    Berechtigung.SPSS_EXPORT,
    Berechtigung.VORLAGEN_ADMIN_LESEN
  );
}

export function hasPlaygroundViewBerechtigung(): RoleMatcher {
  return withOneOfBerechtigungen(Berechtigung.SUPPORT);
}

export function hasAdminPageViewBerechtiung(): RoleMatcher {
  return withOneOfBerechtigungen(
    Berechtigung.IMPFSTOFFE_LESEN,
    Berechtigung.LABELS_ADMIN_LESEN,
    Berechtigung.VORLAGEN_ADMIN_LESEN,
    Berechtigung.AERZTE_LESEN,
    Berechtigung.ARCHIVIERUNG,
    Berechtigung.SCHULE_LESEN,
    Berechtigung.KLASSEN_LESEN,
    Berechtigung.INFOMATERIAL_LESEN,
    Berechtigung.DUBLETTEN_ZUSAMMENFUHREN,
    Berechtigung.ORGANISATION_VERWALTEN_LESEN,
    Berechtigung.BENUTZER_BERECHTIGUNGEN_LESEN
  );
}

export function hasSystemPageViewBerechtiung(): RoleMatcher {
  return withOneOfBerechtigungen(Berechtigung.SUPPORT);
}

export function hasKindPageViewBerechtigung(): RoleMatcher {
  return withOneOfBerechtigungen(
    Berechtigung.KINDER_LESEN_NICHT_MEDIZINISCH,
    Berechtigung.KINDER_LESEN_MEDIZINISCH,
    Berechtigung.KINDER_ALLE_NOTIZEN_LESEN,
    Berechtigung.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_LESEN,
    Berechtigung.DUBLETTEN_ZUSAMMENFUHREN
  );
}

export function hasConfigViewBerechtigung(): RoleMatcher {
  return withOneOfBerechtigungen(
    Berechtigung.SUPPORT,
    // May be different than edit in future, but currently the same
  );
}

export function hasConfigEditBerechtigung(): RoleMatcher {
  return withOneOfBerechtigungen(
    Berechtigung.SUPPORT,
  )
}

export function hasKindNotizViewBerechtigung(): RoleMatcher {
  return withOneOfBerechtigungen(
    Berechtigung.KINDER_ALLE_NOTIZEN_LESEN,
    Berechtigung.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_LESEN,
  )
}

export function hasKindVorlagenViewBerechtigung(): RoleMatcher {
  return withOneOfBerechtigungen(Berechtigung.VORLAGEN_LESEN)
}

export function hasUntersuchungKindDatenViewBerechtigung(): RoleMatcher {
  return withAllOfBerechtigungen(Berechtigung.SU_DATEN_LESEN);
}

export function hasUntersuchungKindDatenEditBerechtigung(): RoleMatcher {
  return withAllOfBerechtigungen(Berechtigung.SU_DATEN_LESEN, Berechtigung.SU_DATEN_SCHREIBEN);
}

export function hasGroesseGewichtViewBerechtigung(): RoleMatcher {
  return withAllOfBerechtigungen(Berechtigung.SU_GROSSE_GEWICHT_LESEN);
}

export function hasGroesseGewichtEditBerechtigung(): RoleMatcher {
  return withAllOfBerechtigungen(Berechtigung.SU_GROSSE_GEWICHT_LESEN, Berechtigung.SU_GROSSE_GEWICHT_SCHREIBEN);
}

export function hasSehenViewBerechtigung(): RoleMatcher {
  return withAllOfBerechtigungen(Berechtigung.SU_SEHEN_LESEN);
}

export function hasSehenEditBerechtigung(): RoleMatcher {
  return withOneOfMatchers(
    withAllOfBerechtigungen(Berechtigung.SU_SEHEN_LESEN, Berechtigung.SU_SEHEN_SCHREIBEN),
    withOneOfBerechtigungen(Berechtigung.SUPPORT),
  );
}

export function hasImpfungenEditBerechtigung(): RoleMatcher {
  return withAllOfBerechtigungen(Berechtigung.SU_IMPFEN_LESEN, Berechtigung.SU_IMPFEN_SCHREIBEN);
}

export function hasImpfungenViewBerechtigung(): RoleMatcher {
  return withAllOfBerechtigungen(Berechtigung.SU_IMPFEN_LESEN);
}

export function hasHoerenViewBerechtigung(): RoleMatcher {
  return withAllOfBerechtigungen(Berechtigung.SU_HOREN_LESEN);
}

export function hasHoerenEditBerechtigung(): RoleMatcher {
  return withAllOfBerechtigungen(Berechtigung.SU_HOREN_LESEN, Berechtigung.SU_HOREN_SCHREIBEN);
}

export function hasTriageViewBerechtigung(): RoleMatcher {
  return withAllOfBerechtigungen(Berechtigung.SU_TRIAGE_LESEN);
}

export function hasAerztlicheUntersuchungViewBerechtigung(): RoleMatcher {
  return withAllOfBerechtigungen(Berechtigung.SU_AERZTLICHE_UNTERSUCHUNG_LESEN);
}

export function hasAerztlicheUntersuchungEditBerechtigung(): RoleMatcher {
  return withAllOfBerechtigungen(
    Berechtigung.SU_AERZTLICHE_UNTERSUCHUNG_LESEN,
    Berechtigung.SU_AERZTLICHE_UNTERSUCHUNG_SCHREIBEN,
  );
}

export function hasUntersuchungAbschlussViewBerechtigung(): RoleMatcher {
  return withAllOfBerechtigungen(Berechtigung.SU_ABSCHLUSS_LESEN);
}

export function hasUntersuchungAbschlussEditBerechtigung(): RoleMatcher {
  return withAllOfBerechtigungen(
    Berechtigung.SU_ABSCHLUSS_LESEN,
    Berechtigung.SU_ABSCHLUSS_SCHREIBEN
  );
}

export function hasUntersuchungVorabklaerungenViewBerechtigung(): RoleMatcher {
  return withAllOfBerechtigungen(Berechtigung.SU_VORABKLAERUNGEN_LESEN);
}

export function hasUntersuchungVorabklaerungenEditBerechtigung(): RoleMatcher {
  return withAllOfBerechtigungen(
    Berechtigung.SU_VORABKLAERUNGEN_LESEN,
    Berechtigung.SU_VORABKLAERUNGEN_SCHREIBEN
  );
}

export function hasLaeusekontrolleViewBerechtigung(): RoleMatcher {
  return withAllOfBerechtigungen(Berechtigung.SU_LAEUSE_LESEN);
}

export function hasLaeusekontrolleEditBerechtigung(): RoleMatcher {
  return withAllOfBerechtigungen(
    Berechtigung.SU_LAEUSE_LESEN,
    Berechtigung.SU_LAEUSE_SCHREIBEN,
  );
}

export function hasMassnahmenViewBerechtigung(): RoleMatcher {
  return withAllOfBerechtigungen(Berechtigung.SU_MASSNAHMEN_LESEN);
}

export function hasAufgabenViewBerechtigungen(): RoleMatcher {
  return withAllOfBerechtigungen(Berechtigung.AUFGABEN_LESEN);
}

export function hasAufgabenEditBerechtigungen(): RoleMatcher {
  return withAllOfBerechtigungen(Berechtigung.AUFGABEN_LESEN, Berechtigung.AUFGABEN_SCHREIBEN);
}

export function hasSpssReportsViewBerechtigung(): RoleMatcher {
  return withAllOfBerechtigungen(Berechtigung.SPSS_EXPORT);
}

export function hasIndividuelleReportsViewBerechtigung(): RoleMatcher {
  return withAllOfBerechtigungen(Berechtigung.INDIVIDUELLE_REPORTS);
}

export function hasDuplikateBerechtigung(): RoleMatcher {
  return withAllOfBerechtigungen(Berechtigung.DUBLETTEN_ZUSAMMENFUHREN);
}

export function hasArchivierungBerechtigung(): RoleMatcher {
  return withAllOfBerechtigungen(Berechtigung.ARCHIVIERUNG);
}

export function hasOrganisationVerwaltungViewBerechtigung(): RoleMatcher {
  return withOneOfMatchers(
    withOneOfBerechtigungen(Berechtigung.ORGANISATION_VERWALTEN_LESEN, Berechtigung.ORGANISATION_VERWALTEN_SCHREIBEN),
  );
}

export function hasOrganisationVerwaltungEditBerechtigung(): RoleMatcher {
  return withOneOfMatchers(
    withAllOfBerechtigungen(Berechtigung.ORGANISATION_VERWALTEN_LESEN, Berechtigung.ORGANISATION_VERWALTEN_SCHREIBEN),
  );
}

export function hasOrganisationVerwaltungSupportBerechtigung(): RoleMatcher {
  return withOneOfMatchers(
    withAllOfBerechtigungen(Berechtigung.SUPPORT),
  );
}

export function hasDoc360ViewBerechtigung(): RoleMatcher {
  return withAnyBerechtigung();
}
