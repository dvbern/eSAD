import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  effect,
  inject,
  input,
  output,
  untracked,
} from '@angular/core';
import {FormsModule, NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {
  AnlassConfigJson,
  Krankheit,
  UntersuchungImpfstatusListSaveJson,
  UntersuchungImpfungenImpfausweisSaveJson,
} from '@dvb/data-access-backend';
import {BenutzerStore} from '@dvb/feat-shared';
import {
  FormErrorMessageComponent,
  HeadingDirective,
  SaveCancelButtonsComponent,
  StatusTextComponent,
} from '@dvb/ui-components';
import {hasImpfungenEditBerechtigung} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';
import {AnlassStore} from '../../../anlass.store';
import {UntersuchungStore} from '../../untersuchung.store';
import {
  UntersuchungImpfstatusListComponent,
} from '../untersuchung-impfstatus-list/untersuchung-impfstatus-list.component';
import {UntersuchungImpfstatusListForm} from '../untersuchung-impfstatus-list/untersuchung-impfstatus-list.form';

@Component({
  selector: 'feat-untersuchung-impfstatus',
  templateUrl: './untersuchung-impfstatus.component.html',
  styleUrl: './untersuchung-impfstatus.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    TranslocoPipe,
    FormErrorMessageComponent,
    UntersuchungImpfstatusListComponent,
    HeadingDirective,
    SaveCancelButtonsComponent,
    StatusTextComponent,
  ],
})
export class UntersuchungImpfstatusComponent {
  krankheitenLeft: Krankheit[] = ['DIPHTERIE', 'TETANUS', 'PERTUSSIS', 'POLIO', 'HEPATITIS_B', 'HPV'];
  krankheitenRight: Krankheit[] = [
    'MASERN',
    'MUMPS',
    'ROETELN',
    'VARIZELLEN',
    'MENINGOKOKKEN_A_C_W_Y',
    'MENINGOKOKKEN_B',
  ];
  columns = [this.krankheitenLeft, this.krankheitenRight];

  readonly benutzerStore = inject(BenutzerStore);
  readonly anlassStore = inject(AnlassStore);
  readonly untersuchungStore = inject(UntersuchungStore);

  public form: UntersuchungImpfstatusListForm = new UntersuchungImpfstatusListForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    krankheiten: [...this.krankheitenLeft, ...this.krankheitenRight],
  });

  dossier = computed(() => this.untersuchungStore.currentDossier());
  hasImpfungenEditBerechtigung = this.benutzerStore.createHasRequiredRoleSig(hasImpfungenEditBerechtigung());

  impfstatusList = input<UntersuchungImpfstatusListSaveJson | null>();
  anlassConfig = input<AnlassConfigJson>();
  impfausweis = input<UntersuchungImpfungenImpfausweisSaveJson>();
  loading = input.required<boolean>();
  saveImpfstatusList = output<UntersuchungImpfstatusListSaveJson>();

  constructor() {
    effect(() => {
      const data = this.impfstatusList();
      this.form.resetForm(data);
    });

    effect(() => {
      const anlassConfig = this.anlassConfig();
      const impfungen = this.impfausweis();
      if (anlassConfig && impfungen) {
        untracked(() => {
          this.form.setValidatorsForAnlassConfig(anlassConfig, impfungen);
        });
      }
      this.form.setDisabled(!this.anlassStore.canUserEditAnlassInCurrentState());
    });
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const payload: UntersuchungImpfstatusListSaveJson = this.form.valueOnValid();
      this.saveImpfstatusList.emit(payload);
    }
  }

  abbrechen(): void {
    const data = this.impfstatusList();
    this.form.resetForm(data);
  }
}


