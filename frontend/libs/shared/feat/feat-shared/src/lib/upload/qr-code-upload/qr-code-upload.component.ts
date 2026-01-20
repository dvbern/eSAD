import {ChangeDetectionStrategy, Component, computed, input} from '@angular/core';
import {NotizTyp} from '@dvb/data-access-backend';
import {QRCodeComponent} from 'angularx-qrcode';

@Component({
  selector: 'feat-qr-code-upload',
  standalone: true,
  imports: [QRCodeComponent],
  templateUrl: './qr-code-upload.component.html',
  styleUrls: ['./qr-code-upload.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class QrCodeUploadComponent {
  readonly qrUrl = input<string>('');
  readonly title = input<string>('');
  readonly width = input<number>(275);
  readonly text = input<string>('QR-Code scannen');
  readonly notizTyp = input<NotizTyp | undefined>(NotizTyp.DEFAULT);

  readonly qrData = computed(() => {
    const base = window.location.origin;
    const data = this.qrUrl();
    return `${base}${data}`;
  });
}
