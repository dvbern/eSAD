import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {PlaygroundNavStore} from '../../playground-nav.store';

@Component({
  selector: 'feat-playground-nav-untersuchung-impfungen',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<h3>Geburtstag: 01.09.2007</h3>
  <p>{{ navStore.selectedEntityId() }}</p>`,
})
export class PlaygroundNavImpfungenComponent {
  navStore = inject(PlaygroundNavStore);
}
