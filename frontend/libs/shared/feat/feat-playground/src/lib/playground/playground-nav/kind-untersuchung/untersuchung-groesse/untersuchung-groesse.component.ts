import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {PlaygroundNavStore} from '../../playground-nav.store';

@Component({
  selector: 'feat-playground-nav-untersuchung-groesse',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<h3>Grösse</h3>
  <p>{{ navStore.selectedEntityId() }}</p>`,
})
export class PlaygroundNavGroesseComponent {
  navStore = inject(PlaygroundNavStore);
}
