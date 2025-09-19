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

import {DestroyRef} from '@angular/core';
import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {AnlassConfigJson, RequirementSetting, UntersuchungArztBemerkungSaveJson} from '@dvb/data-access-backend';
import {globalConstants} from '@dvb/feat-shared';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {ensureProps, Nullish, requireValue, trimToUndefined} from '@dvb/util-shared';

type Config = {
  fb: NonNullableFormBuilder,
  destroyRef: DestroyRef,
}

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    bemerkungen: fb.control<string>('', AppValidators.textareaMaxLength2000()),
    version: fb.control<number | Nullish>({value: undefined, disabled: true}),
  });
  return group;
}

export class UntersuchungArztBemerkungForm extends BaseForm<ReturnType<typeof makeForm>, UntersuchungArztBemerkungSaveJson> {

  constructor(protected config: Config) {
    super(() => makeForm(config));
  }

  resetForm(value: UntersuchungArztBemerkungSaveJson | Nullish): void {
    this.group.reset({
      ...value,
    });
  }

  public override valueOnValid(): UntersuchungArztBemerkungSaveJson {
    const value = requireValue(this.group.getRawValue());
    return ensureProps<UntersuchungArztBemerkungSaveJson>({
      bemerkungen: trimToUndefined(value.bemerkungen),
      version: value.version ?? globalConstants.entityPrePersistVersion,
    });
  }

  public setValidatorsForAnlassConfig(anlassConfig: AnlassConfigJson): void {
    if (anlassConfig.untersuchungBemerkungen === RequirementSetting.REQUIRED) {
      this.group.controls.bemerkungen.addValidators(Validators.required);
    }
  }
}
