import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'util-diry-guard-modal',
  standalone: true,
  templateUrl: 'dirty-guard-modal.component.html',
  styleUrl: 'dirty-guard-modal.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,

  imports: [
    TranslocoPipe,
  ],
})
export class DirtyGuardModalComponent {
  activeModal = inject(NgbActiveModal);
}
