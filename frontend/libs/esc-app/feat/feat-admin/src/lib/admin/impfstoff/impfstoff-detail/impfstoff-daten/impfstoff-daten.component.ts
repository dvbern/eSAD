
import {ChangeDetectionStrategy, Component, effect, inject, untracked} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {ImpfstoffJson, Krankheit} from '@dvb/data-access-backend';
import {ImpfstoffeChargenStore} from '@dvb/feat-shared';
import {BenutzerStore} from '@dvb/feat-shared';
import {Path} from '@dvb/feat-shared';
import {
  FormButtonsComponent,
  FormControlComponent,
  FormLayoutComponent,
  HeadingDirective,
  NgSelectDirective,
  TextInputDirective,
} from '@dvb/ui-components';
import {DetailMode, DirtyMarker} from '@dvb/util-angular';
import {isNullish} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {ImpfstoffDatenForm} from './impfstoff-daten.form';

@Component({
  selector: 'feat-label-detail.page',
  imports: [
    TranslocoPipe,
    ReactiveFormsModule,
    FormButtonsComponent,
    NgOptionComponent,
    NgSelectDirective,
    FormControlComponent,
    FormLayoutComponent,
    NgSelectComponent,
    HeadingDirective,
    TextInputDirective
],
  templateUrl: './impfstoff-daten.component.html',
  styles: ':host { display:block;}',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ImpfstoffDatenComponent implements DirtyMarker {

  readonly store = inject(ImpfstoffeChargenStore);
  readonly #router = inject(Router);
  readonly #activatedRoute = inject(ActivatedRoute);
  readonly benutzerStore = inject(BenutzerStore);
  readonly krankheitTypen = Object.values(Krankheit);
  protected readonly DetailMode = DetailMode;
  protected readonly Path = Path;

  public form: ImpfstoffDatenForm = new ImpfstoffDatenForm({
    fb: inject(NonNullableFormBuilder),
    impfstoffeChargenStore: this.store,
  });

  constructor() {
    effect(() => {
      const selectedEntity = this.store.selectedEntity();
      const mode = this.store.detailMode();
      if (isNullish(selectedEntity)) {
        return;
      }

      untracked(() => {
        this.form.reset(selectedEntity.data, mode);
      });
    });
  }

  isDirty(): boolean {
    return this.form.isDirty() || this.store.saveLoader.isLoading();
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const payload = this.form.valueOnValid();
      if (this.store.selectedIsNew()) {
        this.store.createImpfstoff({
          impfstoff: payload,
          onSuccess: (result: ImpfstoffJson) => {
            this.form.reset();
            this.#router.navigate([
                Path.RELATIVE_TWO_UP,
                result.id,
                Path.CHARGE,
                'all',
              ],
              {relativeTo: this.#activatedRoute});
          },
        });
      } else {
        this.store.updateImpfstoff({
          impfstoff: payload,
          onSuccess: () => {
            this.form.reset();
            this.#router.navigate([Path.RELATIVE_ONE_UP], {relativeTo: this.#activatedRoute});
          },
        });
      }
    }
  }

}
