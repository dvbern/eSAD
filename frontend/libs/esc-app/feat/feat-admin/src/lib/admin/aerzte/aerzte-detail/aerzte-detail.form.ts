/*
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

import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {ArztSaveJson, Fachrichtung, HpvTeilnahme, Organisation} from '@dvb/data-access-backend';
import {AdresseForm, globalConstants} from '@dvb/feat-shared';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {ensurePropsFlat, Nullish, requireFilledIn, requireValue, trimToUndefined} from '@dvb/util-shared';

type Config = {
    fb: NonNullableFormBuilder;
    adresseForm: AdresseForm;
};

function makeForm(config: Config) {
    const fb = config.fb;
    const maxLength = AppValidators.dbMaxLength();

    const group = fb.group({
        anrede: fb.control<string | Nullish>(undefined, [maxLength]),
        fachrichtung: fb.control<Fachrichtung | Nullish>(undefined, [Validators.required]),
        organisation: fb.control<Organisation | Nullish>(undefined, [Validators.required]),
        vorname: fb.control<string | Nullish>(undefined, [maxLength]),
        name: fb.control<string | Nullish>(undefined, [Validators.required, maxLength]),
        adresse: config.adresseForm.group,
        email: fb.control<string | Nullish>(undefined, [AppValidators.emailValidator(), maxLength]),
        telefon: fb.control<string | Nullish>(undefined, [AppValidators.phoneValidator(), maxLength]),
        bemerkungen: fb.control<string | Nullish>(undefined, [maxLength]),
        hpvTeilnahme: fb.control<HpvTeilnahme | Nullish>(undefined, [Validators.required]),
        version: fb.control<number | Nullish>({value: undefined, disabled: true}),
    });
    return group;
}

export class ArztDetailForm extends BaseForm<ReturnType<typeof makeForm>, ArztSaveJson> {
    readonly adresseForm: AdresseForm;

    constructor(config: Config) {
        super(() => makeForm(config));
        this.adresseForm = config.adresseForm;
    }

    override valueOnValid(): ArztSaveJson {
        const value = requireValue(this.group.getRawValue());

        return ensurePropsFlat<ArztSaveJson>({
            anrede: trimToUndefined(value.anrede),
            name: requireFilledIn(value.name),
            vorname: trimToUndefined(value.vorname),
            email: trimToUndefined(value.email),
            telefon: trimToUndefined(value.telefon),
            bemerkungen: trimToUndefined(value.bemerkungen),
            hpvTeilnahme: requireFilledIn(value.hpvTeilnahme),
            fachrichtung: requireFilledIn(value.fachrichtung),
            organisation: requireFilledIn(value.organisation),
            adresse: this.adresseForm.valueOnValid(),
            version: value.version ?? globalConstants.entityPrePersistVersion,
        });
    }
}
