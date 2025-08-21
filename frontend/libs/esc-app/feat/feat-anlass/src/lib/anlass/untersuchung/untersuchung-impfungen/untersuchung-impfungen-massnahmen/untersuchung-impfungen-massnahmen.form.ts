import {DestroyRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {NonNullableFormBuilder} from '@angular/forms';
import {InformationEltern, UntersuchungImpfungenMassnahmenSaveJson} from '@dvb/data-access-backend';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {ensureProps, Nullish, requireValue, trimToUndefined} from '@dvb/util-shared';
import {distinctUntilChanged} from 'rxjs';

type Config = {
  fb: NonNullableFormBuilder,
  destroyRef: DestroyRef,
}

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    keineMassnahmen: fb.control<boolean>(false),
    vormerkenFuerGruppenFolgetermin: fb.control<boolean>(false),
    impfberatungMpaDurchgefuehrt: fb.control<boolean>(false),
    impfberatungMpaDurchgefuehrtBemerkungen: fb.control<string>('', AppValidators.textareaMaxLength()),
    informationenAbgegeben: fb.control<boolean>(false),
    informationEltern: fb.control<InformationEltern | undefined>(undefined),
    informationHausarzt: fb.control<boolean>(false),
    rueckfrageHausarzt: fb.control<boolean>(false),
    bemerkungen: fb.control<string>('', AppValidators.textareaMaxLength()),
  });

  group.controls.impfberatungMpaDurchgefuehrt.valueChanges.pipe(distinctUntilChanged(),
    takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: value => {
        if (!value) {
          group.controls.impfberatungMpaDurchgefuehrtBemerkungen.reset();
        }
      },
      error: console.error,
    });

  return group;
}

export class UntersuchungImpfungenMassnahmenForm
  extends BaseForm<ReturnType<typeof makeForm>, UntersuchungImpfungenMassnahmenSaveJson> {

  constructor(protected config: Config) {
    super(() => makeForm(config));
  }

  resetForm(value: UntersuchungImpfungenMassnahmenSaveJson | Nullish): void {
    const valueToSet = {
      ...value,
      keineMassnahmen: value?.keineMassnahmen,
      vormerkenFuerGruppenFolgetermin: value?.vormerkenFuerGruppenFolgetermin,
      impfberatungMpaDurchgefuehrt: value?.impfberatungMpaDurchgefuehrt,
      impfberatungMpaDurchgefuehrtBemerkungen: value?.impfberatungMpaDurchgefuehrtBemerkungen,
      informationenAbgegeben: value?.informationenAbgegeben,
      informationEltern: value?.informationEltern,
      informationHausarzt: value?.informationHausarzt,
      rueckfrageHausarzt: value?.rueckfrageHausarzt,
      bemerkungen: value?.bemerkungen,
    };
    this.group.reset(valueToSet);
  }

  public override valueOnValid(): UntersuchungImpfungenMassnahmenSaveJson {
    const value = requireValue(this.group.getRawValue());
    return ensureProps<UntersuchungImpfungenMassnahmenSaveJson>({
      keineMassnahmen: value.keineMassnahmen,
      vormerkenFuerGruppenFolgetermin: value.vormerkenFuerGruppenFolgetermin,
      impfberatungMpaDurchgefuehrt: value.impfberatungMpaDurchgefuehrt,
      impfberatungMpaDurchgefuehrtBemerkungen: trimToUndefined(value.impfberatungMpaDurchgefuehrtBemerkungen),
      informationenAbgegeben: value.informationenAbgegeben,
      informationEltern: value.informationEltern,
      informationHausarzt: value.informationHausarzt,
      rueckfrageHausarzt: value.rueckfrageHausarzt,
      bemerkungen: trimToUndefined(value.bemerkungen),
    });
  }
}
