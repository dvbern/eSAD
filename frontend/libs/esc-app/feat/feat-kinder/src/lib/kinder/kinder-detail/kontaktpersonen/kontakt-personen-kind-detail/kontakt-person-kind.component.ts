import {DatePipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, DestroyRef, effect, inject, untracked} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {Geschlecht, ImportTyp, KontaktPersonKindBeziehung} from '@dvb/data-access-backend';
import {isValidBackendEntityId} from '@dvb/data-access-types';
import {AdresseComponent, AdresseForm, BenutzerStore, BooleanPipe, Path} from '@dvb/feat-shared';
import {
  CheckboxDirective,
  FormButtonsComponent,
  FormControlComponent,
  FormErrorMessageComponent,
  FormFullWidthDirective,
  FormGroupComponent,
  FormLayoutComponent,
  HeadingDirective,
  NgSelectDirective,
  RadioDirective,
  SingleRadioWrapperComponent,
  TextInputDirective,
} from '@dvb/ui-components';
import {DirtyMarker} from '@dvb/util-angular';
import {fromISODateTimeString, hasKindEditBerechtigung, isNotNullish, isNullish} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {DossierStore} from '../../../dossier.store';
import {KontaktPersonKindStore} from '../kontakt-person-kind.store';
import {KontaktPersonKindForm} from './kontakt-person-kind.form';

@Component({
  selector: 'feat-kontakt-person-kind',
  standalone: true,
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
    CheckboxDirective,
    AdresseComponent,
    FormFullWidthDirective,
    DatePipe,
    BooleanPipe,
  ],
  templateUrl: './kontakt-person-kind.component.html',
  styleUrl: './kontakt-person-kind.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class KontaktPersonKindComponent implements DirtyMarker {
  readonly store = inject(KontaktPersonKindStore);
  readonly #benutzerStore = inject(BenutzerStore);
  readonly dossierStore = inject(DossierStore);
  readonly #router = inject(Router);
  readonly #activatedRoute = inject(ActivatedRoute);

  readonly geschlechtOptions = Object.values(Geschlecht);
  readonly beziehungOptions = Object.values(KontaktPersonKindBeziehung);

  protected readonly Path = Path;

  canEdit = this.#benutzerStore.createHasRequiredRoleSig(hasKindEditBerechtigung());

  isEscadaObject = computed(() => {
    const externalImportInfo = this.store.selectedEntity()?.externalImportInfoJson;
    return isNotNullish(externalImportInfo)
      && externalImportInfo?.aktivInExternalSystem
      && externalImportInfo?.importTyp === ImportTyp.ESCADA;
  });

  getDateLatestImportIfEscadaObject() {
    if (this.isEscadaObject()) {
      return fromISODateTimeString(this.store.selectedEntity()?.externalImportInfoJson?.timestampLatestImport);
    }
    return undefined;
  }

  public form = new KontaktPersonKindForm({
    fb: inject(NonNullableFormBuilder),
    adresseForm: new AdresseForm({
      fb: inject(NonNullableFormBuilder),
      destroyRef: inject(DestroyRef),
      withLand: true,
      parentIdentifier: 'kontakt-person-kind',
    }),
    destroyRef: inject(DestroyRef),
  });

  pageTitle = computed(() => {
    if (this.store.selectedIsNew()) {
      return 'kind.tab.kontaktperson.create';
    } else {
      return 'kind.tab.kontaktperson.' + this.store.detailMode().toLowerCase();
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
        if (this.isEscadaObject()) {
          this.form.controlAt('geschlecht').disable();
          this.form.controlAt('name').disable();
          this.form.controlAt('vorname').disable();
          this.form.controlAt('beziehung').disable();
          this.form.controlAt('email').disable();
          this.form.controlAt('telefon').disable();
          this.form.controlAt('adresse.strasseUndNr').disable();
          this.form.controlAt('adresse.adresseZusatz').disable();
          this.form.controlAt('adresse.plz').disable();
          this.form.controlAt('adresse.ort').disable();
          this.form.controlAt('adresse.land').disable();
        }
      });
    });
  }

  isDirty(): boolean {
    return this.form.isDirty() || this.store.saveLoader.isLoading();
  }

  async submitIfValid(): Promise<void> {
    const currentKindId = this.dossierStore.selectedKind()?.id;
    if (isValidBackendEntityId(currentKindId)) {
      if (await this.form.validate()) {
        const payload = this.form.valueOnValid();
        if (this.store.selectedIsNew()) {
          this.store.createKontaktPerson({
            kindID: currentKindId,
            kontaktPerson: payload,
            onSuccess: _ => {
              this.form.reset();
              this.goToList(Path.RELATIVE_ONE_UP);
            },
          });
        } else {
          this.store.updateKontaktPerson({
            kindID: currentKindId,
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
