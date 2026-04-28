import {ChangeDetectionStrategy, Component} from '@angular/core';
import {DirtyMarker} from '@dvb/util-angular';

@Component({
  selector: 'feat-statistik-basisdaten',
  templateUrl: './statistik-basisdaten.component.html',
  styleUrl: './statistik-basisdaten.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [],
})
export class StatistikBasisdatenComponent implements DirtyMarker {
  isDirty(): boolean | Promise<boolean> {
    throw new Error("Method not implemented.");
  }

}
