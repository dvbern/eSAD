import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {Path} from '@dvb/feat-shared';
import {
  AnlassSummaryLayoutComponent,
  CardComponent,
  CardHeaderComponent,
  InfoItemComponent,
  SummaryValuesComponent,
} from '@dvb/ui-components';
import {fromISODateString} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';
import {AnlassStore} from '../../../anlass.store';
import {AnlassKinderStore} from '../../anlass-kinder.store';
import {BemerkungenComponent} from '../bemerkungen/bemerkungen.component';

@Component({
  selector: 'feat-anlass-planung-anlassdaten-daten',
  templateUrl: './daten.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    AnlassSummaryLayoutComponent,
    SummaryValuesComponent,
    TranslocoPipe,
    BemerkungenComponent,
    CardComponent,
    CardHeaderComponent,
    SummaryValuesComponent,
    InfoItemComponent,
  ],
})
export class DatenComponent {

  readonly store = inject(AnlassStore);
  readonly anlassKinderStore = inject(AnlassKinderStore);

  protected readonly Path = Path;
  protected readonly fromISODateString = fromISODateString;
}
