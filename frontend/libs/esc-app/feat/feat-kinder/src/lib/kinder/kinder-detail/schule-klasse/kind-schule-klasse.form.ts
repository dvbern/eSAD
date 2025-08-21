import {DestroyRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {NonNullableFormBuilder} from '@angular/forms';
import {KindSaveJson, KlasseOptionJson, SchuleOptionJson} from '@dvb/data-access-backend';
import {KlassenOptionsStore} from '@dvb/feat-shared';
import {BaseForm} from '@dvb/util-angular';
import {requireValue} from '@dvb/util-shared';
import {distinctUntilChanged} from 'rxjs';

type Config = {
  fb: NonNullableFormBuilder;
  destroyRef: DestroyRef;
  klassenStore: KlassenOptionsStore;
};

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    schule: fb.control<SchuleOptionJson | undefined>(undefined, []),
    klasse: fb.control<KlasseOptionJson | undefined>(undefined, []),
  });


  group.controls.schule
    .valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef)).subscribe({
    next: (schule: SchuleOptionJson | undefined) => {
      if (schule) {
        config.klassenStore.loadKlasseOptions({schuleId: schule.id, onlyActive: true});
        group.controls.klasse.reset();
      } else {
        config.klassenStore.resetKlasseOptions();
      }
    },
    error: console.error,
  });

  return group;
}

export class KindSchuleKlasseForm extends BaseForm<ReturnType<typeof makeForm>, Partial<KindSaveJson>> {
  constructor(config: Config) {
    super(() => makeForm(config));
  }

  override valueOnValid(): Partial<KindSaveJson> {
    const value = requireValue(this.group.getRawValue());
    return {
      schule: value.schule ?? undefined,
      klasse: value.klasse ?? undefined,
    };
  }

}

