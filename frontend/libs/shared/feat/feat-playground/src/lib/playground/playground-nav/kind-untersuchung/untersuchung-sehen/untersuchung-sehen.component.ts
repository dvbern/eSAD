import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {PlaygroundNavStore} from '../../playground-nav.store';

@Component({
  selector: 'feat-playground-nav-untersuchung-sehen',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<h3>Brille/Linsen</h3>
  <p>{{ navStore.selectedEntityId() }}</p>`,
})
export class PlaygroundNavSehenComponent {
  navStore = inject(PlaygroundNavStore);
}
