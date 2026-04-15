import {DestroyRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {
  AnlassConfigJson,
  DistanzSehenResultat,
  RequirementSetting,
  UntersuchungSehenBeurteilungSaveJson,
} from '@dvb/data-access-backend';
import {globalConstants, ValueEnhancedForm} from '@dvb/feat-shared';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {ensurePropsFlat, Nullish, requireValue, trimToUndefined} from '@dvb/util-shared';
import {distinctUntilChanged} from 'rxjs';
import {checkIfArrayContainsCheckedValue} from '../../untersuchung.util';

type Config = {
  fb: NonNullableFormBuilder,
  destroyRef: DestroyRef,
  brilleLinsenEnhancedForm: ValueEnhancedForm;
  stereotestEnhancedForm: ValueEnhancedForm;
  farbsehenEnhancedForm: ValueEnhancedForm;
  verwendeteTafelEnhancedForm: ValueEnhancedForm;
  fernvisusAugeLinksEnhancedForm: ValueEnhancedForm;
  fernvisusAugeRechtsEnhancedForm: ValueEnhancedForm;
  motilitaetEnhancedForm: ValueEnhancedForm;
  strabismusAuspraegungEnhancedForm: ValueEnhancedForm;
  strabismusAuftretenEnhancedForm: ValueEnhancedForm;
  inspektionEnhancedForm: ValueEnhancedForm;
  konvergenzEnhancedForm: ValueEnhancedForm;
  fernvisusEnhancedForm: ValueEnhancedForm;
  nahvisusEnhancedForm: ValueEnhancedForm;
  strabismusEnhancedForm: ValueEnhancedForm;
  empfehlungEnhancedForm: ValueEnhancedForm;
}

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    brilleLinsen: config.brilleLinsenEnhancedForm.group,
    stereotest: config.stereotestEnhancedForm.group,
    farbsehen: config.farbsehenEnhancedForm.group,
    verwendeteTafel: config.verwendeteTafelEnhancedForm.group,
    fernvisus: config.fernvisusEnhancedForm.group,
    fernvisusRechts: fb.control<number | undefined>(undefined),
    fernvisusLinks: fb.control<number | undefined>(undefined),
    fernvisusAugeLinks: config.fernvisusAugeLinksEnhancedForm.group,
    fernvisusAugeRechts: config.fernvisusAugeRechtsEnhancedForm.group,
    nahvisus: config.nahvisusEnhancedForm.group,
    nahvisusRechts: fb.control<number | undefined>(undefined),
    nahvisusLinks: fb.control<number | undefined>(undefined),
    motilitaet: config.motilitaetEnhancedForm.group,
    strabismus: config.strabismusEnhancedForm.group,
    strabismusAuspraegung: config.strabismusAuspraegungEnhancedForm.group,
    strabismusAuftreten: config.strabismusAuftretenEnhancedForm.group,
    inspektion: config.inspektionEnhancedForm.group,
    konvergenz: config.konvergenzEnhancedForm.group,
    empfehlung: config.empfehlungEnhancedForm.group,
    bemerkungen: fb.control<string>('', AppValidators.textareaMaxLength2000()),
    version: fb.control<number | Nullish>({value: undefined, disabled: true}),
  });

  // Eine Eingabe aendert den required-Status eines anderen Feldes
  group.controls.fernvisus.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: _ => {
        group.controls.fernvisusRechts.updateValueAndValidity();
        group.controls.fernvisusLinks.updateValueAndValidity();
      },
      error: console.error,
    });
  group.controls.nahvisus.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: _ => {
        group.controls.nahvisusRechts.updateValueAndValidity();
        group.controls.nahvisusLinks.updateValueAndValidity();
      },
      error: console.error,
    });

  return group;
}

export class UntersuchungSehenBeurteilungForm
  extends BaseForm<ReturnType<typeof makeForm>, UntersuchungSehenBeurteilungSaveJson> {
  readonly brilleLinsenEnhancedForm: ValueEnhancedForm;
  readonly stereotestEnhancedForm: ValueEnhancedForm;
  readonly farbsehenEnhancedForm: ValueEnhancedForm;
  readonly verwendeteTafelEnhancedForm: ValueEnhancedForm;
  readonly fernvisusAugeLinksEnhancedForm: ValueEnhancedForm;
  readonly fernvisusAugeRechtsEnhancedForm: ValueEnhancedForm;
  readonly motilitaetEnhancedForm: ValueEnhancedForm;
  readonly strabismusAuspraegungEnhancedForm: ValueEnhancedForm;
  readonly strabismusAuftretenEnhancedForm: ValueEnhancedForm;
  readonly inspektionEnhancedForm: ValueEnhancedForm;
  readonly konvergenzEnhancedForm: ValueEnhancedForm;
  readonly fernvisusEnhancedForm: ValueEnhancedForm;
  readonly nahvisusEnhancedForm: ValueEnhancedForm;
  readonly strabismusEnhancedForm: ValueEnhancedForm;
  readonly empfehlungEnhancedForm: ValueEnhancedForm;

  constructor(protected config: Config) {
    super(() => makeForm(config));
    this.brilleLinsenEnhancedForm = config.brilleLinsenEnhancedForm;
    this.stereotestEnhancedForm = config.stereotestEnhancedForm;
    this.farbsehenEnhancedForm = config.farbsehenEnhancedForm;
    this.verwendeteTafelEnhancedForm = config.verwendeteTafelEnhancedForm;
    this.fernvisusAugeLinksEnhancedForm = config.fernvisusAugeLinksEnhancedForm;
    this.fernvisusAugeRechtsEnhancedForm = config.fernvisusAugeRechtsEnhancedForm;
    this.motilitaetEnhancedForm = config.motilitaetEnhancedForm;
    this.strabismusAuspraegungEnhancedForm = config.strabismusAuspraegungEnhancedForm;
    this.strabismusAuftretenEnhancedForm = config.strabismusAuftretenEnhancedForm;
    this.inspektionEnhancedForm = config.inspektionEnhancedForm;
    this.konvergenzEnhancedForm = config.konvergenzEnhancedForm;
    this.fernvisusEnhancedForm = config.fernvisusEnhancedForm;
    this.nahvisusEnhancedForm = config.nahvisusEnhancedForm;
    this.strabismusEnhancedForm = config.strabismusEnhancedForm;
    this.empfehlungEnhancedForm = config.empfehlungEnhancedForm;
  }

  resetForm(
    value: UntersuchungSehenBeurteilungSaveJson | undefined,
  ): void {
    super.reset(value);
    this.brilleLinsenEnhancedForm.resetForm(value?.brilleLinsen);
    this.stereotestEnhancedForm.resetForm(value?.stereotest);
    this.farbsehenEnhancedForm.resetForm(value?.farbsehen);
    this.verwendeteTafelEnhancedForm.resetForm(value?.verwendeteTafel);
    this.fernvisusAugeLinksEnhancedForm.resetForm(value?.fernvisusAugeLinks);
    this.fernvisusAugeRechtsEnhancedForm.resetForm(value?.fernvisusAugeRechts);
    this.motilitaetEnhancedForm.resetForm(value?.motilitaet);
    this.strabismusAuspraegungEnhancedForm.resetForm(value?.strabismusAuspraegung);
    this.strabismusAuftretenEnhancedForm.resetForm(value?.strabismusAuftreten);
    this.inspektionEnhancedForm.resetForm(value?.inspektion);
    this.konvergenzEnhancedForm.resetForm(value?.konvergenz);
    this.fernvisusEnhancedForm.resetForm(value?.fernvisus);
    this.nahvisusEnhancedForm.resetForm(value?.nahvisus);
    this.strabismusEnhancedForm.resetForm(value?.strabismus);
    this.empfehlungEnhancedForm.resetForm(value?.empfehlung);
  }

  public override valueOnValid(): UntersuchungSehenBeurteilungSaveJson {
    const value = requireValue(this.group.getRawValue());
    return ensurePropsFlat<UntersuchungSehenBeurteilungSaveJson>({
      brilleLinsen: this.brilleLinsenEnhancedForm.valueOnValid(),
      stereotest: this.stereotestEnhancedForm.valueOnValid(),
      farbsehen: this.farbsehenEnhancedForm.valueOnValid(),
      verwendeteTafel: this.verwendeteTafelEnhancedForm.valueOnValid(),
      fernvisus: this.fernvisusEnhancedForm.valueOnValid(),
      fernvisusRechts: value.fernvisusRechts,
      fernvisusLinks: value.fernvisusLinks,
      fernvisusAugeLinks: this.fernvisusAugeLinksEnhancedForm.valueOnValid(),
      fernvisusAugeRechts: this.fernvisusAugeRechtsEnhancedForm.valueOnValid(),
      nahvisus: this.nahvisusEnhancedForm.valueOnValid(),
      nahvisusRechts: value.nahvisusRechts,
      nahvisusLinks: value.nahvisusLinks,
      motilitaet: this.motilitaetEnhancedForm.valueOnValid(),
      inspektion: this.inspektionEnhancedForm.valueOnValid(),
      konvergenz: this.konvergenzEnhancedForm.valueOnValid(),
      bemerkungen: trimToUndefined(value.bemerkungen),
      strabismus: this.strabismusEnhancedForm.valueOnValid(),
      strabismusAuspraegung: this.strabismusAuspraegungEnhancedForm.valueOnValid(),
      strabismusAuftreten: this.strabismusAuftretenEnhancedForm.valueOnValid(),
      empfehlung: this.empfehlungEnhancedForm.valueOnValid(),
      version: value.version ?? globalConstants.entityPrePersistVersion,
    });
  }

  setValidatorsForAnlassConfig(config: AnlassConfigJson, nichtGeprueftGrundEnabled: boolean): void {
    this.fernvisusEnhancedForm.setValidatorsForAnlassConfig(
      config.data.sehenFernvisus,
      nichtGeprueftGrundEnabled,
    );
    this.nahvisusEnhancedForm.setValidatorsForAnlassConfig(
      config.data.sehenNahvisus,
      nichtGeprueftGrundEnabled,
    );

    if (config.data.sehenFernvisus === RequirementSetting.REQUIRED) {
      this.group.controls.fernvisusRechts.addValidators(AppValidators.requiredIfValidator(() =>
        !this.isFernvisusResultatNichtGeprueftChecked()
      ));
      this.group.controls.fernvisusLinks.addValidators(AppValidators.requiredIfValidator(() =>
        !this.isFernvisusResultatNichtGeprueftChecked()
      ));
    }
    if (config.data.sehenNahvisus === RequirementSetting.REQUIRED) {
      this.group.controls.nahvisusRechts.addValidators(AppValidators.requiredIfValidator(() =>
        !this.isNahvisusResultatNichtGeprueftChecked()
      ));
      this.group.controls.nahvisusLinks.addValidators(AppValidators.requiredIfValidator(() =>
        !this.isNahvisusResultatNichtGeprueftChecked()
      ));
    }

    this.strabismusEnhancedForm.setValidatorsForAnlassConfig(
      config.data.sehenStrabismus,
      nichtGeprueftGrundEnabled,
    );
    this.empfehlungEnhancedForm.setValidatorsForAnlassConfig(
      config.data.sehenEmpfehlung,
      nichtGeprueftGrundEnabled,
    );

    if (config.data.sehenBemerkungen === RequirementSetting.REQUIRED) {
      this.group.controls.bemerkungen.addValidators(Validators.required);
    }

    this.brilleLinsenEnhancedForm.setValidatorsForAnlassConfig(
      config.data.sehenBrilleLinsen,
      nichtGeprueftGrundEnabled,
    );
    this.stereotestEnhancedForm.setValidatorsForAnlassConfig(
      config.data.sehenStereoTest,
      nichtGeprueftGrundEnabled,
    );
    this.farbsehenEnhancedForm.setValidatorsForAnlassConfig(
      config.data.sehenFarbsehen,
      nichtGeprueftGrundEnabled,
    );
    this.verwendeteTafelEnhancedForm.setValidatorsForAnlassConfig(
      config.data.sehenTafel,
      nichtGeprueftGrundEnabled,
    );
    this.fernvisusAugeLinksEnhancedForm.setValidatorsForAnlassConfig(
      config.data.sehenFernvisusAugeEinzeln,
      nichtGeprueftGrundEnabled,
    );
    this.fernvisusAugeRechtsEnhancedForm.setValidatorsForAnlassConfig(
      config.data.sehenFernvisusAugeEinzeln,
      nichtGeprueftGrundEnabled,
    );
    this.motilitaetEnhancedForm.setValidatorsForAnlassConfig(
      config.data.sehenMotilitaet,
      nichtGeprueftGrundEnabled,
    );
    this.strabismusAuspraegungEnhancedForm.setValidatorsForAnlassConfig(
      config.data.sehenStrabismusAuspraegung,
      nichtGeprueftGrundEnabled,
    );
    this.strabismusAuftretenEnhancedForm.setValidatorsForAnlassConfig(
      config.data.sehenStrabismusAuftreten,
      nichtGeprueftGrundEnabled,
    );
    this.inspektionEnhancedForm.setValidatorsForAnlassConfig(
      config.data.sehenInspektion,
      nichtGeprueftGrundEnabled,
    );
    this.konvergenzEnhancedForm.setValidatorsForAnlassConfig(
      config.data.sehenKonvergenz,
      nichtGeprueftGrundEnabled,
    );
  }

  public isFernvisusResultatNichtGeprueftChecked(): boolean {
    return checkIfArrayContainsCheckedValue(this.fernvisusEnhancedForm.group.controls.valueArray.controls,
      [DistanzSehenResultat.NICHT_GEPRUEFT]);
  }

  public isNahvisusResultatNichtGeprueftChecked(): boolean {
    return checkIfArrayContainsCheckedValue(this.nahvisusEnhancedForm.group.controls.valueArray.controls,
      [DistanzSehenResultat.NICHT_GEPRUEFT]);
  }
}
