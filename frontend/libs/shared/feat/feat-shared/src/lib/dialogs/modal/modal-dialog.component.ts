import {ChangeDetectionStrategy, Component, inject, input} from '@angular/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'feat-dialogs-modal',
  standalone: true,
  templateUrl: './modal-dialog.component.html',
  styleUrl: './modal-dialog.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ModalDialogComponent {
  activeModal = inject(NgbActiveModal);
  modalTitle = input.required<string>();
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  dismissReason = input<any>('Cross click');
}
