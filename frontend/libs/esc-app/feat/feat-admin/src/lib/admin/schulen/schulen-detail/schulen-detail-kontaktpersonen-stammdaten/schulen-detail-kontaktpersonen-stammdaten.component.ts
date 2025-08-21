import {ChangeDetectionStrategy, Component, computed, effect, inject, untracked} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {Geschlecht, KontaktPersonSchuleFunktion} from '@dvb/data-access-backend';
import {isValidBackendEntityId} from '@dvb/data-access-types';
import {KontaktPersonSchuleStore, SchulenStore} from '@dvb/feat-shared';
import {Path} from '@dvb/feat-shared';
import {
  FormButtonsComponent,
  FormControlComponent,
  FormErrorMessageComponent,
  FormGroupComponent,
  FormLayoutComponent,
  HeadingDirective,
  NgSelectDirective,
  RadioDirective,
  SingleRadioWrapperComponent,
  TextInputDirective,
} from '@dvb/ui-components';
import {DirtyMarker} from '@dvb/util-angular';
import {isNullish} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {KontaktPersonSchulenForm} from './schulen-detail-kontaktpersonen.form';

@Component({
  selector: 'feat-schulen-detail-kontaktpersonen-stammdaten',
  imports: [
    FormButtonsComponent,
    FormErrorMessageComponent,
    NgOptionComponent,
    NgSelectDirective,
    FormControlComponent,
    FormLayoutComponent,
    ReactiveFormsModule,
    RadioDirective,
    TextInputDirective,
    NgSelectComponent,
    TranslocoPipe,
    HeadingDirective,
    FormGroupComponent,
    SingleRadioWrapperComponent,
  ],
  templateUrl: './schulen-detail-kontaktpersonen-stammdaten.component.html',
  styleUrl: './schulen-detail-kontaktpersonen-stammdaten.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SchulenDetailKontaktpersonenStammdatenComponent implements DirtyMarker {
  readonly store = inject(KontaktPersonSchuleStore);
  readonly schulenStore = inject(SchulenStore);
  readonly #router = inject(Router);
  readonly #activatedRoute = inject(ActivatedRoute);

  readonly geschlechtOptions = Object.values(Geschlecht);
  readonly funktionOptions = Object.values(KontaktPersonSchuleFunktion);

  protected readonly Path = Path;

  public form = new KontaktPersonSchulenForm({
    fb: inject(NonNullableFormBuilder),
  });

  pageTitle = computed(() => {
    if (this.store.selectedIsNew()) {
      return 'schule.detail.tab.kontaktperson.create';
    } else {
      return 'schule.detail.tab.kontaktperson.' + this.store.detailMode().toLowerCase();
    }
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
    const selectedSchuleId = this.schulenStore.selectedEntityId();
    if (isValidBackendEntityId(selectedSchuleId)) {
      if (await this.form.validate()) {
        const payload = this.form.valueOnValid();
        if (this.store.selectedIsNew()) {
          this.store.createKontaktPersonSchule({
            schuleId: selectedSchuleId,
            kontaktPerson: payload,
            onSuccess: _ => {
              this.form.reset();
              this.goToList(Path.RELATIVE_ONE_UP);
            },
          });
        } else {
          this.store.updateKontaktPersonSchule({
            schuleId: selectedSchuleId,
            kontaktPersonToUpdate: payload,
            onSuccess: _ => {
              this.form.reset();
              this.#router.navigate([Path.RELATIVE_TWO_UP, Path.ALL], {relativeTo: this.#activatedRoute});
            },
          });
        }
      }
    }
  }

  goToList(path: Path): void {
    this.#router.navigate([path, 'all'], {relativeTo: this.#activatedRoute});
  }
}
