/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
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

import {LabelJson, TranslatedStringJson} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {Nullish} from '@dvb/util-shared';
import {TranslatedStringPipe} from '../translated-strings/view/translated-string.pipe';
import {sortAktivLabels, sortInaktivLabels, sortLabelsByName} from './label-sort.util';

// The real pipe would need the TranslocoService and the TenantSettingsStore, here we only care about the language
function translatedStringPipe(lang: 'de' | 'fr'): TranslatedStringPipe {
  return {
    transform: (value: TranslatedStringJson | Nullish) => value?.[lang] ?? '',
  } as TranslatedStringPipe;
}

function label(id: string, de: string, fr = de, aktiv = true): LabelJson {
  return {
    id: id as BackendEntityId,
    data: {name: {de, fr}, aktiv},
  } as LabelJson;
}

const impfausweis = label('1', 'Impfausweis', 'Certificat de vaccination');
const dispensation = label('2', 'Dispensationsgesuch', 'Demande de dispense');
const fragebogen = label('3', 'Fragebogen', 'Questionnaire');
const inaktivesAttest = label('4', 'Attest', 'Attestation', false);
const inaktivesZeugnis = label('5', 'Zeugnis', 'Certificat', false);

describe('sortLabelsByName', () => {
  it('should sort the labels alphabetically by their translated name', () => {
    const sorted = sortLabelsByName([impfausweis, fragebogen, dispensation], translatedStringPipe('de'));

    expect(sorted).toEqual([dispensation, fragebogen, impfausweis]);
  });

  it('should sort by the french name when french is the active language', () => {
    const sorted = sortLabelsByName([impfausweis, fragebogen, dispensation], translatedStringPipe('fr'));

    expect(sorted).toEqual([impfausweis, dispensation, fragebogen]);
  });

  it('should not modify the given array', () => {
    const labels = [impfausweis, fragebogen, dispensation];

    sortLabelsByName(labels, translatedStringPipe('de'));

    expect(labels).toEqual([impfausweis, fragebogen, dispensation]);
  });

  it('should return an empty array for nullish values', () => {
    expect(sortLabelsByName(undefined, translatedStringPipe('de'))).toEqual([]);
    expect(sortLabelsByName(null, translatedStringPipe('de'))).toEqual([]);
  });
});

describe('sortAktivLabels', () => {
  it('should keep only the aktive labels, alphabetically sorted', () => {
    const labels = [inaktivesZeugnis, impfausweis, inaktivesAttest, dispensation];

    expect(sortAktivLabels(labels, translatedStringPipe('de'))).toEqual([dispensation, impfausweis]);
  });
});

describe('sortInaktivLabels', () => {
  it('should keep only the inaktive labels, alphabetically sorted', () => {
    const labels = [inaktivesZeugnis, impfausweis, inaktivesAttest, dispensation];

    expect(sortInaktivLabels(labels, translatedStringPipe('de'))).toEqual([inaktivesAttest, inaktivesZeugnis]);
  });

  it('should return an empty array for nullish values', () => {
    expect(sortInaktivLabels(undefined, translatedStringPipe('de'))).toEqual([]);
  });
});
