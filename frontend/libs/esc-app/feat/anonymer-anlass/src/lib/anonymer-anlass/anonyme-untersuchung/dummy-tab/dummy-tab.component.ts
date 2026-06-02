import {ChangeDetectionStrategy, Component} from '@angular/core';

@Component({
  selector: 'feat-statistik-dummy-tab',
  template: '<p>DUMMY TAB</p>',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DummyTabComponent {}
