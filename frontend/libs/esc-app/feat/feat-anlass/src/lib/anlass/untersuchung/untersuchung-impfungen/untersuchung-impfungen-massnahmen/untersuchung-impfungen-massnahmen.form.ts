import { DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, Validators } from '@angular/forms';
import {
  AnlassConfigJson,
  ImpfungenMassnahmenEmpfehlung,
  InformationEltern,
  RequirementSetting,
  UntersuchungImpfungenMassnahmenSaveJson,
} from '@dvb/data-access-backend';
import { globalConstants } from '@dvb/feat-shared';
import { AppValidators, BaseForm } from '@dvb/util-angular';
import { ensureProps, Nullish, requireValue, trimToUndefined } from '@dvb/util-shared';
import { distinctUntilChanged } from 'rxjs';
import { markAllMultiCheckboxesAsTouched } from '../../untersuchung.util';
import {BackendEntityId} from "@dvb/data-access-types";

type Config = {
  fb: NonNullableFormBuilder;
  destroyRef: DestroyRef;
};

function makeEmpfehlungSubForm(fb: NonNullableFormBuilder, value: ImpfungenMassnahmenEmpfehlung) {
  return fb.group({
    resultat: fb.control<ImpfungenMassnahmenEmpfehlung>({ value, disabled: true }),
    isChecked: fb.control<boolean>(false),
  });
}

export type EmpfehlungSubFormType = ReturnType<typeof makeEmpfehlungSubForm>;

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    keineMassnahmen: fb.control<boolean>(false),
    vormerkenFuerGruppenFolgetermin: fb.control<boolean>(false),
    impfberatungMpaDurchgefuehrt: fb.control<boolean>(false),
    impfberatungMpaDurchgefuehrtBemerkungen: fb.control<string>('', AppValidators.textareaMaxLength2000()),
    infomaterialAbgegeben: fb.control<boolean>(false),
    informationEltern: fb.control<InformationEltern | undefined>(undefined),
    informationHausarzt: fb.control<boolean>(false),
    rueckfrageHausarzt: fb.control<boolean>(false),
    empfehlungArray: fb.array<EmpfehlungSubFormType>([]),
    achtung: fb.control<string>('', AppValidators.textareaMaxLength2000()),
    bemerkungen: fb.control<string>('', AppValidators.textareaMaxLength2000()),
    folgeTerminImpfen: fb.control<BackendEntityId | Nullish>(undefined),
    version: fb.control<number | Nullish>({ value: undefined, disabled: true }),
  });

  group.controls.impfberatungMpaDurchgefuehrt.valueChanges
    .pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: value => {
        if (!value) {
          group.controls.impfberatungMpaDurchgefuehrtBemerkungen.reset();
        }
      },
      error: console.error,
    });

  // Multi-Checkboxen: Wenn eines touched ist aber keines ausgewählt, müssen alle rot werden
  markAllMultiCheckboxesAsTouched(group.controls.empfehlungArray, config.destroyRef);

  return group;
}

export class UntersuchungImpfungenMassnahmenForm extends BaseForm<
  ReturnType<typeof makeForm>,
  UntersuchungImpfungenMassnahmenSaveJson
> {
  constructor(protected config: Config) {
    super(() => makeForm(config));
  }

  resetForm(
    value: UntersuchungImpfungenMassnahmenSaveJson | undefined,
    anlassConfig: AnlassConfigJson | undefined
  ): void {
    this.group.reset(value);
    this.resetEmpfehlung(value, anlassConfig);
  }

  private resetEmpfehlung(
    value: UntersuchungImpfungenMassnahmenSaveJson | Nullish,
    anlassConfig: AnlassConfigJson | undefined
  ): void {
    this.group.controls.empfehlungArray.clear();
    if (anlassConfig?.data?.untersuchungImpfungEmpfehlungValues) {
      for (const resultat of anlassConfig.data.untersuchungImpfungEmpfehlungValues) {
        const subForm = makeEmpfehlungSubForm(this.config.fb, resultat);
        subForm.controls.isChecked.reset(!!value?.empfehlung?.find(fv => fv === resultat));
        this.group.controls.empfehlungArray.push(subForm);
      }
    }
  }

  setValidatorsForAnlassConfig(config: AnlassConfigJson): void {
    if (config.data.untersuchungImpfungAchtung === RequirementSetting.REQUIRED) {
      this.group.controls.achtung.addValidators(Validators.required);
    }
  }

  public getAndResetImpfFolgetermin() : BackendEntityId | Nullish {
    const anlassId = this.group.getRawValue().folgeTerminImpfen;
    this.group.controls.folgeTerminImpfen.reset();
    return anlassId;
  }

  public override valueOnValid(): UntersuchungImpfungenMassnahmenSaveJson {
    const value = requireValue(this.group.getRawValue());
    return ensureProps<UntersuchungImpfungenMassnahmenSaveJson>({
      keineMassnahmen: value.keineMassnahmen,
      vormerkenFuerGruppenFolgetermin: value.vormerkenFuerGruppenFolgetermin,
      impfberatungMpaDurchgefuehrt: value.impfberatungMpaDurchgefuehrt,
      impfberatungMpaDurchgefuehrtBemerkungen: trimToUndefined(value.impfberatungMpaDurchgefuehrtBemerkungen),
      infomaterialAbgegeben: value.infomaterialAbgegeben,
      informationEltern: value.informationEltern,
      informationHausarzt: value.informationHausarzt,
      rueckfrageHausarzt: value.rueckfrageHausarzt,
      empfehlung: value.empfehlungArray.filter(i => i.isChecked).map(i => i.resultat),
      achtung: trimToUndefined(value.achtung),
      bemerkungen: trimToUndefined(value.bemerkungen),
      version: value.version ?? globalConstants.entityPrePersistVersion,
    });
  }
}
