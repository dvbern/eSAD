import {runId} from "./env.util";


export function withTestId(value :string): string{
  return `${value}-${runId}`;
}

export const testData = {
  anlass1 : {
    title: withTestId('Anlass 1'),
  },
  anlass2 : {
    title: withTestId('Anlass 2'),
  },
  anlass3 : {
    title: withTestId('Anlass 3'),
  },
  // The Anlass edit tests get their own one: they abmelden the Kind and take the Anlass out of
  // planning, which would break the Untersuchung tests running against the same Anlass.
  anlassEdit : {
    title: withTestId('Anlass Edit'),
  },
  notizTitle: withTestId('Test Notiz'),
  // Kinder the tests create for themselves, see createKind() for why two tests must not share one.
  kindForListFilter: {nachname: withTestId('Listenkind'), vorname: 'Lena'},
  kindForSearch: {nachname: withTestId('Suchkind'), vorname: 'Sam'},
  kindForNotiz: {nachname: withTestId('Notizkind'), vorname: 'Nina'},
  kindForNotizValidation: {nachname: withTestId('Validierungskind'), vorname: 'Valentina'},
  kindForNotizFileRemoval: {nachname: withTestId('Dateiloeschkind'), vorname: 'Dana'},
  // The seed Kind Anlass 3 is created from; the Untersuchung tests on Anlass 3 expect this Kind.
  kindForAnlass3: {nachname: 'Ammann', vorname: 'Fridolin'},
} as const;
