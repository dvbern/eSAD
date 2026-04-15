import {ChangeDetectionStrategy, Component, computed, DestroyRef, effect, inject, untracked} from '@angular/core';
import {FormsModule, NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {
  EntityAktivStatus,
  ImportTyp,
  KindSaveJson,
  KindSaveModeDuplikat,
  KlasseOptionJson,
  SchuleOptionJson,
} from '@dvb/data-access-backend';
import {BenutzerStore, KlassenOptionsStore, Path, SchulenOptionsStore} from '@dvb/feat-shared';
import {
  FormButtonsComponent,
  FormControlComponent,
  FormErrorMessageComponent,
  FormLayoutComponent,
  HeadingDirective,
  NgSelectDirective,
} from '@dvb/ui-components';
import {DetailMode, DirtyMarker} from '@dvb/util-angular';
import {hasKindEditBerechtigung, isExternalObject, isNotNullish, isNullish, requireValue} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {DossierStore} from '../../dossier.store';
import {KinderSummaryStore} from '../../kinder-summary.store';
import {KindSchuleKlasseForm} from './kind-schule-klasse.form';

@Component({
  selector: 'feat-kinder-detail-schule-klasse',
  templateUrl: './kinder-schule-klasse.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FormLayoutComponent,
    FormControlComponent,
    FormsModule,
    HeadingDirective,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    TranslocoPipe,
    ReactiveFormsModule,
    FormButtonsComponent,
    FormErrorMessageComponent,
  ],
})
export class KinderSchuleKlasseComponent implements DirtyMarker {
  readonly store = inject(KinderSummaryStore);
  readonly dossierStore = inject(DossierStore);
  readonly schulenOptionsStore = inject(SchulenOptionsStore);
  readonly klassenStore = inject(KlassenOptionsStore);
  readonly #router = inject(Router);
  readonly #activatedRoute = inject(ActivatedRoute);
  readonly #transloco = inject(TranslocoService);
  readonly benutzerStore = inject(BenutzerStore);
  protected readonly Path = Path;

  schuleOptions = computed(() =>
    this.schulenOptionsStore.entitiesIncluding(this.dossierStore.selectedKind()?.data.schule),
  );
  klasseOptions = computed(() => {
    if (this.dossierStore.detailMode() == DetailMode.EDIT && this.form.group.controls.schule.dirty) {
      return this.klassenStore.entities();
    } else {
      return this.klassenStore.entitiesIncluding(this.dossierStore.selectedKind()?.data.klasse);
    }
  });
  getImportSchnittstelle = computed(() => {
    const externalImportInfo = this.dossierStore.selectedKind()?.externalImportInfoJson;
    return isNotNullish(externalImportInfo) ? this.#transloco.translate('ImportTyp.' + externalImportInfo.importTyp) : '';
  });
  isExternalObject = computed(() => isExternalObject(this.dossierStore.selectedKind()));

  canEdit = this.benutzerStore.createHasRequiredRoleSig(hasKindEditBerechtigung());

  public form = new KindSchuleKlasseForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    klassenStore: this.klassenStore,
  });

  constructor() {
    effect(() => {
      const selectedEntity = this.dossierStore.selectedKind();
      const mode = this.dossierStore.detailMode();
      if (isNullish(selectedEntity)) {
        return;
      }
      untracked(() => {
        this.form.reset(selectedEntity.data, mode);
        if (this.isExternalObject()) {
          this.form.group.controls.schule.disable();
          this.form.group.controls.klasse.disable();
        }
      });
    });
  }

  isDirty(): boolean {
    return this.form.isDirty() || this.store.saveLoader.isLoading();
  }

  compareSchule(item: SchuleOptionJson, selected: SchuleOptionJson): boolean {
    return item.id === selected.id;
  }

  compareKlasse(item: KlasseOptionJson, selected: KlasseOptionJson): boolean {
    return item.id === selected.id;
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const value: KindSaveJson = {
        ...requireValue(this.dossierStore.selectedKind()).data,
        ...this.form.valueOnValid(),
      };

      this.dossierStore.updateKind({
        kind: value,
        kindSaveMode: KindSaveModeDuplikat.NO_CHECK,
        onSuccess: () => {
          // Beim Ändern der Schule können MPA und Arzt auf dem Dossier geändert werden, deshalb laden wir diese Daten ebenfalls nach
          const kindId = this.dossierStore.selectedKind()?.id;
          if (this.form.group.controls.schule.dirty && kindId) {
            this.dossierStore.loadDossierForKind(kindId);
          }

          this.form.reset();
          this.#router.navigate([Path.RELATIVE_ONE_UP, 'view'], {relativeTo: this.#activatedRoute});
        },
      });

    }
  }
}

function getOptionWithSuffix<T extends {
  aktivStatus: EntityAktivStatus,
  bezeichnung: string
}>(translatedSuffix: string, currentOption?: T) {
  if (currentOption?.aktivStatus === EntityAktivStatus.INAKTIV) {
    return {
      ...currentOption,
      bezeichnung: currentOption.bezeichnung + translatedSuffix,
    };
  }
  return currentOption
}
