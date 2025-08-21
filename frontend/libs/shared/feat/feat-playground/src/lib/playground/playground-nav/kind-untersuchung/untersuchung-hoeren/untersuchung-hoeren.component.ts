import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {PlaygroundNavStore} from '../../playground-nav.store';

@Component({
  selector: 'feat-playground-nav-untersuchung-hoeren',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<h3>Gehör</h3>
  <p>{{ navStore.selectedEntityId() }}</p>`,
})
export class PlaygroundNavHoerenComponent {
  navStore = inject(PlaygroundNavStore);
}
