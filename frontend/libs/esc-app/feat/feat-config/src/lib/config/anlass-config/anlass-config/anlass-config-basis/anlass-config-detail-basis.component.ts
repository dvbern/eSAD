import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, DestroyRef, effect, inject, untracked} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {AnlassConfigJson, AnlassTyp} from '@dvb/data-access-backend';
import {BenutzerStore, Path} from '@dvb/feat-shared';
import {
  CheckboxDirective,
  FormButtonsComponent,
  FormControlComponent,
  FormErrorMessageComponent, FormGroupComponent,
  FormLayoutComponent,
  HeadingDirective,
  NgSelectDirective, SingleCheckboxWrapperComponent,
  TextInputDirective,
} from '@dvb/ui-components';
import {DirtyMarker} from '@dvb/util-angular';
import {fromISODateString, isNullish} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {AnlassConfigStore} from '../../anlass-config.store';
import {AnlassConfigForm} from '../anlass-config-detail.form';
import {AnlassConfigHelperService} from '../../anlass-config-helper.service';
import {ActivatedRoute} from '@angular/router';

@Component({
  selector: 'feat-config-detail-basis',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormButtonsComponent,
    FormControlComponent,
    FormErrorMessageComponent,
    FormLayoutComponent,
    HeadingDirective,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    TextInputDirective,
    TranslocoPipe,
    CheckboxDirective,
    FormGroupComponent,
    SingleCheckboxWrapperComponent,
  ],
  templateUrl: './anlass-config-detail-basis.component.html',
  styleUrl: './anlass-config-detail-basis.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AnlassConfigDetailBasisComponent implements DirtyMarker {
  readonly store = inject(AnlassConfigStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly anlassConfigHelperService = inject(AnlassConfigHelperService);
  readonly #activatedRoute = inject(ActivatedRoute);

  readonly anlassTypOptions = Object.values(AnlassTyp);
  protected readonly Path = Path;

  public form = new AnlassConfigForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    anlassConfigStore: this.store,
  });

  constructor() {
    effect(() => {
      const selectedEntity = this.store.selectedEntity();
      this.form.disableFields(this.store.selectedIsNew());
      const mode = this.store.detailMode();
      if (isNullish(selectedEntity)) {
        return;
      }
      untracked(() => {
        this.form.reset(selectedEntity.data, mode);
      });
    });
  }

  get anlassConfigsAsBasis() {
    const anlassTyp = this.form.group.controls.anlassTyp.value;
    return this.store.entities()
      .filter(config => config.data.anlassTyp === anlassTyp)
      .sort((a, b) => {
        const aDatumBis = fromISODateString(a.data.datumVon);
        const bDatumBis = fromISODateString(b.data.datumVon);
        return bDatumBis.getTime() - aDatumBis.getTime();
      });
  };

  selectBasis(config: AnlassConfigJson | undefined): void {
    this.form.setBasis(config?.data);
  }

  isDirty(): boolean {
    return this.anlassConfigHelperService.isDirty(this.form);
  }

  async submitIfValid(): Promise<void> {
    this.anlassConfigHelperService.submitIfValid(this.form, this.#activatedRoute);
  }
}
