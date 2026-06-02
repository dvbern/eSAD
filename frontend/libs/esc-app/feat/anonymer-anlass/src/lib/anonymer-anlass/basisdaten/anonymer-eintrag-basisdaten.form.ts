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
import {AnlassTyp, AnonymerEintragCreateJson, Geschlecht} from '@dvb/data-access-backend';
import {AppValidators, BaseForm, DetailMode} from '@dvb/util-angular';
import {ensureProps, requireValue} from '@dvb/util-shared';
import {ISODateString} from "@dvb/data-access-types";

type Config = {
  fb: NonNullableFormBuilder;
  destroyRef: DestroyRef;
};

function makeForm(config: Config) {
  const fb = config.fb;
  const today = new Date();
  return fb.group({
    anlassTyp: fb.control<AnlassTyp | undefined>(undefined, Validators.required),
    anlassDatum: fb.control<ISODateString | undefined>(undefined,
      [Validators.required, AppValidators.nonFutureDates(() => true, today)]),
    kindName: fb.control<string>('', [Validators.required, AppValidators.dbMaxLength()]),
    kindVorname: fb.control<string>('', [Validators.required, AppValidators.dbMaxLength()]),
    kindGeschlecht: fb.control<Geschlecht | undefined>(undefined, [Validators.required]),
    kindGeburtsdatum: fb.control<ISODateString | undefined>(undefined,
      [Validators.required, AppValidators.geburtsdatumKindValidator(today)]),
  });
}

export class AnonymerEintragBasisdatenForm extends BaseForm<
  ReturnType<typeof makeForm>,
  AnonymerEintragCreateJson
> {
  constructor(protected config: Config) {
    super(() => makeForm(config));
  }

  resetForm(value: AnonymerEintragCreateJson | undefined): void {
    super.reset(value, DetailMode.EDIT);
  }

  override valueOnValid(): AnonymerEintragCreateJson {
    const value = requireValue(this.group.getRawValue());
    return ensureProps<AnonymerEintragCreateJson>({
      anlassTyp: requireValue(value.anlassTyp),
      anlassDatum: requireValue(value.anlassDatum),
      kindName: requireValue(value.kindName),
      kindVorname: requireValue(value.kindVorname),
      kindGeschlecht: requireValue(value.kindGeschlecht),
      kindGeburtsdatum: requireValue(value.kindGeburtsdatum),
    });
  }
}
