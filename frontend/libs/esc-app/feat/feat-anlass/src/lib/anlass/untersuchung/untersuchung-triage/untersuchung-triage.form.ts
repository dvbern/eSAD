import {DestroyRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {NonNullableFormBuilder} from '@angular/forms';
import {NaechsteStation, UntersuchungTriageSaveJson} from '@dvb/data-access-backend';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {ensurePropsFlat, requireValue} from '@dvb/util-shared';
import {distinctUntilChanged} from 'rxjs';

type Config = {
  fb: NonNullableFormBuilder,
  destroyRef: DestroyRef
}

function makeForm(config: Config) {
  const fb = config.fb;

  const group = fb.group({
    triageSu: fb.control(false),
    triageSuBemerkungen: fb.control('', AppValidators.textareaMaxLength()),
    triageMpa: fb.control(false),
    triageMpaBemerkungen: fb.control('', AppValidators.textareaMaxLength()),
    befundSu: fb.control(false),
    befundSuBemerkungen: fb.control('', AppValidators.textareaMaxLength()),
    wunschSchueler: fb.control(false),
    wunschSchuelerBemerkungen: fb.control('', AppValidators.textareaMaxLength()),
    vorinformationLehrperson: fb.control(false),
    vorinformationLehrpersonBemerkungen: fb.control('', AppValidators.textareaMaxLength()),
    naechsteStation: fb.control<NaechsteStation | undefined>(undefined),
  });

  group.controls.triageSu.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: value => {
        if (!value) {
          group.controls.triageSuBemerkungen.reset();
        }
      },
      error: console.error,
    });

  group.controls.triageMpa.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: value => {
        if (!value) {
          group.controls.triageMpaBemerkungen.reset();
        }
      },
      error: console.error,
    });

  group.controls.befundSu.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: value => {
        if (!value) {
          group.controls.befundSuBemerkungen.reset();
        }
      },
      error: console.error,
    });

  group.controls.wunschSchueler.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: value => {
        if (!value) {
          group.controls.wunschSchuelerBemerkungen.reset();
        }
      },
      error: console.error,
    });

  group.controls.vorinformationLehrperson.valueChanges.pipe(distinctUntilChanged(),
    takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: value => {
        if (!value) {
          group.controls.vorinformationLehrpersonBemerkungen.reset();
        }
      },
      error: console.error,
    });

  return group;
}

export class UntersuchungTriageForm extends BaseForm<ReturnType<typeof makeForm>, UntersuchungTriageSaveJson> {
  constructor(config: Config) {
    super(() => makeForm(config));
  }

  public override valueOnValid(): UntersuchungTriageSaveJson {
    const value = requireValue(this.group.getRawValue());
    return ensurePropsFlat<UntersuchungTriageSaveJson>({
      triageSu: value.triageSu,
      triageSuBemerkungen: value.triageSuBemerkungen,
      triageMpa: value.triageMpa,
      triageMpaBemerkungen: value.triageMpaBemerkungen,
      befundSu: value.befundSu,
      befundSuBemerkungen: value.befundSuBemerkungen,
      wunschSchueler: value.wunschSchueler,
      wunschSchuelerBemerkungen: value.wunschSchuelerBemerkungen,
      vorinformationLehrperson: value.vorinformationLehrperson,
      vorinformationLehrpersonBemerkungen: value.vorinformationLehrpersonBemerkungen,
      naechsteStation: value.naechsteStation,
    });
  }
}
