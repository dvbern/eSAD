import {ChangeDetectionStrategy, Component, computed, DestroyRef, effect, inject, untracked} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {RequirementSetting} from '@dvb/data-access-backend';
import {AnlassStore} from '../../../anlass.store';
import {BenutzerStore} from '@dvb/feat-shared';
import {
  FormControlComponent,
  FormLayoutComponent,
  SaveCancelButtonsComponent,
  TextareaDirective,
  TitleComponent,
} from '@dvb/ui-components';
import {DirtyMarker} from '@dvb/util-angular';
import {hasAerztlicheUntersuchungEditBerechtigung, requireValue} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';
import {UntersuchungStore} from '../../untersuchung.store';
import {UntersuchungArztStore} from '../untersuchung-arzt.store';
import {UntersuchungArztBemerkungForm} from './untersuchung-arzt-bemerkung.form';

@Component({
  selector: 'feat-untersuchung-arzt-bemerkung',
  imports: [
    FormLayoutComponent,
    ReactiveFormsModule,
    SaveCancelButtonsComponent,
    TranslocoPipe,
    FormControlComponent,
    TextareaDirective,
    TitleComponent
  ],
  templateUrl: './untersuchung-arzt-bemerkung.component.html',
  styleUrl: './untersuchung-arzt-bemerkung.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UntersuchungArztBemerkungComponent implements DirtyMarker {

  readonly anlassStore = inject(AnlassStore);
  readonly untersuchungStore = inject(UntersuchungStore);
  readonly store = inject(UntersuchungArztStore);
  readonly destroyRef = inject(DestroyRef);
  readonly benutzerStore = inject(BenutzerStore);

  protected readonly RequirementSetting = RequirementSetting;

  canEdit = computed(() => this.benutzerStore.createHasRequiredRoleSig(hasAerztlicheUntersuchungEditBerechtigung())()
    && this.anlassStore.canUserEditAnlassInCurrentState());

  form = new UntersuchungArztBemerkungForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: this.destroyRef,
  });

  constructor() {
    effect(() => {
      const untersuchung = this.store.untersuchungArztBemerkung();
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
    const arztUntersuchung = this.store.untersuchungArztBemerkung();
    this.form.resetForm(arztUntersuchung?.data);
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const payload = this.form.valueOnValid();
      const anlassId = requireValue(this.anlassStore.selectedEntity()?.id);
      this.store.saveUntersuchungArztBemerkung({
        anlassId: anlassId,
        einladungId: requireValue(this.untersuchungStore.getWrappedEinladungId()),
        untersuchungArztBemerkungSaveJson: payload,
      });
    }
  }
}
