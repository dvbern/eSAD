import {ChangeDetectionStrategy, Component, DestroyRef, effect, inject, untracked} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {Nachsprechen, RequirementSetting, Sprachverstaendnis, Wortschatz} from '@dvb/data-access-backend';
import {
  FormControlComponent,
  FormLayoutComponent,
  SaveCancelButtonsComponent,
  TextareaDirective,
  TitleComponent,
} from '@dvb/ui-components';
import {DirtyMarker} from '@dvb/util-angular';
import {requireValue} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';
import {UntersuchungSpracheForm} from './untersuchung-sprache.form';
import {AnlassStore} from '../../anlass.store';
import {UntersuchungStore} from '../untersuchung.store';
import {UntersuchungSpracheStore} from './untersuchung-sprache.store';
import {createSingleValueEnhancedForm, TenantSettingsStore, ValueEnhancedComponent} from '@dvb/feat-shared';

@Component({
  selector: 'feat-untersuchung-sprache',
  imports: [
    ReactiveFormsModule,
    SaveCancelButtonsComponent,
    TranslocoPipe,
    FormLayoutComponent,
    FormControlComponent,
    TextareaDirective,
    TitleComponent,
    ValueEnhancedComponent,
  ],
  templateUrl: './untersuchung-sprache.component.html',
  styleUrl: './untersuchung-sprache.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UntersuchungSpracheComponent implements DirtyMarker {
  protected readonly RequirementSetting = RequirementSetting;

  readonly anlassStore = inject(AnlassStore);
  readonly untersuchungStore = inject(UntersuchungStore);
  readonly tenantSettingsStore = inject(TenantSettingsStore);
  readonly store = inject(UntersuchungSpracheStore);

  nachsprechenItems = Object.values(Nachsprechen);
  wortschatzItems = Object.values(Wortschatz);
  sprachverstaendnisItems = Object.values(Sprachverstaendnis);

  form = new UntersuchungSpracheForm({
    fb: inject(NonNullableFormBuilder),
    nachsprechenValueEnhancedForm: createSingleValueEnhancedForm(this.nachsprechenItems),
    wortschatzValueEnhancedForm: createSingleValueEnhancedForm(this.wortschatzItems),
    sprachverstaendnisValueEnhancedForm: createSingleValueEnhancedForm(this.sprachverstaendnisItems),
    destroyRef: inject(DestroyRef),
  });

  constructor() {
    effect(() => {
      const untersuchungSprache = this.store.untersuchungSprache();
      this.form.resetForm(untersuchungSprache?.data);
      const anlassConfig = this.anlassStore.selectedEntity()?.anlassConfig;
      const nichtGeprueftGrundEnabled = this.tenantSettingsStore.nichtGeprueftGrundEnabled();
      if (anlassConfig) {
        untracked(() => {
          this.form.setValidatorsForAnlassConfig(anlassConfig, !!nichtGeprueftGrundEnabled);
        });
      }
      this.form.setDisabled(!this.anlassStore.canUserEditAnlassInCurrentState());
    });
  }

  isDirty(): boolean {
    return this.form.isDirty() || this.store.saveLoader.isLoading();
  }

  abbrechen(): void {
    const untersuchungSprache = this.store.untersuchungSprache();
    this.form.resetForm(untersuchungSprache?.data);
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const anlassId = requireValue(this.anlassStore.selectedEntity()?.id);
      const payload = this.form.valueOnValid();
      this.store.saveUntersuchungSprache({
        anlassId: anlassId,
        einladungId: requireValue(this.untersuchungStore.getWrappedEinladungId()),
        untersuchungSpracheSaveJson: payload,
      });
    }
  }
}
