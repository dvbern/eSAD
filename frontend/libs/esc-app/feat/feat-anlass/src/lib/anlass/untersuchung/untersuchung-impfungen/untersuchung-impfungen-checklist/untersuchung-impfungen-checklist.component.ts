import {ChangeDetectionStrategy, Component, computed, DestroyRef, effect, inject, untracked} from '@angular/core';
import {FormsModule, NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {RequirementSetting, UntersuchungImpfungenChecklisteSaveJson} from '@dvb/data-access-backend';
import {BenutzerStore} from '@dvb/feat-shared';
import {
  CheckboxDirective,
  FormLayoutComponent,
  HeadingDirective,
  SaveCancelButtonsComponent,
  StandaloneControlComponent,
  TextareaDirective,
} from '@dvb/ui-components';
import {DetailMode, DirtyMarker} from '@dvb/util-angular';
import {hasImpfungenEditBerechtigung, requireValue} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';
import {AnlassStore} from '../../../anlass.store';
import {UntersuchungStore} from '../../untersuchung.store';
import {UntersuchungImpfungenStore} from '../untersuchung-impfungen.store';
import {UntersuchungImpfungenChecklistForm} from './untersuchung-impfungen-checklist.form';

@Component({
  selector: 'feat-untersuchung-impfungen-checklist',
  templateUrl: './untersuchung-impfungen-checklist.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FormsModule,
    TranslocoPipe,
    ReactiveFormsModule,
    SaveCancelButtonsComponent,
    CheckboxDirective,
    StandaloneControlComponent,
    FormLayoutComponent,
    HeadingDirective,
    TextareaDirective,
  ],
})
export class UntersuchungImpfungenChecklistComponent implements DirtyMarker {
  readonly #store = inject(UntersuchungImpfungenStore);
  readonly anlassStore = inject(AnlassStore);
  readonly #untersuchungStore = inject(UntersuchungStore);
  readonly benutzerStore = inject(BenutzerStore);

  checklistSig = computed(() => this.#store.checklist);

  canEdit = computed(
    () =>
      this.benutzerStore.createHasRequiredRoleSig(hasImpfungenEditBerechtigung())() &&
      this.anlassStore.canUserEditAnlassInCurrentState(),
  );

  public form = new UntersuchungImpfungenChecklistForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
  });

  checklistPair: {
    nameCheckbox: keyof UntersuchungImpfungenChecklisteSaveJson;
    label: string;
    isVisible: boolean;
  }[] = [
    {
      nameCheckbox: 'checkEinwilligungVorhanden',
      label: 'untersuchung.impfungen.checklist.einverstandnis-vorhanden',
      isVisible:
        this.anlassStore.selectedEntity()?.anlassConfig?.checkEinwilligungVorhanden !== RequirementSetting.NOT_VISIBLE,
    },
    {
      nameCheckbox: 'checkKontraindikationenAbgeklaert',
      label: 'untersuchung.impfungen.checklist.kontraindikationen-abgeklaert',
      isVisible:
        this.anlassStore.selectedEntity()?.anlassConfig?.checkKontraindikationenAbgeklaert !==
        RequirementSetting.NOT_VISIBLE,
    },
    {
      nameCheckbox: 'checkDoppelkontrolleImpfentscheidDurchgefuehrt',
      label: 'untersuchung.impfungen.checklist.doppelkontrolle-impfentscheid-gemacht',
      isVisible:
        this.anlassStore.selectedEntity()?.anlassConfig?.checkDoppelkontrolleImpfentscheidDurchgefuehrt !==
        RequirementSetting.NOT_VISIBLE,
    },
  ];

  constructor() {
    effect(() => {
      const data = this.checklistSig()();
      untracked(() => {
        this.form.resetForm(data);
      });
      const anlassConfig = this.anlassStore.selectedEntity()?.anlassConfig;
      if (anlassConfig) {
        untracked(() => {
          this.form.setValidatorsForAnlassConfig(anlassConfig);
        });
      }
      this.form.setDisabled(!this.canEdit());
    });
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const checklist = this.form.valueOnValid();
      this.#store.changeUntersuchungImpfungenChecklist({
        anlassId: requireValue(this.anlassStore.selectedEntity()).id,
        einladungId: requireValue(this.#untersuchungStore.currentTab()).einladungID,
        checklist: checklist,
      });
    }
  }

  abbrechen(): void {
    const data = this.checklistSig()();
    untracked(() => {
      this.form.resetForm(data);
    });
  }

  isDirty(): boolean {
    return this.form.isDirty() || this.#store.loader.isLoading();
  }

  protected readonly DetailMode = DetailMode;
  protected readonly RequirementSetting = RequirementSetting;
}
