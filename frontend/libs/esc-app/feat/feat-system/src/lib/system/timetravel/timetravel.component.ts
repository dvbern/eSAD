import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, effect, inject, untracked} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {ISODateString} from '@dvb/data-access-types';
import {BenutzerStore} from '@dvb/feat-shared';
import {Path, TimetravelStore} from '@dvb/feat-shared';
import {DeploymentStore} from '@dvb/feat-shared';
import {
  ButtonComponent,
  CardComponent,
  HeadingDirective,
  StandaloneControlComponent,
  TextInputDirective,
} from '@dvb/ui-components';
import {DetailMode} from '@dvb/util-angular';
import {hasSystemPageViewBerechtiung} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';
import {TimetravelForm} from './timetravel-form';

@Component({
  selector: 'feat-system-timetravel',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    CardComponent,
    HeadingDirective,
    ReactiveFormsModule,
    TextInputDirective,
    ButtonComponent,
    TranslocoPipe,
    StandaloneControlComponent,

  ],
  templateUrl: './timetravel.component.html',
  styleUrl: './timetravel.component.scss',
})
export class TimetravelComponent {
  protected readonly Path = Path;

  benutzerStore = inject(BenutzerStore);

  timetravelStore = inject(TimetravelStore);
  timetravelForm = new TimetravelForm();

  deploymentStore = inject(DeploymentStore);
  isTimetravelEnabled = this.deploymentStore.isTimetravelEnabled;

  hasTimetravelRights = this.benutzerStore.createHasRequiredRoleSig(hasSystemPageViewBerechtiung());

  protected readonly DetailMode = DetailMode;

  constructor() {
    effect(() => {
      const date: ISODateString | undefined = this.timetravelStore.now();
      untracked(() => {
        this.timetravelForm.group.reset({date: date});
      });
    });
  }

  async submitTimetravelIfValid(): Promise<void> {
    if (await this.timetravelForm.validate()) {
      const payload = this.timetravelForm.valueOnValid();
      this.timetravelStore.updateNow({
        date: payload.date,
        onSuccess: () => {
          console.log('updated time travel date');
        },
      });
    }
  }

  resetTimetravel(): void {
    this.timetravelStore.resetNow({
      onSuccess: () => {
        console.log('reset time travel date');
      },
    })
  }

}
