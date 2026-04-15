import {DatePipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, effect, inject, untracked} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {Geschlecht} from '@dvb/data-access-backend';
import {KlassenStore, Path} from '@dvb/feat-shared';
import {
  FormButtonsComponent,
  FormControlComponent,
  FormErrorMessageComponent,
  FormGroupComponent,
  FormLayoutComponent,
  HeadingDirective,
  RadioDirective,
  SingleRadioWrapperComponent,
  TextInputDirective,
} from '@dvb/ui-components';
import {DirtyMarker} from '@dvb/util-angular';
import {fromISODateTimeString, isExternalObject, isNotNullish, isNullish, requireValue} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {LehrpersonenStore} from '../lehrpersonen.store';
import {LehrpersonForm} from './lehrperson.form';

@Component({
  selector: 'feat-lehrperson',
  standalone: true,
  imports: [
    FormButtonsComponent,
    FormErrorMessageComponent,
    FormControlComponent,
    FormLayoutComponent,
    ReactiveFormsModule,
    RadioDirective,
    TextInputDirective,
    TranslocoPipe,
    HeadingDirective,
    FormGroupComponent,
    SingleRadioWrapperComponent,
    DatePipe,
  ],
  templateUrl: './lehrperson.component.html',
  styleUrl: './lehrperson.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LehrpersonComponent implements DirtyMarker {
  readonly store = inject(LehrpersonenStore);
  readonly #klassenStore = inject(KlassenStore);
  readonly #router = inject(Router);
  readonly #activatedRoute = inject(ActivatedRoute);
  readonly #transloco = inject(TranslocoService);

  readonly geschlechtOptions = Object.values(Geschlecht);

  protected readonly Path = Path;

  getImportSchnittstelle = computed(() => {
    const externalImportInfo = this.store.selectedEntity()?.externalImportInfoJson;
    return isNotNullish(externalImportInfo) ? this.#transloco.translate('ImportTyp.' + externalImportInfo.importTyp) : '';
  });

  isExternalObject = computed(() => isExternalObject(this.store.selectedEntity()));

  getDateLatestImportIfExternalObject() {
    if (this.isExternalObject()) {
      return fromISODateTimeString(this.store.selectedEntity()?.externalImportInfoJson?.timestampLatestImport);
    }
    return undefined;
  }

  public form = new LehrpersonForm({
    fb: inject(NonNullableFormBuilder),
  });

  pageTitle = computed(() => {
    if (this.store.selectedIsNew()) {
      return 'klasse.tab.lehrpersonen.create';
    } else {
      return 'klasse.tab.lehrpersonen.' + this.store.detailMode().toLowerCase();
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
        if (this.isExternalObject()) {
          this.form.controlAt('geschlecht').disable();
          this.form.controlAt('name').disable();
          this.form.controlAt('vorname').disable();
        }
      });
    });
  }

  isDirty(): boolean {
    return this.form.isDirty() || this.store.saveLoader.isLoading();
  }

  async submitIfValid(): Promise<void> {
    const currentKlasse = this.#klassenStore.selectedEntity();
    if (await this.form.validate()) {
      const payload = this.form.valueOnValid();
      if (this.store.selectedIsNew()) {
        this.store.createLehrperson({
          klasseID: requireValue(currentKlasse?.id),
          lehrperson: payload,
          onSuccess: _ => {
            this.form.reset();
            this.goToList(Path.RELATIVE_ONE_UP);
          },
        });
      } else {
        this.store.updateLehrperson({
          klasseID: requireValue(currentKlasse?.id),
          lehrpersonToUpdate: payload,
          onSuccess: _ => {
            this.form.reset();
            this.#router.navigate([Path.RELATIVE_TWO_UP, Path.ALL], {relativeTo: this.#activatedRoute});
          },
        });
      }
    }
  }

  goToList(path: Path): void {
    this.#router.navigate([path, 'all'], {relativeTo: this.#activatedRoute});
  }
}
