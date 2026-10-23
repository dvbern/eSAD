
import {ChangeDetectionStrategy, Component, inject, ViewChild} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {RequirementSetting,} from '@dvb/data-access-backend';
import {CardInCardComponent, IconComponent,} from '@dvb/ui-components';
import {DirtyMarker} from '@dvb/util-angular';
import {TranslocoPipe} from '@jsverse/transloco';
import {AnlassStore} from '../../anlass.store';
import {UntersuchungSehenStore} from './untersuchung-sehen.store';
import {
  UntersuchungSehenBeurteilungComponent
} from "./untersuchung-sehen-beurteilung/untersuchung-sehen-beurteilung.component";
import {
  UntersuchungSehenMassnahmenComponent
} from "./untersuchung-sehen-massnahmen/untersuchung-sehen-massnahmen.component";
import {
  UntersuchungSehenBemerkungfragebogenComponent
} from "./untersuchung-sehen-bemerkungfragebogen/untersuchung-sehen-bemerkungfragebogen.component";

@Component({
  selector: 'feat-untersuchung-sehen',
  imports: [
    ReactiveFormsModule,
    TranslocoPipe,
    CardInCardComponent,
    IconComponent,
    UntersuchungSehenBeurteilungComponent,
    UntersuchungSehenMassnahmenComponent,
    UntersuchungSehenBemerkungfragebogenComponent,
  ],
  templateUrl: './untersuchung-sehen.component.html',
  styleUrl: './untersuchung-sehen.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UntersuchungSehenComponent implements DirtyMarker {

  @ViewChild(UntersuchungSehenBeurteilungComponent) sehenBeurteilungComponent?: UntersuchungSehenBeurteilungComponent;
  @ViewChild(UntersuchungSehenMassnahmenComponent) sehenMassnahmenComponent?: UntersuchungSehenMassnahmenComponent;
  @ViewChild(UntersuchungSehenBemerkungfragebogenComponent) sehenBemerkungFragebogenComponent?: UntersuchungSehenBemerkungfragebogenComponent;

  protected readonly RequirementSetting = RequirementSetting;

  readonly anlassStore = inject(AnlassStore);
  readonly sehenStore = inject(UntersuchungSehenStore);


  isDirty(): boolean {
    return (this.sehenBeurteilungComponent?.isDirty() ?? false)
      || (this.sehenMassnahmenComponent?.isDirty() ?? false)
      || (this.sehenBemerkungFragebogenComponent?.isDirty() ?? false);
  }

  loadDefault(): void {
    this.sehenBeurteilungComponent?.loadDefault();
  }
}
