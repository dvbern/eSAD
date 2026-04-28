import {NonNullableFormBuilder, Validators} from "@angular/forms";
import { globalConstants } from '../../constants/global-constants';
import {ensurePropsFlat, requireValue, trimToUndefined} from "@dvb/util-shared";
import {
  BenutzerSaveJson,
  EntityAktivStatus,
  FachRolle,
  OrganisationRollenJson, SystemRollenJson
} from "@dvb/data-access-backend";
import {AppValidators, BaseForm, DetailMode} from "@dvb/util-angular";
import {BenutzerEnhancedJson} from "../../admin-shared/benutzer-list.store";
import { TenantSettingsStore } from "../../tenant/tenant-settings.store";

type Config = {
  fb: NonNullableFormBuilder;
  tenantSettingStore: TenantSettingsStore;
};

function makeForm(config: Config) {
  const fb = config.fb;
  const maxLength = AppValidators.dbMaxLength();
  return fb.group({
    titel: fb.control<string | undefined>(undefined, maxLength),
    name: fb.control<string | undefined>(undefined, [Validators.required, maxLength]),
    vorname: fb.control<string | undefined>(undefined, [Validators.required, maxLength]),
    email: fb.control<string | undefined>({value: undefined, disabled: true},
      [Validators.required, maxLength, AppValidators.emailValidator()]),
    telefon: fb.control<string | undefined>(undefined, [maxLength, AppValidators.phoneValidator()]),
    benutzerAktivStatus: fb.control<EntityAktivStatus>(EntityAktivStatus.AKTIV, Validators.required),
    fachRollen: fb.control<FachRolle[]>([]),
    organisationRollen: fb.control<OrganisationRollenJson[]>([]),
    systemRollen: fb.control<SystemRollenJson | undefined>({value: undefined, disabled: true}),
    version: fb.control<number | undefined>({value: undefined, disabled: true}),
  });
}

export class BenutzerDetailsForm extends BaseForm<ReturnType<typeof makeForm>, BenutzerSaveJson> {
  constructor(config: Config) {
    super(() => makeForm(config))
  }

  resetForm(value: BenutzerEnhancedJson, mode: DetailMode) {
    super.reset(value.data, mode);
    this.group.controls.fachRollen.reset(value.fachRollen);
  }

  override valueOnValid(): BenutzerSaveJson {
    const values = this.group.getRawValue();

    const organisationsRollen = values.fachRollen.filter(rolle => rolle !== FachRolle.SYSTEM_ADMIN && rolle !== FachRolle.SUPPORT);
    if (organisationsRollen) {
      if (values.organisationRollen.length > 0) {
        values.organisationRollen[0].fachRollen = organisationsRollen;
      } else {
        values.organisationRollen.push({fachRollen: organisationsRollen, organisation: undefined, berechtigungen: []});
      }
    }

    const systemRollen = values.fachRollen.filter(rolle => rolle === FachRolle.SYSTEM_ADMIN || rolle === FachRolle.SUPPORT);
    if (systemRollen) {
      if (values.systemRollen) {
        values.systemRollen.fachRollen = systemRollen;
      } else {
        values.systemRollen = {fachRollen: systemRollen, berechtigungen: []};
      }
    }

    return ensurePropsFlat<BenutzerSaveJson>({
      titel: trimToUndefined(values.titel),
      name: requireValue(values.name),
      vorname: requireValue(values.vorname),
      fullName: '', // wird berechnet im mapper
      username: '', // wird berechnet im mapper
      email: requireValue(values.email),
      telefon: values.telefon,
      benutzerAktivStatus: values.benutzerAktivStatus,
      organisationRollen: values.organisationRollen,
      systemRollen: values.systemRollen,
      version: values.version ?? globalConstants.entityPrePersistVersion,
    });
  }
}
