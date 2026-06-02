import {DatePipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, DestroyRef, effect, inject, signal} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {FormsModule, NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {
  BenutzerOptionJson,
  EntityAktivStatus,
  Geschlecht,
  KindJson,
  KindSaveJson,
  KindSaveModeDuplikat,
  Land,
} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {
  AdresseComponent,
  AdresseForm,
  BenutzerOptionsStore,
  BenutzerStore,
  ConfirmService,
  EventBusStore,
  NavigationService,
  Path,
  TenantSettingsStore
} from '@dvb/feat-shared';
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
  readonly benutzerOptionsStore = inject(BenutzerOptionsStore);
  readonly tenantSettingsStore = inject(TenantSettingsStore);

  readonly navigationService = inject(NavigationService);
  readonly #router = inject(Router);
  readonly #activatedRoute = inject(ActivatedRoute);
  readonly #destroyRef = inject(DestroyRef);
  readonly #transloco = inject(TranslocoService);
  readonly #confirmService = inject(ConfirmService);

  protected readonly Path = Path;
  protected readonly EntityAktivStatus = EntityAktivStatus;

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

  compareBenutzerFn(item: BenutzerOptionJson, selected: BenutzerOptionJson): boolean {
    return item.id === selected.id;
  }

  arztOptions = computed(() => {
    return this.benutzerOptionsStore.benutzerFiltered(
      this.dossierStore.selectedDossier()?.dataNichtMedizinisch.zustaendigArzt,
      'ARZT',
      !this.dossierStore.selectedDossier()?.dataNichtMedizinisch.zustaendigArzt);
  });

  hilfspersonOptions = computed(() => {
    return this.benutzerOptionsStore.benutzerFiltered(
      this.dossierStore.selectedDossier()?.dataNichtMedizinisch.zustaendigMPA,
      'HILFSPERSON',
      !this.dossierStore.selectedDossier()?.dataNichtMedizinisch.zustaendigMPA);
  });

  public form = new KindPersonendatenForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    kinderStore: this.store,
    tenantSettingsStore: this.tenantSettingsStore,
    benutzerStore: this.benutzerStore,
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
      if (isNullish(selectedEntity)) {
        this.isNew = true;
        return; // stop processing
      } else {
        this.isNew = false;
      }
      this.resetForm();
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

  resetForm(): void {
    const selectedEntity = this.dossierStore.selectedKind();
    if (isNotNullish(selectedEntity)) {
      const selectedDossierIfExisting = this.dossierStore.selectedDossier();
      const mode = this.dossierStore.detailMode();
      this.form.resetForm(selectedEntity.data, selectedDossierIfExisting?.dataNichtMedizinisch?.zustaendigArzt, selectedDossierIfExisting?.dataNichtMedizinisch?.zustaendigMPA, mode);
    }
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
      const {zustaendigArzt, zustaendigMPA, ...kindSaveJson} = payload;
      if (this.isNew) {
        this.store.createKind({
          kind: kindSaveJson,
          arztId: zustaendigArzt?.id,
          mpaId: zustaendigMPA?.id,
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
        if (this.dossierStore.showZustaendigerChangedWarning(payload.zustaendigArzt)) {
          this.#confirmService
            .confirm({
              question: this.#transloco.translate('dossier.change.zustaendigkeit.question'),
              consequence: this.#transloco.translate('dossier.change.zustaendigkeit.consequence'),
              decision: this.#transloco.translate('dossier.change.zustaendigkeit.decision'),
            })
            .subscribe(() => {
              this.updateKind(kindSaveJson, zustaendigArzt?.id, zustaendigMPA?.id);
            });
        } else {
          this.updateKind(kindSaveJson, zustaendigArzt?.id, zustaendigMPA?.id);
        }
      }
    }
  }

  private updateKind(
    kindSaveJson: KindSaveJson,
    zustaendigArzt: BackendEntityId | undefined,
    zustaendigMPA: BackendEntityId | undefined
  ): void {
    this.dossierStore.updateKind({
      kind: kindSaveJson,
      arztId: zustaendigArzt,
      mpaId: zustaendigMPA,
      kindSaveMode: this.markDuplicatesAsFalsePositives ?
        KindSaveModeDuplikat.MARK_AS_FALSE_POSITIVE :
        KindSaveModeDuplikat.CHECK,
      onSuccess: () => {
        this.resetForm();
        this.resetDuplikatFields();
        this.#router.navigate([Path.RELATIVE_ONE_UP, DetailMode.VIEW], {relativeTo: this.#activatedRoute});
      },
    });
  }
}
