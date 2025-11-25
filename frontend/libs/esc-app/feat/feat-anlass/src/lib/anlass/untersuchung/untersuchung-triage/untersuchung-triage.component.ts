
import {ChangeDetectionStrategy, Component, DestroyRef, effect, inject} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {NaechsteStation, UntersuchungTriageSaveJson} from '@dvb/data-access-backend';
import {
  CheckboxDirective,
  HeadingDirective,
  KachelControlComponent,
  KachelFormSectionComponent,
  RadioDirective,
  SaveCancelButtonsComponent,
  StandaloneControlComponent,
  TextareaDirective,
} from '@dvb/ui-components';
import {DirtyMarker} from '@dvb/util-angular';
import {requireValue} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';
import {AnlassStore} from '../../anlass.store';
import {UntersuchungStore} from '../untersuchung.store';
import {UntersuchungTriageForm} from './untersuchung-triage.form';
import {UntersuchungTriageStore} from './untersuchung-triage.store';

@Component({
  selector: 'feat-untersuchung-triage',
  imports: [
    CheckboxDirective,
    ReactiveFormsModule,
    StandaloneControlComponent,
    TextareaDirective,
    TranslocoPipe,
    HeadingDirective,
    SaveCancelButtonsComponent,
    KachelControlComponent,
    RadioDirective,
    KachelFormSectionComponent
],
  templateUrl: './untersuchung-triage.component.html',
  styleUrl: './untersuchung-triage.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UntersuchungTriageComponent implements DirtyMarker {
  readonly store = inject(UntersuchungTriageStore);
  readonly anlassStore = inject(AnlassStore);
  readonly untersuchungStore = inject(UntersuchungStore);
  readonly stationen = Object.values(NaechsteStation);

  public form = new UntersuchungTriageForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
  });

  triagePairs: {
    nameCheckbox: keyof UntersuchungTriageSaveJson,
    nameBemerkung: keyof UntersuchungTriageSaveJson,
    label: string
  }[] = [
    {nameCheckbox: 'triageSu', nameBemerkung: 'triageSuBemerkungen', label: 'untersuchung.tab.triage.triageSu'},
    {nameCheckbox: 'triageMpa', nameBemerkung: 'triageMpaBemerkungen', label: 'untersuchung.tab.triage.triageMpa'},
    {nameCheckbox: 'befundSu', nameBemerkung: 'befundSuBemerkungen', label: 'untersuchung.tab.triage.befundSu'},
    {
      nameCheckbox: 'wunschSchueler',
      nameBemerkung: 'wunschSchuelerBemerkungen',
      label: 'untersuchung.tab.triage.wunschSchueler',
    },
    {
      nameCheckbox: 'vorinformationLehrperson',
      nameBemerkung: 'vorinformationLehrpersonBemerkungen',
      label: 'untersuchung.tab.triage.vorinformationLehrperson',
    },
  ];

  constructor() {
    effect(() => {
      const triage = this.store.triage();
      if (triage) {
        this.form.reset(triage.data);
      }
      this.form.setDisabled(!this.anlassStore.canUserEditAnlassInCurrentState());
    });
  }

  isDirty(): boolean {
    return this.form.isDirty() || this.store.saveLoader.isLoading();
  }

  abbrechen(): void {
    this.form.reset(this.store.triage()?.data ?? undefined);
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const payload = this.form.valueOnValid();
      const anlassId = requireValue(this.anlassStore.selectedEntity()?.id);
      const einladungId = requireValue(this.untersuchungStore.currentTab()?.einladungID);
      this.store.saveTriage({
        anlassId: anlassId,
        einladungId: einladungId,
        untersuchungTriageSaveJson: payload,
      });
    }
  }
}
