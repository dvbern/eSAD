/*
 * Paths are used in emails and bookmarks, touch them with care.
 */

/**
 * Important, synced to backend enum Path!!!
 * If you change a *value* here, make sure to check the equivalent enum Path in the backend!
 */
export enum Path {
  ADMIN = 'admin',
  SYSTEM = 'system',
  SCHULEN = 'schulen',
  STAMMDATEN = 'stammdaten',
  KONTAKTPERSONEN = 'kontaktpersonen',
  PROFIL = 'profil',
  PERSONENDATEN = 'personendaten',
  SCHULE_KLASSE = 'schule',
  KLASSEN = 'klassen',
  LABEL = 'label',
  INFOMATERIAL = 'infomaterial',
  KINDER = 'kinder',
  BENUTZER = 'benutzer',
  BRIEFVORLAGEN = 'briefvorlagen',
  SYSTEM_ADMIN = 'systemadmin',
  IMPFSTOFF = 'impfstoff',
  CHARGE = 'charge',
  DATEN = 'daten',
  LEHRPERSONEN = 'lehrpersonen',
  ANLASS = 'anlass',
  NOTIZEN = 'notizen',
  ANLASS_BEARBEITUNG = 'anlassbearbeitung',
  ANLASS_PLANUNG = 'anlassplanung',
  ANLASS_UNTERSUCHUNG = 'untersuchung',
  ANLASS_UNTERSUCHUNG_EINLADUNG = 'einladung',
  ANLASS_UNTERSUCHUNG_EINLADUNG_DATEN = 'untersuchungdaten',
  ANLASS_UNTERSUCHUNG_EINLADUNG_IMPFUNGEN = 'impfungen',
  ANLASS_UNTERSUCHUNG_EINLADUNG_SEHEN = 'sehen',
  ANLASS_UNTERSUCHUNG_EINLADUNG_HOEREN = 'hoeren',
  ANLASS_UNTERSUCHUNG_EINLADUNG_GROESSEGEWICHT = 'groessegewicht',
  ANLASS_UNTERSUCHUNG_EINLADUNG_TRIAGE = 'triage',
  ANLASS_UNTERSUCHUNG_EINLADUNG_ARZT = 'arzt',
  ANLASS_UNTERSUCHUNG_EINLADUNG_MASSNAHMEN = 'massnahmen',
  IMPFAUSWEIS = 'impfausweis',
  FRAGEBOGEN = 'fragebogen',
  AUFGABEN = 'aufgaben',
  SYSTEM_GENERAL = 'general',
  SYSTEM_JOB = 'jobs',
  REPORT = 'report',
  DUPLIKATE = 'duplikate',
  AERZTE = 'aerzte',

  NO_ROLES = 'no-roles',

  PLAYGROUND = 'playground',
  TYPORGRAPHY = 'typography',
  TABLE = 'table',
  FORM = 'form',
  COLUMNS = 'columns',
  DASHBOARD = 'dashboard',
  DIALOGS = 'dialogs',

  ALL = 'all',
  CREATE = 'create',
  RELATIVE_HERE = '.',
  RELATIVE_ONE_UP = '..',
  RELATIVE_TWO_UP = '../..',
  RELATIVE_THREE_UP = '../../..',
  RELATIVE_FOUR_UP = '../../../..',
}
