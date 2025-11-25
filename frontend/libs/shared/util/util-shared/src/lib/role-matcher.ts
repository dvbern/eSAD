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
  return _recht => true;
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

export function hasAlleAnlaesseBerechtigungen(): RoleMatcher {
  return withOneOfBerechtigungen(Berechtigung.ANLASSPLANUNG_LESEN, Berechtigung.ANLASSPLANUNG_SCHREIBEN);
}

export function hasAnlassCreateBerechtigung(): RoleMatcher {
  return withOneOfMatchers(
      withAllOfBerechtigungen(Berechtigung.ANLASSPLANUNG_LESEN, Berechtigung.ANLASSPLANUNG_SCHREIBEN),
      withAllOfBerechtigungen(Berechtigung.ANLASSPLANUNG_LESEN_LAEUSE, Berechtigung.ANLASSPLANUNG_SCHREIBEN_LAEUSE),
      withOneOfBerechtigungen(Berechtigung.SUPPORT, Berechtigung.SYSTEM_ADMIN),
    );
}

export function hasAnlassViewBerechtigung(): RoleMatcher {
  return withOneOfMatchers(
    withOneOfBerechtigungen(Berechtigung.ANLASSPLANUNG_LESEN, Berechtigung.ANLASSPLANUNG_LESEN_LAEUSE),
    withOneOfBerechtigungen(Berechtigung.SUPPORT, Berechtigung.SYSTEM_ADMIN),
  );
}

export function hasAnlassEditBerechtigungForAbgeschlossen(): RoleMatcher {
  return withOneOfMatchers(
    withAllOfBerechtigungen(Berechtigung.VERANDERUNGEN_NACH_ABSCHLUSS),
    withOneOfBerechtigungen(Berechtigung.SUPPORT, Berechtigung.SYSTEM_ADMIN),
  );
}

export function hasAnlassEditBerechtigungForNotAbgeschlossen(): RoleMatcher {
  return withOneOfMatchers(
      withAllOfBerechtigungen(Berechtigung.ANLASSPLANUNG_LESEN, Berechtigung.ANLASSPLANUNG_SCHREIBEN),
      withAllOfBerechtigungen(Berechtigung.ANLASSPLANUNG_LESEN_LAEUSE, Berechtigung.ANLASSPLANUNG_SCHREIBEN_LAEUSE),
      withOneOfBerechtigungen(Berechtigung.SUPPORT, Berechtigung.SYSTEM_ADMIN),
  );
}

export function hasKindEditBerechtigung(): RoleMatcher {
  return withOneOfMatchers(
    withAllOfBerechtigungen(Berechtigung.KINDER_LESEN_NICHT_MEDIZINISCH, Berechtigung.KINDER_SCHREIBEN_NICHT_MEDIZINISCH),
    withOneOfBerechtigungen(Berechtigung.SUPPORT),
  );
}

export function hasKinderMedizinischeDatenViewBerechtigung(): RoleMatcher {
  return withOneOfBerechtigungen(Berechtigung.KINDER_LESEN_MEDIZINISCH, Berechtigung.SUPPORT);
}

export function hasKinderMedizinischeDatenEditBerechtigung(): RoleMatcher {
  return withOneOfMatchers(
    withAllOfBerechtigungen(Berechtigung.KINDER_LESEN_MEDIZINISCH, Berechtigung.KINDER_SCHREIBEN_MEDIZINISCH),
    withOneOfBerechtigungen(Berechtigung.SUPPORT),
  );
}

export function hasDownloadDossierBerechtigung(): RoleMatcher {
  return withOneOfMatchers(
    withAllOfBerechtigungen(Berechtigung.KOMPLETTER_EXPORT),
    withOneOfBerechtigungen(Berechtigung.SUPPORT),
  );
}

export function hasExportAnlassPlanungBerechtigung(): RoleMatcher {
  return withOneOfMatchers(
    withOneOfBerechtigungen(Berechtigung.ANLASSPLANUNG_SCHREIBEN,
      Berechtigung.ANLASSPLANUNG_SCHREIBEN_LAEUSE,
      Berechtigung.KOMPLETTER_EXPORT),
    withOneOfBerechtigungen(Berechtigung.SUPPORT),
  );
}

export function hasAllNotizenEditBerechtigung(): RoleMatcher {
  return withOneOfMatchers(
    withOneOfBerechtigungen(Berechtigung.KINDER_ALLE_NOTIZEN_SCHREIBEN),
    withOneOfBerechtigungen(Berechtigung.SUPPORT),
  );
}

export function hasNotizEditBerechtigung(): RoleMatcher {
  return withOneOfMatchers(
    withOneOfBerechtigungen(
      Berechtigung.KINDER_ALLE_NOTIZEN_SCHREIBEN,
      Berechtigung.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_SCHREIBEN,
    ),
    withOneOfBerechtigungen(Berechtigung.SUPPORT),
  );
}

export function hasListEditBerechtigung(): RoleMatcher {
  return withOneOfMatchers(
    withAllOfBerechtigungen(Berechtigung.LISTEN_LESEN, Berechtigung.LISTEN_SCHREIBEN),
    withOneOfBerechtigungen(Berechtigung.SUPPORT),
  );
}

export function hasListViewBerechtigung(): RoleMatcher {
  return withOneOfBerechtigungen(Berechtigung.LISTEN_LESEN, Berechtigung.LISTEN_ADMIN_LESEN, Berechtigung.SUPPORT);
}

export function hasLabelEditBerechtigung(): RoleMatcher {
  return withOneOfMatchers(
    withAllOfBerechtigungen(Berechtigung.LABELS_LESEN, Berechtigung.LABELS_SCHREIBEN),
    withOneOfBerechtigungen(Berechtigung.SUPPORT),
  );
}

export function hasImpfstoffEditBerechtigung(): RoleMatcher {
  return withOneOfMatchers(
    withAllOfBerechtigungen(Berechtigung.IMPFSTOFFE_LESEN, Berechtigung.IMPFSTOFFE_SCHREIBEN),
    withOneOfBerechtigungen(Berechtigung.SUPPORT),
  );
}

export function hasBenutzerViewBerechtigung(): RoleMatcher {
  return withOneOfBerechtigungen(Berechtigung.MITARBEITERLISTE_BERECHTIGUNGEN_LESEN, Berechtigung.SUPPORT);
}

export function hasAerztePageViewBerechtigung(): RoleMatcher {
  return withOneOfBerechtigungen(Berechtigung.AERZTE_LESEN, Berechtigung.SUPPORT);
}

export function hasAerztePageEditBerechtigung(): RoleMatcher {
  return withOneOfMatchers(
    withAllOfBerechtigungen(Berechtigung.AERZTE_LESEN, Berechtigung.AERZTE_SCHREIBEN),
    withOneOfBerechtigungen(Berechtigung.SUPPORT),
  );
}

export function hasLabelPageViewBerechtigung(): RoleMatcher {
  return withOneOfBerechtigungen(Berechtigung.LABELS_ADMIN_LESEN, Berechtigung.SUPPORT);
}

export function hasImpfstoffePageViewBerechtigung(): RoleMatcher {
  return withOneOfBerechtigungen(Berechtigung.IMPFSTOFFE_LESEN, Berechtigung.SUPPORT);
}

export function hasBriefvorlagenViewBerechtigung(): RoleMatcher {
  return withOneOfBerechtigungen(Berechtigung.VORLAGEN_ADMIN_LESEN, Berechtigung.SUPPORT);
}

export function hasBriefvorlageEditBerechtigung(): RoleMatcher {
  return withOneOfMatchers(
    withAllOfBerechtigungen(Berechtigung.VORLAGEN_LESEN, Berechtigung.VORLAGEN_SCHREIBEN),
    withOneOfBerechtigungen(Berechtigung.SUPPORT),
  );
}

export function hasReportViewBerechtigung(): RoleMatcher {
  return withOneOfBerechtigungen(
    Berechtigung.INDIVIDUELLE_REPORTS,
    Berechtigung.SPSS_EXPORT,
    Berechtigung.KOMPLETTER_EXPORT,
    Berechtigung.SUPPORT,
  );
}

export function hasPlaygroundViewBerechtigung(): RoleMatcher {
  return withOneOfBerechtigungen(Berechtigung.SUPPORT);
}

export function hasSystemAdminViewBerechtigung(): RoleMatcher {
  return withAllOfBerechtigungen(Berechtigung.SUPPORT);
}

export function hasAdminPageViewBerechtiung(): RoleMatcher {
  return withOneOfBerechtigungen(
    Berechtigung.LISTEN_ADMIN_LESEN,
    Berechtigung.IMPFSTOFFE_LESEN,
    Berechtigung.LABELS_ADMIN_LESEN,
    Berechtigung.VORLAGEN_ADMIN_LESEN,
    Berechtigung.SUPPORT,
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
    Berechtigung.DUBLETTEN_ZUSAMMENFUHREN,
    Berechtigung.SUPPORT,
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
    Berechtigung.SUPPORT,
  )
}

export function hasKindVorlagenViewBerechtigung(): RoleMatcher {
  return withOneOfBerechtigungen(
    Berechtigung.VORLAGEN_LESEN,
    Berechtigung.SUPPORT,
  )
}

export function hasUntersuchungKindDatenViewBerechtigung(): RoleMatcher {
  return withOneOfMatchers(
    withAllOfBerechtigungen(Berechtigung.SU_DATEN_LESEN),
    withOneOfBerechtigungen(Berechtigung.SUPPORT),
  );
}

export function hasUntersuchungKindDatenEditBerechtigung(): RoleMatcher {
  return withOneOfMatchers(
    withAllOfBerechtigungen(Berechtigung.SU_DATEN_LESEN, Berechtigung.SU_DATEN_SCHREIBEN),
    withOneOfBerechtigungen(Berechtigung.SUPPORT),
  );
}

export function hasGroesseGewichtViewBerechtigung(): RoleMatcher {
  return withOneOfMatchers(
    withAllOfBerechtigungen(Berechtigung.SU_GROSSE_GEWICHT_LESEN),
    withOneOfBerechtigungen(Berechtigung.SUPPORT),
  );
}

export function hasGroesseGewichtEditBerechtigung(): RoleMatcher {
  return withOneOfMatchers(
    withAllOfBerechtigungen(Berechtigung.SU_GROSSE_GEWICHT_LESEN, Berechtigung.SU_GROSSE_GEWICHT_SCHREIBEN),
    withOneOfBerechtigungen(Berechtigung.SUPPORT),
  );
}

export function hasSehenViewBerechtigung(): RoleMatcher {
  return withOneOfMatchers(
    withAllOfBerechtigungen(Berechtigung.SU_SEHEN_LESEN),
    withOneOfBerechtigungen(Berechtigung.SUPPORT),
  );
}

export function hasImpfungenEditBerechtigung(): RoleMatcher {
  return withOneOfMatchers(
    withAllOfBerechtigungen(Berechtigung.SU_IMPFEN_LESEN, Berechtigung.SU_IMPFEN_SCHREIBEN),
    withOneOfBerechtigungen(Berechtigung.SUPPORT),
  );
}

export function hasImpfungenViewBerechtigung(): RoleMatcher {
  return withOneOfMatchers(
    withAllOfBerechtigungen(Berechtigung.SU_IMPFEN_LESEN),
    withOneOfBerechtigungen(Berechtigung.SUPPORT),
  );
}

export function hasHoerenViewBerechtigung(): RoleMatcher {
  return withOneOfMatchers(
    withAllOfBerechtigungen(Berechtigung.SU_HOREN_LESEN),
    withOneOfBerechtigungen(Berechtigung.SUPPORT),
  );
}

export function hasHoerenEditBerechtigung(): RoleMatcher {
  return withOneOfMatchers(
    withAllOfBerechtigungen(Berechtigung.SU_HOREN_LESEN, Berechtigung.SU_HOREN_SCHREIBEN),
    withOneOfBerechtigungen(Berechtigung.SUPPORT),
  );
}

export function hasTriageViewBerechtigung(): RoleMatcher {
  return withOneOfMatchers(
    withAllOfBerechtigungen(Berechtigung.SU_TRIAGE_LESEN),
    withOneOfBerechtigungen(Berechtigung.SUPPORT),
  );
}

export function hasAerztlicheUntersuchungViewBerechtigung(): RoleMatcher {
  return withOneOfMatchers(
    withAllOfBerechtigungen(Berechtigung.SU_AERZTLICHE_UNTERSUCHUNG_LESEN),
    withOneOfBerechtigungen(Berechtigung.SUPPORT),
  );
}

export function hasAerztlicheUntersuchungEditBerechtigung(): RoleMatcher {
  return withOneOfMatchers(
    withAllOfBerechtigungen(
      Berechtigung.SU_AERZTLICHE_UNTERSUCHUNG_LESEN,
      Berechtigung.SU_AERZTLICHE_UNTERSUCHUNG_SCHREIBEN,
    ),
    withOneOfBerechtigungen(Berechtigung.SUPPORT),
  );
}

export function hasLaeusekontrolleViewBerechtigung(): RoleMatcher {
  return withOneOfMatchers(
    withAllOfBerechtigungen(Berechtigung.SU_LAEUSE_LESEN),
    withOneOfBerechtigungen(Berechtigung.SUPPORT),
  );
}

export function hasLaeusekontrolleEditBerechtigung(): RoleMatcher {
  return withOneOfMatchers(
    withAllOfBerechtigungen(
      Berechtigung.SU_LAEUSE_LESEN,
      Berechtigung.SU_LAEUSE_SCHREIBEN,
    ),
    withOneOfBerechtigungen(Berechtigung.SUPPORT),
  );
}

export function hasMassnahmenViewBerechtigung(): RoleMatcher {
  return withOneOfMatchers(
    withAllOfBerechtigungen(Berechtigung.SU_MASSNAHMEN_LESEN),
    withOneOfBerechtigungen(Berechtigung.SUPPORT),
  );
}

export function hasAufgabenViewBerechtigungen(): RoleMatcher {
  return withOneOfMatchers(
    withAllOfBerechtigungen(Berechtigung.AUFGABEN_LESEN),
    withOneOfBerechtigungen(Berechtigung.SUPPORT),
  );
}

export function hasAufgabenEditBerechtigungen(): RoleMatcher {
  return withOneOfMatchers(
    withAllOfBerechtigungen(Berechtigung.AUFGABEN_LESEN, Berechtigung.AUFGABEN_SCHREIBEN),
    withOneOfBerechtigungen(Berechtigung.SUPPORT),
  );
}

export function hasReportsViewBerechtigung(): RoleMatcher {
  return withOneOfMatchers(
    withAllOfBerechtigungen(Berechtigung.SPSS_EXPORT),
    withOneOfBerechtigungen(Berechtigung.SUPPORT),
  );
}

export function hasDuplikateBerechtigung(): RoleMatcher {
  return withOneOfMatchers(
    withAllOfBerechtigungen(Berechtigung.DUBLETTEN_ZUSAMMENFUHREN),
    withOneOfBerechtigungen(Berechtigung.SUPPORT),
  );
}
