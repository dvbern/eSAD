import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {PlaygroundNavStore} from '../../playground-nav.store';

@Component({
  selector: 'feat-playground-nav-untersuchung-daten',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<h3>Personendaten</h3><p>{{ navStore.selectedEntityId() }}</p>`,
})
export class PlaygroundNavDatenComponent {
  navStore = inject(PlaygroundNavStore);
}
