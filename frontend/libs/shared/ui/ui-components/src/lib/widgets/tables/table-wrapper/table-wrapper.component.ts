import {ChangeDetectionStrategy, Component, input, output} from '@angular/core';
import {PageChanged, PaginationDTO, TableModelConfig} from '@dvb/util-angular';
import {TranslocoPipe} from '@jsverse/transloco';
import {IconOnlyButtonComponent} from '../../buttons/icon-only-button';
import {SpinnerComponent} from '../../icons/spinner';
import { PaginationComponent} from '../pagination/pagination.component';
import {VirtualScrollTriggerComponent} from '../virtual-scroll/virtual-scroll-trigger.component';

/**
 * scroll big tables horizontally
 */
@Component({
  selector: 'svui-table-wrapper',
  templateUrl: './table-wrapper.component.html',
  styleUrl: './table-wrapper.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    PaginationComponent, IconOnlyButtonComponent, TranslocoPipe, SpinnerComponent, VirtualScrollTriggerComponent,
  ],
})
export class TableWrapperComponent {
  readonly tableConfig = input<TableModelConfig | undefined>();
  readonly downloadPossible = input<boolean>(false);

  readonly totalRows = input<number>(0); // filtered length (unpaginated)
  readonly loadedRowCount = input<number>(0);
  readonly lastLoadedPage = input<PaginationDTO | undefined>();
  readonly pagination = input<PaginationDTO | undefined>();

  readonly loading = input<boolean>(false);

  // the actual signal will be set dynamically!
  // FIXME: there must be a better way!
  pageChanged = output<PageChanged>();
  // the actual signal will be set dynamically!
  // FIXME: there must be a better way!
  downloadContent = output();

  onPageChange($event: PageChanged): void {
    this.pageChanged.emit($event);
  }

  onVirtualScroll($event: PageChanged): void {
    this.pageChanged.emit($event);
  }

  onDownloadClicked(): void {
    if (this.totalRows() > 0) {
      this.downloadContent.emit();
    }
  }
}
