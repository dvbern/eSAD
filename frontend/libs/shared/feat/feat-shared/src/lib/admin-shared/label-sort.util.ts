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

import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {FormControl} from '@angular/forms';
import {LabelJson} from '@dvb/data-access-backend';
import {Nullish} from '@dvb/util-shared';
import {TranslatedStringPipe} from '../translated-strings/view/translated-string.pipe';

/**
 * Compares two Labels by their translated name, so that Labels are always shown alphabetically.
 */
function compareLabelsByName(
  translatedStringPipe: TranslatedStringPipe,
): (a: LabelJson, b: LabelJson) => number {
  return (a: LabelJson, b: LabelJson) => translatedStringPipe.transform(a.data.name)
    .localeCompare(translatedStringPipe.transform(b.data.name));
}

/**
 * Returns a new, alphabetically sorted copy of the given Labels.
 */
export function sortLabelsByName(
  labels: LabelJson[] | Nullish,
  translatedStringPipe: TranslatedStringPipe,
): LabelJson[] {
  return [...labels ?? []].sort(compareLabelsByName(translatedStringPipe));
}

/**
 * Only aktive Labels may be edited, so they are the ones shown in the ng-select.
 */
export function sortAktivLabels(
  labels: LabelJson[] | Nullish,
  translatedStringPipe: TranslatedStringPipe,
): LabelJson[] {
  return sortLabelsByName(labels?.filter(label => label.data.aktiv), translatedStringPipe);
}

/**
 * Inaktive Labels are kept out of the ng-select and are only displayed as badges.
 */
export function sortInaktivLabels(
  labels: LabelJson[] | Nullish,
  translatedStringPipe: TranslatedStringPipe,
): LabelJson[] {
  return sortLabelsByName(labels?.filter(label => !label.data.aktiv), translatedStringPipe);
}

/**
 * ng-select renders the selected Labels in the order of the control value and appends every newly
 * selected Label at the end. Re-sorting the value keeps the badges alphabetical.
 *
 * Must be called from an injection context, the subscription ends when its owner is destroyed.
 */
export function keepLabelsSorted(
  control: FormControl<LabelJson[]>,
  translatedStringPipe: TranslatedStringPipe,
): void {
  control.valueChanges
    .pipe(takeUntilDestroyed())
    .subscribe(labels => {
      const sorted = sortLabelsByName(labels, translatedStringPipe);
      if (sorted.some((label, index) => label.id !== labels[index].id)) {
        control.setValue(sorted, {emitEvent: false});
      }
    });
}
