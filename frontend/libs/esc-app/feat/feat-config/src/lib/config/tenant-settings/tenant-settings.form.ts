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

import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {AppLanguages, EinladungStatus, FeatureEnabledSetting, TenantSettingSaveJson} from '@dvb/data-access-backend';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {ensurePropsFlat, requireValue} from '@dvb/util-shared';

// Keep the structure and style similar to anlass-config-detail.form.ts but adapted for TenantSettingSaveJson

type Config = {
  fb: NonNullableFormBuilder;
};

function makeForm(config: Config) {
  const fb = config.fb;

  const featuresGroup = fb.group({
    esadIdFeature: fb.group({
      featureEnabled: fb.control<FeatureEnabledSetting>(FeatureEnabledSetting.DISABLED, [Validators.required]),
      enabledForStage: fb.control<boolean>({value: false, disabled: true}),
    }),
    laenderCodeFeature: fb.group({
      featureEnabled: fb.control<FeatureEnabledSetting>(FeatureEnabledSetting.DISABLED, [Validators.required]),
      enabledForStage: fb.control<boolean>({value: false, disabled: true}),
    }),
    mobileScannerFeature: fb.group({
      featureEnabled: fb.control<FeatureEnabledSetting>(FeatureEnabledSetting.DISABLED, [Validators.required]),
      enabledForStage: fb.control<boolean>({value: false, disabled: true}),
    }),
    massenimportFeature: fb.group({
      featureEnabled: fb.control<FeatureEnabledSetting>(FeatureEnabledSetting.DISABLED, [Validators.required]),
      enabledForStage: fb.control<boolean>({value: false, disabled: true}),
    }),

  });

  const group = fb.group({
    defaultLanguage: fb.control<AppLanguages>(AppLanguages.DE, Validators.required),
    supportedLanguages: fb.control<AppLanguages[]>([], Validators.required),
    folgeTermineMaxSlots: fb.control<number>(0, AppValidators.zeroDecimalPlaces()),
    einladungStatusValues: fb.control<EinladungStatus[]>([], Validators.required),
    features: featuresGroup,
  });

  return group;
}

export class TenantSettingsForm extends BaseForm<ReturnType<typeof makeForm>, TenantSettingSaveJson> {
  constructor(config: Config) {
    super(() => makeForm(config));
  }

  override valueOnValid(): TenantSettingSaveJson {
    const value = requireValue(this.group.getRawValue());

    const val = ensurePropsFlat({
      features: {
        esadIdFeature: {
          featureEnabled: value.features.esadIdFeature.featureEnabled,
          enabledForStage: value.features.esadIdFeature.enabledForStage,
        },
        laenderCodeFeature: {
          featureEnabled: value.features.laenderCodeFeature.featureEnabled,
          enabledForStage: value.features.laenderCodeFeature.enabledForStage,
        },
        mobileScannerFeature: {
          featureEnabled: value.features.mobileScannerFeature.featureEnabled,
          enabledForStage: value.features.mobileScannerFeature.enabledForStage,
        },
        massenimportFeature: {
          featureEnabled: value.features.massenimportFeature.featureEnabled,
          enabledForStage: value.features.massenimportFeature.enabledForStage,
        },
      },
      defaultLanguage: value.defaultLanguage,
      supportedLanguages: value.supportedLanguages,
      folgeTermineMaxSlots: value.folgeTermineMaxSlots,
      einladungStatusValues: value.einladungStatusValues,
    });
    return val;
  }
}
