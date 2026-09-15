import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  effect,
  inject,
  output,
  untracked
} from '@angular/core';
import {FormsModule, NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {ImpfausweisGrundUnvollstaendig, ImpfausweisVollstaendig, RequirementSetting,} from '@dvb/data-access-backend';
import {BenutzerStore, TenantSettingsStore,} from '@dvb/feat-shared';
import {
  FormControlComponent,
  FormLayoutComponent,
  HeadingDirective,
  IconComponent,
  KachelControlComponent,
  KachelFormSectionComponent,
  NgSelectDirective,
  RadioDirective,
  SaveCancelButtonsComponent,
} from '@dvb/ui-components';
import {DirtyMarker} from '@dvb/util-angular';
import {hasImpfungenEditBerechtigung, Nullish, requireValue} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';
import {AnlassStore} from '../../../anlass.store';
import {UntersuchungStore} from '../../untersuchung.store';
import {UntersuchungImpfungenStore} from '../untersuchung-impfungen.store';
import {UntersuchungImpfungenNurUnvollstaendigeForm} from './untersuchung-impfungen-nur-unvollstaendige.form';
import {NgOptionComponent, NgSelectComponent} from "@ng-select/ng-select";

@Component({
  selector: 'feat-untersuchung-impfungen-nur-unvollstaendige',
  imports: [
    FormLayoutComponent,
    ReactiveFormsModule,
    TranslocoPipe,
    SaveCancelButtonsComponent,
    HeadingDirective,
    FormsModule,
    FormControlComponent,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    KachelControlComponent,
    KachelFormSectionComponent,
    RadioDirective,
    IconComponent,
  ],
  templateUrl: './untersuchung-impfungen-nur-unvollstaendige.component.html',
  styleUrl: './untersuchung-impfungen-nur-unvollstaendige.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UntersuchungImpfungenNurUnvollstaendigeComponent implements DirtyMarker {
  readonly impfungenStore = inject(UntersuchungImpfungenStore);
  readonly anlassStore = inject(AnlassStore);
  readonly untersuchungStore = inject(UntersuchungStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly tenantSettingsStore = inject(TenantSettingsStore);

  impfausweisVollstaendigValues = Object.values(ImpfausweisVollstaendig);
  impfausweisGrundUnvollstaendigValues = Object.values(ImpfausweisGrundUnvollstaendig);

  protected readonly RequirementSetting = RequirementSetting;
  protected readonly ImpfausweisVollstaendig = ImpfausweisVollstaendig;

  impfausweisVollstaendig = output<boolean>();

  canEdit = computed(
    () =>
      this.benutzerStore.createHasRequiredRoleSig(hasImpfungenEditBerechtigung())() &&
      this.anlassStore.canUserEditAnlassInCurrentState(),
  );

  readonly form = new UntersuchungImpfungenNurUnvollstaendigeForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
  });

  constructor() {
    effect(() => {
      const data = this.impfungenStore.nurUnvollstaendige();
      this.form.reset(data?.data);
      this.impfausweisVollstaendigChanged(data?.data.impfausweisVollstaendig);
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
    return this.form.isDirty() || this.impfungenStore.saveNurUnvollstaendigeLoader.isLoading();
  }

  abbrechen(): void {
    const dataMinimal = this.impfungenStore.nurUnvollstaendige();
    this.form.reset(dataMinimal?.data);
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const payload = this.form.valueOnValid();
      this.impfungenStore.saveNurUnvollstaendige({
        anlassId: requireValue(this.anlassStore.selectedEntity()).id,
        einladungId: requireValue(this.untersuchungStore.getWrappedEinladungId()),
        untersuchungImpfungenNurUnvollstaendigeSaveJson: payload,
      });
    }
  }

  protected impfausweisVollstaendigChanged(value: ImpfausweisVollstaendig | Nullish) {
    this.impfausweisVollstaendig.emit(value === ImpfausweisVollstaendig.NEIN);
  }
}
