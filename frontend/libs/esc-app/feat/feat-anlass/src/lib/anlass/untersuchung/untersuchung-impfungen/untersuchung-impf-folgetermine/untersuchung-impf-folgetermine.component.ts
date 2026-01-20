import {ChangeDetectionStrategy, Component, input} from "@angular/core";
import {DatePipe} from "@angular/common";
import {FormControlComponent, NgSelectDirective, TitleComponent} from "@dvb/ui-components";
import {NgOptionComponent, NgSelectComponent} from "@ng-select/ng-select";
import {TranslocoPipe} from "@jsverse/transloco";
import {
  UntersuchungImpfungenMassnahmenForm
} from "../untersuchung-impfungen-massnahmen/untersuchung-impfungen-massnahmen.form";
import {AnlassOptionJson, AnlassWithDateTimeJson, AnlassWithOpenSlotsOptionJson} from "@dvb/data-access-backend";
import {ReactiveFormsModule} from "@angular/forms";


@Component({
  selector: 'feat-untersuchung-impf-folgetermine',

  styleUrl: 'untersuchung-impf-folgetermine.component.scss',
  templateUrl: 'untersuchung-impf-folgetermine.component.html',
  imports: [
    DatePipe,
    FormControlComponent,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    TitleComponent,
    TranslocoPipe,
    ReactiveFormsModule
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UntersuchungImpfFolgetermineComponent {
  anlaesseWithOpenEinladungenSlots = input<AnlassWithOpenSlotsOptionJson[]>();
  imfanlaesseScheduled = input<AnlassWithDateTimeJson[]>();
  form = input.required<UntersuchungImpfungenMassnahmenForm>();
}
