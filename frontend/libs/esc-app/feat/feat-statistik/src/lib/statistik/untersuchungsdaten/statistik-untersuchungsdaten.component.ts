import {ChangeDetectionStrategy, Component} from '@angular/core';
import {DirtyMarker} from '@dvb/util-angular';

@Component({
  selector: 'feat-statistik-untersuchungsdaten',
  templateUrl: './statistik-untersuchungsdaten.component.html',
  styleUrl: './statistik-untersuchungsdaten.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [],
})
export class StatistikUntersuchungsdatenComponent implements DirtyMarker {
  isDirty(): boolean | Promise<boolean> {
    throw new Error("Method not implemented.");
  }

}
