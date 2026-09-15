import {ChangeDetectionStrategy, Component, computed, DestroyRef, effect, inject, untracked} from '@angular/core';
import {FormArray, FormsModule, NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {Router, UrlTree} from '@angular/router';
import {AnlassArt, AnlassSaveJson, AnlassTyp, InformationEltern, RequirementSetting} from '@dvb/data-access-backend';
import {BenutzerStore, Path} from '@dvb/feat-shared';
import {
  ButtonComponent,
  ButtonsRightContainerComponent,
  CheckboxDirective,
  FormControlComponent,
  FormLayoutComponent,
  HeadingDirective,
  KachelControlComponent,
  KachelFormSectionComponent,
  NgSelectDirective,
  SaveCancelButtonsComponent,
  StandaloneControlComponent,
  TextareaDirective,
  TitleComponent,
} from '@dvb/ui-components';
import {DirtyMarker} from '@dvb/util-angular';
import {hasImpfungenEditBerechtigung, isNotNullish, requireValue} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {AnlassStore} from '../../../anlass.store';
import {UntersuchungStore} from '../../untersuchung.store';
import {UntersuchungImpfungenStore} from '../untersuchung-impfungen.store';
import {EmpfehlungSubFormType, UntersuchungImpfungenMassnahmenForm} from './untersuchung-impfungen-massnahmen.form';
import {requireValidBackendEntityId} from '@dvb/data-access-types';
import {
  UntersuchungImpfFolgetermineComponent
} from '../untersuchung-impf-folgetermine/untersuchung-impf-folgetermine.component';

@Component({
  selector: 'feat-untersuchung-impfungen-massnahmen',
  imports: [
    FormLayoutComponent,
    ReactiveFormsModule,
    CheckboxDirective,
    TranslocoPipe,
    FormControlComponent,
    NgSelectDirective,
    NgOptionComponent,
    NgSelectComponent,
    SaveCancelButtonsComponent,
    StandaloneControlComponent,
    HeadingDirective,
    ButtonComponent,
    ButtonsRightContainerComponent,
    TextareaDirective,
    FormsModule,
    KachelControlComponent,
    KachelFormSectionComponent,
    TitleComponent,
    UntersuchungImpfFolgetermineComponent,
  ],
  templateUrl: './untersuchung-impfungen-massnahmen.component.html',
  styleUrl: './untersuchung-impfungen-massnahmen.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UntersuchungImpfungenMassnahmenComponent implements DirtyMarker {
  readonly impfungenStore = inject(UntersuchungImpfungenStore);
  readonly anlassStore = inject(AnlassStore);
  readonly untersuchungStore = inject(UntersuchungStore);
  readonly benutzerStore = inject(BenutzerStore);

  readonly #router = inject(Router);
  readonly #translocoService = inject(TranslocoService);

  informationEltern = Object.values(InformationEltern);
  protected readonly RequirementSetting = RequirementSetting;

  canEdit = computed(
    () =>
      this.benutzerStore.createHasRequiredRoleSig(hasImpfungenEditBerechtigung())() &&
      this.anlassStore.canUserEditAnlassInCurrentState(),
  );

  readonly form = new UntersuchungImpfungenMassnahmenForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
  });

  constructor() {
    effect(() => {
      const massnahmen = this.impfungenStore.massnahmen();
      const anlassConfig = this.anlassStore.selectedEntity()?.anlassConfig;
      this.form.resetForm(massnahmen?.data, anlassConfig);
      if (anlassConfig) {
        untracked(() => {
          this.form.setValidatorsForAnlassConfig(anlassConfig);
        });
      }
      this.form.setDisabled(!this.canEdit());
    });
  }

  isDirty(): boolean {
    return this.form.isDirty() || this.impfungenStore.loader.isLoading();
  }

  abbrechen(): void {
    const massnahmen = this.impfungenStore.massnahmen();
    const anlassConfig = this.anlassStore.selectedEntity()?.anlassConfig;
    this.form.resetForm(massnahmen?.data, anlassConfig);
    this.form.getAndResetImpfFolgetermin();
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const payload = this.form.valueOnValid();
      this.impfungenStore.saveMassnahmen({
        anlassId: requireValue(this.anlassStore.selectedEntity()).id,
        einladungId: requireValue(this.untersuchungStore.getWrappedEinladungId()),
        untersuchungImpfungenMassnahmenSaveJson: payload,
      });

      const anlassId = this.form.getAndResetImpfFolgetermin();

      if (isNotNullish(anlassId)) {
        this.impfungenStore.addKindToFolgetermin({
          folgeTerminAnlassId: requireValidBackendEntityId(anlassId),
          kindId: requireValidBackendEntityId(this.untersuchungStore.currentDossier()?.kind.id),
          currentAnlassId: requireValidBackendEntityId(this.anlassStore.selectedEntity()?.id),
        });
      }
    }
  }

  public einzelFolgeterminErstellen(): void {
    this.impfungenStore.einzelTerminErstellen({
      anlassId: requireValue(this.anlassStore.selectedEntity()).id,
      einladungId: requireValidBackendEntityId(requireValue(this.untersuchungStore.currentTab()).einladungID),
      onSuccess: () => {
        const anlassTitel = this.#translocoService.translate('anlass.impfen.einzeltermin', {
          kind: this.untersuchungStore.currentTab()?.label,
        });
        const anlassPartial: Partial<AnlassSaveJson> = {
          titel: anlassTitel,
          anlassArt: AnlassArt.EINZEL_ANLASS,
          anlassTyp: AnlassTyp.IMPFTERMIN,
        };
        const url: UrlTree = this.#router.createUrlTree([Path.ANLASS, Path.CREATE], {
          queryParams: {
            anlassPartial: JSON.stringify(anlassPartial),
            kindId: this.untersuchungStore.currentTab()?.kindID,
          },
        });
        window.open(url.toString(), '_blank');
      },
    });
  }

  getEmpfehlungFormArray(): FormArray<EmpfehlungSubFormType> {
    return this.form.group.controls.empfehlungArray;
  }
}
