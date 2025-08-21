import {DestroyRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {NonNullableFormBuilder} from '@angular/forms';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {ensureProps, trimToUndefined} from '@dvb/util-shared';
import {distinctUntilChanged} from 'rxjs';

export interface KinderZusatzinformationenJson {
  kinderschutzfall: boolean;
  keineAuskunftAnDrittpersonen: boolean;
  sansPapier: boolean;
  andereZusatzinformation?: boolean;
  andereZusatzInformationText?: string;
}

type Config = {
  fb: NonNullableFormBuilder,
  destroyRef: DestroyRef
}

function makeForm(config: Config) {
  const {fb, destroyRef} = config;
  const group = fb.group({
    kinderschutzfall: fb.control<boolean>(false),
    keineAuskunftAnDrittpersonen: fb.control<boolean>(false),
    sansPapier: fb.control<boolean>(false),
    andereZusatzinformation: fb.control<boolean>(false),
    andereZusatzInformationText: fb.control<string>('', AppValidators.textareaMaxLength()),
  });

  group.controls.andereZusatzinformation.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(destroyRef))
    .subscribe({
      next: value => {
        if (!value) {
          group.controls.andereZusatzInformationText.reset();
        }
      },
      error: console.error,
    });

  group.controls.andereZusatzInformationText.addValidators(AppValidators.requiredIfValidator(() => group.controls.andereZusatzinformation.value));

  return group;
}

export class KinderZusatzinformationenForm
  extends BaseForm<ReturnType<typeof makeForm>, KinderZusatzinformationenJson> {
  constructor(config: Config) {
    super(() => makeForm(config));
  }

  valueOnValid(): KinderZusatzinformationenJson {
    const value = this.group.getRawValue();
    return ensureProps<KinderZusatzinformationenJson>({
      kinderschutzfall: value.kinderschutzfall,
      keineAuskunftAnDrittpersonen: value.keineAuskunftAnDrittpersonen,
      sansPapier: value.sansPapier,
      andereZusatzinformation: value.andereZusatzinformation,
      andereZusatzInformationText: trimToUndefined(value.andereZusatzInformationText),
    });
  }
}
