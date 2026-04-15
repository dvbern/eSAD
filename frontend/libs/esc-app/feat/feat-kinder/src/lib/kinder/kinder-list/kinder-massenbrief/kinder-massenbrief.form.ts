/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
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
import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {DocumentQueueMassenbriefSaveJson} from '@dvb/data-access-backend';
import {BaseForm} from '@dvb/util-angular';
import {ensurePropsFlat, requireValue} from '@dvb/util-shared';
import {BackendEntityId} from "@dvb/data-access-types";
import {KinderMassenbriefStore} from "./kinder-massenbrief.store";

type Config = {
  fb: NonNullableFormBuilder,
  destroyRef: DestroyRef,
  store: KinderMassenbriefStore
}

function makeForm(config: Config) {
  const fb = config.fb;
  const selectedKinder = config.store.selectedKinderIds();

  const group = fb.group({
    briefvorlageID: fb.control<BackendEntityId | undefined>(undefined, Validators.required),
    arztID: fb.control<BackendEntityId | undefined>(undefined),
    kinderIds: fb.control<BackendEntityId[] | undefined>(undefined, Validators.required),
  });
  group.controls.kinderIds.setValue(selectedKinder);
  return group;
}

export class KinderMassenbriefForm extends BaseForm<
  ReturnType<typeof makeForm>,
  DocumentQueueMassenbriefSaveJson
> {
  constructor(config: Config) {
    super(() => makeForm(config));
  }

  valueOnValid(): DocumentQueueMassenbriefSaveJson {
    const value = this.group.getRawValue();
    return ensurePropsFlat<DocumentQueueMassenbriefSaveJson>({
      briefvorlageID: requireValue(value.briefvorlageID),
      arztID: value.arztID,
      kinderIds: requireValue(value.kinderIds),
    })
  }
}
