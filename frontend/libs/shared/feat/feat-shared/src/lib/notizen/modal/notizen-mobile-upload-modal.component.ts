import { ChangeDetectionStrategy, Component, inject, model } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { QrCodeUploadComponent } from '../../upload/qr-code-upload/qr-code-upload.component';
import { TranslocoPipe } from '@jsverse/transloco';
import { NotizTyp } from '@dvb/data-access-backend';
import {ButtonComponent } from '@dvb/ui-components';

@Component({
  standalone: true,
  selector: 'feat-notizen-mobile-upload-modal',
  templateUrl: './notizen-mobile-upload-modal.component.html',
  styleUrl: './notizen-mobile-upload-modal.component.scss',
  imports: [ QrCodeUploadComponent, TranslocoPipe, ButtonComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NotizenMobileUploadModalComponent {
  activeModal = inject(NgbActiveModal);

  notizTyp = model<NotizTyp>(NotizTyp.DEFAULT);
  qrCodeData = model<string>('');

  close(): void {
    this.activeModal.dismiss('cancel');
  }
}
