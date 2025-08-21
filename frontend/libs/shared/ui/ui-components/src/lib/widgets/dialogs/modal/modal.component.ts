import { ChangeDetectionStrategy, Component, inject, input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'svui-modal',
  standalone: true,
  templateUrl: './modal.component.html',
  styleUrl: './modal.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ModalComponent {
  readonly activeModal = inject(NgbActiveModal);
  readonly modalTitle = input.required<string>();
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  readonly dismissReason = input<any>('Cross click');
}
