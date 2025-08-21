import {DatePipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, inject, input} from '@angular/core';
import {TranslocoPipe} from '@jsverse/transloco';
import {IconComponent} from '../../icons/icon/icon.component';
import {InfoItemComponent} from '../info-items/info-item/info-item.component';

@Component({
  selector: 'svui-anlass-summary-layout',
  templateUrl: './anlass-summary-layout.component.html',
  styleUrl: './anlass-summary-layout.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [DatePipe],
  imports: [IconComponent, InfoItemComponent, TranslocoPipe, DatePipe],
})
export class AnlassSummaryLayoutComponent {
  readonly arzt = input<string | undefined>('');
  readonly mpa = input<string | undefined>('');
  readonly datum = input<Date | undefined>();
  readonly uhrzeitVon = input<string | undefined>('');
  readonly uhrzeitBis = input<string | undefined>('');
  readonly datePipe = inject(DatePipe);

  uhrzeit = computed(() => {
    if (this.uhrzeitVon() && this.uhrzeitBis()) {
      return (
        this.datePipe.transform(this.uhrzeitVon(), 'HH:mm') +
        ' - ' +
        this.datePipe.transform(this.uhrzeitBis(), 'HH:mm')
      );
    }
    return undefined;
  });
}
