import {NgIf} from '@angular/common';
import {ChangeDetectionStrategy, Component, input} from '@angular/core';

export type SummaryListItem = {
  name: string,
  totalCount: number,
  filteredCount: number
}

@Component({
    selector: 'svui-page-header-list-summary',
    templateUrl: './page-header-list-summary.component.html',
    styleUrl: './page-header-list-summary.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        NgIf,
    ]
})
export class PageHeaderListSummaryComponent {

  items = input.required<SummaryListItem[]>();
}
