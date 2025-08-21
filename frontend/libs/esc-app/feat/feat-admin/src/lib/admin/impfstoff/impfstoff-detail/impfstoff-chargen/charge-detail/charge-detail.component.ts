import {ChangeDetectionStrategy, Component, DestroyRef, inject} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {ImpfstoffeChargenStore} from '@dvb/feat-shared';
import {Path} from '@dvb/feat-shared';
import {
  FormButtonsComponent,
  FormControlComponent,
  FormGroupComponent,
  FormLayoutComponent,
  RadioDirective,
  SingleRadioWrapperComponent,
  TextInputDirective,
} from '@dvb/ui-components';
import {DirtyMarker} from '@dvb/util-angular';
import {requireValue} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';
import {ChargenStore} from '../chargen.store';
import {ChargeDetailForm} from './charge-detail.form';

@Component({
  selector: 'feat-impfstoff-charge-modal',
  templateUrl: './charge-detail.component.html',
  styles: ':host { display:block;}',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    TranslocoPipe,
    ReactiveFormsModule,
    TextInputDirective,
    RadioDirective,
    SingleRadioWrapperComponent,
    FormGroupComponent,
    FormLayoutComponent,
    FormControlComponent,
    FormButtonsComponent,
  ],
})
export class ChargeDetailComponent implements DirtyMarker {

  readonly store = inject(ChargenStore);
  readonly #impfstoffStore = inject(ImpfstoffeChargenStore);
  readonly #router = inject(Router);
  readonly #activatedRoute = inject(ActivatedRoute);

  form = new ChargeDetailForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    chargenStore: this.store,
  });

  isDirty(): boolean {
    return this.form.isDirty();
  }

  async submitIfValid(): Promise<void> {
    const currentImpfstoff = this.#impfstoffStore.selectedEntity();
    if (await this.form.validate()) {
      const payload = this.form.valueOnValid();
      if (this.store.selectedIsNew()) {
        this.store.createImpfstoffCharge({
          impfstoffID: requireValue(currentImpfstoff?.id),
          impfstoffChargeSaveJson: payload,
          onSuccess: () => {
            this.form.reset();
            this.#router.navigate([Path.RELATIVE_ONE_UP, 'all'], { relativeTo: this.#activatedRoute });
          },
        });
      } else {
        this.store.updateImpfstoffCharge({
          impfstoffID: requireValue(currentImpfstoff?.id),
          chargeToUpdate: payload,
          onSuccess: () => {
            this.form.reset();
            this.#router.navigate([Path.RELATIVE_TWO_UP, Path.ALL], { relativeTo: this.#activatedRoute });
          },
        });
      }
    }
  }

  protected readonly Path = Path;
}
