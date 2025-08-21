import {computed} from '@angular/core';
import {toSignal} from '@angular/core/rxjs-interop';
import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {
  AnlassOptionJson,
  AufgabeSaveJson,
  AufgabeStatus,
  BenutzerOptionJson,
  FileDataJson,
  KindJson,
  KindSearchJson,
  LabelJson,
} from '@dvb/data-access-backend';
import {BackendURL, isBackendURL, ISODateString} from '@dvb/data-access-types';
import { DeploymentStore } from '../../deployment/deployment.store';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {ensureProps, ensurePropsFlat, requireFilledIn, requireValue} from '@dvb/util-shared';

type Config = {
  fb: NonNullableFormBuilder;
  deploymentStore: DeploymentStore;
};

function makeForm(config: Config) {
  const { fb, deploymentStore } = config;
  return fb.group({
    titel: fb.control<string>('', [Validators.required, AppValidators.dbMaxLength()]),
    bemerkungen: fb.control<string>('', [AppValidators.textareaMaxLength()]),
    status: fb.control<AufgabeStatus>(AufgabeStatus.OFFEN, Validators.required),
    faelligBis: fb.control<ISODateString | undefined>(undefined,
      [Validators.required, AppValidators.aufgabeFaelligBisValidator(new Date())]),
    empfaenger: fb.control<BenutzerOptionJson | undefined>(undefined, Validators.required),
    userErfasst: fb.control<BenutzerOptionJson | undefined>({ value: undefined, disabled: true }),
    kind: fb.control<KindSearchJson | null>(null),
    anlass: fb.control<AnlassOptionJson | null>(null),
    labels: fb.control<LabelJson[]>([]),
    files: fb.control<FileDataJson[]>([]),
    link: fb.control<BackendURL | undefined>(undefined,
      [AppValidators.textareaMaxLength(), AppValidators.publicUrlValidator(deploymentStore.publicUrl())]),
  });
}

export class AufgabeDetailsForm extends BaseForm<ReturnType<typeof makeForm>, AufgabeSaveJson> {
  readonly deploymentStore: DeploymentStore;

  constructor(config: Config) {
    super(() => makeForm(config));
    this.deploymentStore = config.deploymentStore;
  }

  readonly kindChanged = toSignal(this.group.controls.kind.valueChanges);

  readonly #linkChanged = toSignal(this.group.controls.link.valueChanges);
  customLink = computed(() => {
    const link = this.#linkChanged();
    return this.group.controls.link.valid ? link : undefined;
  });

  setDefaultBenutzer(defaultBenutzer: BenutzerOptionJson) {
    this.group.controls.userErfasst.reset(defaultBenutzer);
    this.group.controls.empfaenger.reset(defaultBenutzer);
  }

  setDefaultKind(kind: KindJson): void {
    this.group.controls.kind.reset(
      ensureProps<KindSearchJson>({
        id: kind.id,
        esadId: kind.esadId,
        sozialversicherungsNummer: kind.data.sozialversicherungsNummer,
        name: kind.data.name,
        vorname: kind.data.vorname,
        geschlecht: kind.data.geschlecht,
        geburtsdatum: kind.data.geburtsdatum,
      }),
    );
  }

  valueOnValid(): AufgabeSaveJson {
    const value = requireValue(this.group.getRawValue());
    return ensurePropsFlat<AufgabeSaveJson>({
      titel: requireFilledIn(value.titel),
      bemerkungen: value.bemerkungen,
      status: requireFilledIn(value.status),
      faelligBis: requireFilledIn(value.faelligBis),
      empfaenger: requireFilledIn(value.empfaenger),
      userErfasst: requireFilledIn(value.userErfasst),
      kind: value.kind ?? undefined,
      anlass: value.anlass ?? undefined,
      labels: value.labels,
      files: value.files,
      link: isBackendURL(value.link) ? value.link : undefined,
    });
  }
}
