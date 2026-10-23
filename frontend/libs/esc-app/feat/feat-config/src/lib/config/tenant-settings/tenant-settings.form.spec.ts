import { TestBed } from '@angular/core/testing';
import { NonNullableFormBuilder } from '@angular/forms';
import {
  AgbResponsability,
  AppLanguages,
  EinladungStatus,
  FeatureEnabledSetting,
  LandingPageOption,
  LoginMethod,
  TenantSettingFeaturesJson,
  TenantSettingSaveJson,
} from '@dvb/data-access-backend';
import { DetailMode } from '@dvb/util-angular';
import { ensureProps } from '@dvb/util-shared';
import { TenantSettingsForm } from './tenant-settings.form';

function makeFeatures(): TenantSettingFeaturesJson {
  const featureState = { featureEnabled: FeatureEnabledSetting.DISABLED, enabledForStage: false };
  return {
    esadIdFeature: featureState,
    laenderCodeFeature: featureState,
    mobileScannerFeature: featureState,
    massenimportFeature: featureState,
    massenbriefFeature: featureState,
    nichtGeprueftGrundFeature: featureState,
    arztListeFeature: featureState,
    adHocImpfstoffChargenFeature: featureState,
    footerFeature: featureState,
    benutzerverwaltungInternFeature: featureState,
    benutzerverwaltungIgnoreFachRolleFromKeycloakFeature: featureState,
    archivierungFeature: featureState,
    zentraleOrganisationFeature: featureState,
    anonymeStatistikFeature: featureState,
    nachkontrolleFeature: featureState,
    anlassSpracheSpalteFeature: featureState,
    anlassSpracheZusammenfassungFeature: featureState,
    anlassInformationenFeature: featureState,
    anlassTitleRequiredFeature: featureState,
    tenantHeaderInExportsFeature: featureState,
  };
}

function makeValidDto(loginMethods: LoginMethod[]): TenantSettingSaveJson {
  return ensureProps<TenantSettingSaveJson>({
    defaultLanguage: AppLanguages.DE,
    supportedLanguages: [AppLanguages.DE],
    folgeTermineMaxSlots: 0,
    customerAdminEmails: 'admin@dvbern.ch',
    supportEmail: 'support@dvbern.ch',
    agbResponsability: AgbResponsability.AGB_MANAGED_BY_TENANT,
    defaultLandingPage: LandingPageOption.DASHBOARD,
    gueltigAbWachstumskurvePch: '2026-10-06',
    einladungStatusValues: [EinladungStatus.TEILNAHME_BESTAETIGT],
    loginMethods,
    features: makeFeatures(),
  });
}

function createForm(): TenantSettingsForm {
  return TestBed.runInInjectionContext(
    () => new TenantSettingsForm({ fb: TestBed.inject(NonNullableFormBuilder) }),
  );
}

describe('TenantSettingsForm', () => {
  describe('loginMethods', () => {
    it('maps the selected login methods into TenantSettingSaveJson on save', async () => {
      const form = createForm();
      form.reset(makeValidDto([LoginMethod.PASSWORD, LoginMethod.BE_LOGIN]), DetailMode.EDIT, undefined);

      expect(await form.validate()).toBe(true);
      expect(form.valueOnValid().loginMethods).toEqual([LoginMethod.PASSWORD, LoginMethod.BE_LOGIN]);
    });

    it('maps back into the form control when the settings are (re-)loaded', () => {
      const form = createForm();
      form.reset(makeValidDto([LoginMethod.HIN]), DetailMode.EDIT, undefined);

      expect(form.group.controls.loginMethods.value).toEqual([LoginMethod.HIN]);
    });

    it('blocks submit when no login method is selected (required-at-least-one)', async () => {
      const form = createForm();
      form.reset(makeValidDto([]), DetailMode.EDIT, undefined);

      expect(await form.validate()).toBe(false);
      expect(form.group.controls.loginMethods.errors).toEqual({ loginMethodsRequired: true });
    });

    it('is valid again once at least one login method is selected', async () => {
      const form = createForm();
      form.reset(makeValidDto([]), DetailMode.EDIT, undefined);
      expect(await form.validate()).toBe(false);

      form.group.controls.loginMethods.setValue([LoginMethod.PASSWORD]);

      expect(await form.validate()).toBe(true);
      expect(form.group.controls.loginMethods.errors).toBeNull();
    });
  });
});
