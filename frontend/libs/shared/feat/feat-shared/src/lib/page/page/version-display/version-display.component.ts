
import {ChangeDetectionStrategy, Component, input} from '@angular/core';
import {TranslocoPipe} from '@jsverse/transloco';

@Component({
  selector: 'feat-page-version-display',
  standalone: true,
  imports: [TranslocoPipe],
  templateUrl: './version-display.component.html',
  styleUrl: './version-display.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VersionDisplayComponent {
  version = input.required<string>();
}
