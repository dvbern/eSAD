import { ChangeDetectionStrategy, Component, output } from '@angular/core';
import { IconOnlyButtonComponent } from '@dvb/ui-components';
import { TranslocoPipe } from '@jsverse/transloco';

@Component({
  selector: 'feat-mobile-upload',
  imports: [IconOnlyButtonComponent, TranslocoPipe],
  templateUrl: './mobile-upload.component.html',
  styleUrl: './mobile-upload.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MobileUploadComponent {
  readonly clicked = output<void>();
}
