import {DatePipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, DestroyRef, effect, inject, signal} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {FormsModule, NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {
  EntityAktivStatus,
  Geschlecht,
  KindJson,
  KindSaveJson,
  KindSaveModeDuplikat,
  Land,
} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {AdresseComponent, AdresseForm, BenutzerStore, EventBusStore, NavigationService, Path} from '@dvb/feat-shared';
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
import {DetailMode, DirtyMarker} from '@dvb/util-angular';
import {
  fromISODateTimeString,
  hasKindEditBerechtigung,
  isExternalObject,
  isNotNullish,
  isNullish
} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NgbAlert} from '@ng-bootstrap/ng-bootstrap';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {distinctUntilChanged} from 'rxjs';
import {DossierStore} from '../../dossier.store';
import {KinderSummaryStore} from '../../kinder-summary.store';
import {KindPersonendatenForm} from './kind-personendaten.form';

@Component({
  selector: 'feat-kinder-detail-personendaten-page',
  templateUrl: './kind-personendaten.component.html',
  styleUrl: './kind-personendaten.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    TranslocoPipe,
    HeadingDirective,
    FormControlComponent,
    TextInputDirective,
    FormLayoutComponent,
    FormsModule,
    ReactiveFormsModule,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    RadioDirective,
    DatePipe,
    FormButtonsComponent,
    FormErrorMessageComponent,
    SingleRadioWrapperComponent,
    FormGroupComponent,
    AdresseComponent,
    FormFullWidthDirective,
    NgbAlert,
    CheckboxDirective,
  ],
})
export class KindPersonendatenComponent implements DirtyMarker {

  readonly store = inject(KinderSummaryStore);
  readonly dossierStore = inject(DossierStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly #eventBusStore = inject(EventBusStore);

  readonly navigationService = inject(NavigationService);
  readonly #router = inject(Router);
  readonly #activatedRoute = inject(ActivatedRoute);
  readonly #destroyRef = inject(DestroyRef);
  readonly #transloco = inject(TranslocoService);

  protected readonly Path = Path;

  public canEditKind = computed(() => {
    return this.benutzerStore.createHasRequiredRoleSig(hasKindEditBerechtigung())()
      && (this.dossierStore.isKindAktiv() || this.isNew)
  });

  nationalitaeten = Object.values(Land);
  geschlechtOptions = Object.values(Geschlecht);
  kindAktivStati = Object.values(EntityAktivStatus);
  isNew = false;

  kindTriedToSave = signal<KindSaveJson | undefined>(undefined);
  kindIdsOfDuplicates = signal<BackendEntityId[]>([]);
  markDuplicatesAsFalsePositives = false;

  getImportSchnittstelle = computed(() => {
    const externalImportInfo = this.dossierStore.selectedKind()?.externalImportInfoJson;
    return isNotNullish(externalImportInfo) ? this.#transloco.translate('ImportTyp.' + externalImportInfo.importTyp) : '';
  });

  isExternalObject = computed(() => isExternalObject(this.dossierStore.selectedKind()));

  getDateLatestImportIfExternalObject() {
    if (this.isExternalObject()) {
      return fromISODateTimeString(this.dossierStore.selectedKind()?.externalImportInfoJson?.timestampLatestImport);
    }
    return undefined;
  }

  public form = new KindPersonendatenForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    kinderStore: this.store,
    adresseForm: new AdresseForm({
      fb: inject(NonNullableFormBuilder),
      destroyRef: this.#destroyRef,
      withLand: true,
      parentIdentifier: 'kind-personendaten',
    }),
  });

  constructor() {
    effect(() => {
      const selectedEntity = this.dossierStore.selectedKind();
      const mode = this.dossierStore.detailMode();
      if (isNullish(selectedEntity)) {
        this.isNew = true;
        return; // stop processing
      } else {
        this.isNew = false;
      }
      this.form.reset(selectedEntity.data, mode);
      if (this.isExternalObject()) {
        this.form.disableEscadaFields()
      }
    });

    this.#eventBusStore
      .on('KIND_DUPLIKAT_VORHANDEN')
      .pipe(takeUntilDestroyed())
      .subscribe(event => {
        this.kindTriedToSave.set(event.kindTriedToSave);
        this.kindIdsOfDuplicates.set(event.kindIdsOfDuplicates);
      });

    this.form.group.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(this.#destroyRef)).subscribe({
      next: kindChanges => {
        const nameChanged = kindChanges.name !== this.kindTriedToSave()?.name;
        const vornameChanged = kindChanges.vorname !== this.kindTriedToSave()?.vorname;
        const geburtsdatumChanged = kindChanges.geburtsdatum !== this.kindTriedToSave()?.geburtsdatum;
        const geschlechtChanged = kindChanges.geschlecht !== this.kindTriedToSave()?.geschlecht;
        if (nameChanged || vornameChanged || geburtsdatumChanged || geschlechtChanged) {
          this.resetDuplikatFields();
        }
      },
      error: console.error,
    });
  }

  resetDuplikatFields(): void {
    this.kindTriedToSave.set(undefined);
    this.kindIdsOfDuplicates.set([]);
    this.markDuplicatesAsFalsePositives = false;
  }

  isDirty(): boolean {
    return this.form.isDirty() || this.store.saveLoader.isLoading();
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const payload = this.form.valueOnValid();
      if (this.isNew) {
        this.store.createKind({
          kind: payload,
          kindSaveMode: this.markDuplicatesAsFalsePositives ?
            KindSaveModeDuplikat.MARK_AS_FALSE_POSITIVE :
            KindSaveModeDuplikat.CHECK,
          onSuccess: (result: KindJson) => {
            this.form.reset();
            this.resetDuplikatFields();
            this.#router.navigate([Path.RELATIVE_TWO_UP, result.id, Path.PROFIL, Path.SCHULE_KLASSE, DetailMode.EDIT],
              {
                relativeTo: this.#activatedRoute,
              });
          },
        });
      } else {
        this.dossierStore.updateKind({
          kind: payload,
          kindSaveMode: this.markDuplicatesAsFalsePositives ?
            KindSaveModeDuplikat.MARK_AS_FALSE_POSITIVE :
            KindSaveModeDuplikat.CHECK,
          onSuccess: () => {
            this.form.reset();
            this.resetDuplikatFields();
            this.#router.navigate([Path.RELATIVE_ONE_UP, DetailMode.VIEW], {relativeTo: this.#activatedRoute});
          },
        });
      }
    }
  }
}
