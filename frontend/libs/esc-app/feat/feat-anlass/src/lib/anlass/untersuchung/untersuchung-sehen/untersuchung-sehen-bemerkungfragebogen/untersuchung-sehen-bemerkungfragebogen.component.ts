import {ChangeDetectionStrategy, Component, computed, DestroyRef, effect, inject, untracked} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {RequirementSetting} from '@dvb/data-access-backend';
import {BenutzerStore} from '@dvb/feat-shared';
import {
  FormControlComponent,
  FormLayoutComponent,
  SaveCancelButtonsComponent,
  TextareaDirective,
  TitleComponent,
} from '@dvb/ui-components';
import {DirtyMarker} from '@dvb/util-angular';
import {hasSehenEditBerechtigung, requireValue} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';
import {AnlassStore} from '../../../anlass.store';
import {UntersuchungStore} from '../../untersuchung.store';
import {UntersuchungSehenStore} from '../untersuchung-sehen.store';
import {UntersuchungSehenBemerkungfragebogenForm} from './untersuchung-sehen-bemerkungfragebogen.form';

@Component({
  selector: 'feat-untersuchung-sehen-bemerkungfragebogen',
  imports: [
    FormLayoutComponent,
    ReactiveFormsModule,
    SaveCancelButtonsComponent,
    TranslocoPipe,
    FormControlComponent,
    TextareaDirective,
    TitleComponent,
  ],
  templateUrl: './untersuchung-sehen-bemerkungfragebogen.component.html',
  styleUrl: './untersuchung-sehen-bemerkungfragebogen.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UntersuchungSehenBemerkungfragebogenComponent implements DirtyMarker {

  readonly anlassStore = inject(AnlassStore);
  readonly untersuchungStore = inject(UntersuchungStore);
  readonly store = inject(UntersuchungSehenStore);
  readonly destroyRef = inject(DestroyRef);
  readonly benutzerStore = inject(BenutzerStore);

  protected readonly RequirementSetting = RequirementSetting;

  canEdit = computed(() => this.benutzerStore.createHasRequiredRoleSig(hasSehenEditBerechtigung())()
    && this.anlassStore.canUserEditAnlassInCurrentState());

  form = new UntersuchungSehenBemerkungfragebogenForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: this.destroyRef,
  });

  constructor() {
    effect(() => {
      const untersuchung = this.store.sehenBemerkungFragebogen();
      this.form.resetForm(untersuchung?.data);
      const anlassConfig = this.anlassStore.selectedEntity()?.anlassConfig;
      if (anlassConfig) {
        untracked(() => {
          this.form.setValidatorsForAnlassConfig(anlassConfig);
        });
      }
      this.form.setDisabled(!this.canEdit());
    });
  }

  isDirty(): boolean {
    return this.form.isDirty() || this.store.saveLoader.isLoading();
  }

  abbrechen(): void {
    const arztUntersuchung = this.store.sehenBemerkungFragebogen();
    this.form.resetForm(arztUntersuchung?.data);
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const payload = this.form.valueOnValid();
      const anlassId = requireValue(this.anlassStore.selectedEntity()?.id);
      this.store.saveSehenBemerkungFragebogen({
        anlassId: anlassId,
        einladungId: requireValue(this.untersuchungStore.getWrappedEinladungId()),
        untersuchungSehenBemerkungFragebogenSaveJson: payload,
      });
    }
  }
}
