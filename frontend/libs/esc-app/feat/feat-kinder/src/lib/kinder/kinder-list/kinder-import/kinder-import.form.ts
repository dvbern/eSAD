/*
 * Copyright (C) 2025 DV Bern AG, Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {DestroyRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {KlasseOptionJson, SchuleOptionJson} from '@dvb/data-access-backend';
import {KlassenOptionsStore} from '@dvb/feat-shared';
import {BaseForm} from '@dvb/util-angular';
import {ensurePropsFlat} from '@dvb/util-shared';
import {distinctUntilChanged} from 'rxjs';

export interface KinderImportJson {
  standardImport: boolean,
  schule: SchuleOptionJson | undefined,
  klasse: KlasseOptionJson | undefined,
}

type Config = {
  fb: NonNullableFormBuilder,
  destroyRef: DestroyRef,
  klassenOptionsStore: KlassenOptionsStore,
}

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    standardImport: fb.control<boolean>(true, Validators.required),
    schule: fb.control<SchuleOptionJson | undefined>(undefined, Validators.required),
    klasse: fb.control<KlasseOptionJson | undefined>(undefined, Validators.required),
  });

  group.controls.standardImport.valueChanges.pipe(takeUntilDestroyed(config.destroyRef), distinctUntilChanged())
    .subscribe(
      value => {
        console.log(value);
        if (value) {
          group.controls.klasse.addValidators(Validators.required);
        } else {
          group.controls.klasse.clearValidators();
          group.controls.klasse.setValue(undefined);
        }
      },
    );

  group.controls.schule.valueChanges.pipe(takeUntilDestroyed(config.destroyRef), distinctUntilChanged()).subscribe({
    next: (schule: SchuleOptionJson | undefined) => {
      if (schule) {
        config.klassenOptionsStore.loadKlasseOptions({schuleId: schule.id, onlyActive: true});
        group.controls.klasse.reset();
      } else {
        config.klassenOptionsStore.resetKlasseOptions();
      }
    },
    error: console.error,
  });

  return group;
}

export class KinderImportForm extends BaseForm<
  ReturnType<typeof makeForm>,
  KinderImportJson
> {
  constructor(config: Config) {
    super(() => makeForm(config));
  }

  isStandardImportSelected(): boolean {
    return this.group.get('standardImport')?.value === true;
  }

  valueOnValid(): KinderImportJson {
    const value = this.group.getRawValue();
    return ensurePropsFlat<KinderImportJson>({
      standardImport: value.standardImport,
      schule: value.schule,
      klasse: value.klasse,
    })
  }
}
