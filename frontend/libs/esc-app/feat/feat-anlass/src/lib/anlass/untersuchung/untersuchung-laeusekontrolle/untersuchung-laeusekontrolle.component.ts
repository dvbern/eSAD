import { ChangeDetectionStrategy, Component, DestroyRef, effect, inject, untracked } from '@angular/core';
import { FormArray, NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RequirementSetting } from '@dvb/data-access-backend';
import {
  CheckboxDirective,
  FormControlComponent,
  FormLayoutComponent,
  KachelControlComponent,
  KachelFormSectionComponent,
  SaveCancelButtonsComponent,
  TextareaDirective,
  TitleComponent,
} from '@dvb/ui-components';
import { DirtyMarker } from '@dvb/util-angular';
import { requireValue } from '@dvb/util-shared';
import { TranslocoPipe } from '@jsverse/transloco';
import { AnlassStore } from '../../anlass.store';
import { UntersuchungStore } from '../untersuchung.store';
import {
  LaeusekontrolleBeurteilungSubFormType,
  UntersuchungLaeusekontrolleForm,
} from './untersuchung-laeusekontrolle.form';
import { UntersuchungLaeusekontrolleStore } from './untersuchung-laeusekontrolle.store';

@Component({
  selector: 'feat-untersuchung-triage',
  imports: [
    CheckboxDirective,
    ReactiveFormsModule,
    TextareaDirective,
    TranslocoPipe,
    SaveCancelButtonsComponent,
    KachelControlComponent,
    KachelFormSectionComponent,
    FormControlComponent,
    TitleComponent,
    FormLayoutComponent,
  ],
  templateUrl: './untersuchung-laeusekontrolle.component.html',
  styleUrl: './untersuchung-laeusekontrolle.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UntersuchungLaeusekontrolleComponent implements DirtyMarker {
  readonly store = inject(UntersuchungLaeusekontrolleStore);
  readonly anlassStore = inject(AnlassStore);
  readonly untersuchungStore = inject(UntersuchungStore);

  readonly RequirementSetting = RequirementSetting;

  public form = new UntersuchungLaeusekontrolleForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
  });

  constructor() {
    effect(() => {
      const laeusekontrolle = this.store.laeusekontrolle();
      this.form.resetForm(laeusekontrolle?.data);
      const anlassConfig = this.anlassStore.selectedEntity()?.anlassConfig;
      if (anlassConfig) {
        untracked(() => {
          this.form.setValidators(anlassConfig);
        });
      }
      this.form.setDisabled(!this.anlassStore.canUserEditAnlassInCurrentState());
    });
  }

  isDirty(): boolean {
    return this.form.isDirty() || this.store.saveLoader.isLoading();
  }

  abbrechen(): void {
    this.form.reset(this.store.laeusekontrolle()?.data ?? undefined);
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const payload = this.form.valueOnValid();
      const anlassId = requireValue(this.anlassStore.selectedEntity()?.id);
      const einladungId = requireValue(this.untersuchungStore.currentTab()?.einladungID);
      this.store.saveLaeusekontrolle({
        anlassId: anlassId,
        einladungId: einladungId,
        untersuchungLaeusekontrolleSaveJson: payload,
      });
    }
  }

  getBeurteilungFormArray(): FormArray<LaeusekontrolleBeurteilungSubFormType> {
    return this.form.group.controls.beurteilungArray;
  }
}
