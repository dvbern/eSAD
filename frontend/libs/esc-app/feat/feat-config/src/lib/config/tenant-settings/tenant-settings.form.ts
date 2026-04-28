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
import {
  AgbResponsability,
  AppLanguages,
  EinladungStatus,
  FeatureEnabledSetting,
  LandingPageOption,
  TenantSettingSaveJson
} from '@dvb/data-access-backend';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {ensureProps, requireFilledIn, requireValue, trimToUndefined} from '@dvb/util-shared';

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
    massenbriefFeature: fb.group({
      featureEnabled: fb.control<FeatureEnabledSetting>(FeatureEnabledSetting.DISABLED, [Validators.required]),
      enabledForStage: fb.control<boolean>({value: false, disabled: true}),
    }),
    nichtGeprueftGrundFeature: fb.group({
      featureEnabled: fb.control<FeatureEnabledSetting>(FeatureEnabledSetting.DISABLED, [Validators.required]),
      enabledForStage: fb.control<boolean>({value: false, disabled: true}),
    }),
    arztListeFeature: fb.group({
      featureEnabled: fb.control<FeatureEnabledSetting>(FeatureEnabledSetting.DISABLED, [Validators.required]),
      enabledForStage: fb.control<boolean>({value: false, disabled: true}),
    }),
    adHocImpfstoffChargenFeature: fb.group({
      featureEnabled: fb.control<FeatureEnabledSetting>(FeatureEnabledSetting.DISABLED, [Validators.required]),
      enabledForStage: fb.control<boolean>({value: false, disabled: true}),
    }),
    footerFeature: fb.group({
      featureEnabled: fb.control<FeatureEnabledSetting>(FeatureEnabledSetting.DISABLED, [Validators.required]),
      enabledForStage: fb.control<boolean>({value: false, disabled: true}),
    }),
    benutzerverwaltungInternFeature: fb.group({
      featureEnabled: fb.control<FeatureEnabledSetting>(FeatureEnabledSetting.DISABLED, [Validators.required]),
      enabledForStage: fb.control<boolean>({value: false, disabled: true}),
    }),
    benutzerverwaltungIgnoreFachRolleFromKeycloakFeature: fb.group({
      featureEnabled: fb.control<FeatureEnabledSetting>(FeatureEnabledSetting.DISABLED, [Validators.required]),
      enabledForStage: fb.control<boolean>({value: false, disabled: true}),
    }),
    archivierungFeature: fb.group({
      featureEnabled: fb.control<FeatureEnabledSetting>(FeatureEnabledSetting.DISABLED, [Validators.required]),
      enabledForStage: fb.control<boolean>({value: false, disabled: true}),
    }),
    zentraleOrganisationFeature: fb.group({
      featureEnabled: fb.control<FeatureEnabledSetting>(FeatureEnabledSetting.DISABLED, [Validators.required]),
      enabledForStage: fb.control<boolean>({value: false, disabled: true}),
    }),
    anonymeStatistikFeature: fb.group({
      featureEnabled: fb.control<FeatureEnabledSetting>(FeatureEnabledSetting.DISABLED, [Validators.required]),
      enabledForStage: fb.control<boolean>({value: false, disabled: true}),
    }),
  });

  // ZentraleOrganisation kann nicht verändert werden
  featuresGroup.controls.zentraleOrganisationFeature.disable();

  return fb.group({
    defaultLanguage: fb.control<AppLanguages>(AppLanguages.DE, Validators.required),
    supportedLanguages: fb.control<AppLanguages[]>([], Validators.required),
    folgeTermineMaxSlots: fb.control<number>(0, AppValidators.zeroDecimalPlaces()),
    customerAdminEmails: fb.control<string>('', [Validators.required, AppValidators.multipleEmailValidator()]),
    supportEmail: fb.control<string>('', [Validators.required, AppValidators.emailValidator()]),
    agbResponsability: fb.control<AgbResponsability>(AgbResponsability.AGB_MANAGED_BY_TENANT, Validators.required),
    defaultLandingPage: fb.control<LandingPageOption>(LandingPageOption.DASHBOARD, Validators.required),
    einladungStatusValues: fb.control<EinladungStatus[]>([], Validators.required),
    features: featuresGroup,
  });
}

export class TenantSettingsForm extends BaseForm<ReturnType<typeof makeForm>, TenantSettingSaveJson> {
  constructor(config: Config) {
    super(() => makeForm(config));
  }

  override valueOnValid(): TenantSettingSaveJson {
    const value = requireValue(this.group.getRawValue());

    const val = ensureProps<TenantSettingSaveJson>({
      defaultLanguage: value.defaultLanguage,
      supportedLanguages: value.supportedLanguages,
      folgeTermineMaxSlots: value.folgeTermineMaxSlots,
      agbResponsability: value.agbResponsability,
      defaultLandingPage: value.defaultLandingPage,
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
        massenbriefFeature: {
          featureEnabled: value.features.massenbriefFeature.featureEnabled,
          enabledForStage: value.features.massenbriefFeature.enabledForStage,
        },
        nichtGeprueftGrundFeature: {
          featureEnabled: value.features.nichtGeprueftGrundFeature.featureEnabled,
          enabledForStage: value.features.nichtGeprueftGrundFeature.enabledForStage,
        },
        arztListeFeature: {
          featureEnabled: value.features.arztListeFeature.featureEnabled,
          enabledForStage: value.features.arztListeFeature.enabledForStage,
        },
        adHocImpfstoffChargenFeature: {
          featureEnabled: value.features.adHocImpfstoffChargenFeature.featureEnabled,
          enabledForStage: value.features.adHocImpfstoffChargenFeature.enabledForStage,
        },
        footerFeature: {
          featureEnabled: value.features.footerFeature.featureEnabled,
          enabledForStage: value.features.footerFeature.enabledForStage,
        },
        benutzerverwaltungInternFeature: {
          featureEnabled: value.features.benutzerverwaltungInternFeature.featureEnabled,
          enabledForStage: value.features.benutzerverwaltungInternFeature.enabledForStage,
        },
        benutzerverwaltungIgnoreFachRolleFromKeycloakFeature: {
          featureEnabled: value.features.benutzerverwaltungIgnoreFachRolleFromKeycloakFeature.featureEnabled,
          enabledForStage: value.features.benutzerverwaltungIgnoreFachRolleFromKeycloakFeature.enabledForStage,
        },
        archivierungFeature: {
          featureEnabled: value.features.archivierungFeature.featureEnabled,
          enabledForStage: value.features.archivierungFeature.enabledForStage,
        },
        zentraleOrganisationFeature: {
          featureEnabled: value.features.zentraleOrganisationFeature.featureEnabled,
          enabledForStage: value.features.zentraleOrganisationFeature.enabledForStage,
        },
        anonymeStatistikFeature: {
          featureEnabled: value.features.anonymeStatistikFeature.featureEnabled,
          enabledForStage: value.features.anonymeStatistikFeature.enabledForStage,
        }
      },
      einladungStatusValues: value.einladungStatusValues,
      customerAdminEmails: this.cleanCustomerAdminEmails(requireFilledIn(trimToUndefined(value.customerAdminEmails))),
      supportEmail: requireFilledIn(trimToUndefined(value.supportEmail)),
    });
    return val;
  }

  private cleanCustomerAdminEmails(emails: string): string {
    return emails
      // Soll Kommas am Anfang und am Ende entfernen;
      .replace(new RegExp(String.raw`^[${','}\s]+|[${','}\s]+$`, 'g'), '')
      // Ausserdem sollen sämtliche Leerzeichen entfernt werden.
      .replace(/\s/g, '')
      .trim();
  }
}
