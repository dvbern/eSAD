import {ChangeDetectionStrategy, Component, computed, input, output} from '@angular/core';
import {PageChanged, PaginationDTO} from '@dvb/util-angular';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgbPaginationModule} from '@ng-bootstrap/ng-bootstrap';
import {ButtonComponent} from '../../buttons';
import {IconWithTooltipComponent} from '../../icons';
import {VirtualScrollAutoTriggerComponent} from './trigger/virtual-scroll-auto-trigger.component';

@Component({
  selector: 'svui-virtual-scroll-trigger',
  templateUrl: './virtual-scroll-trigger.component.html',
  styleUrl: './virtual-scroll-trigger.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgbPaginationModule, ButtonComponent, TranslocoPipe, VirtualScrollAutoTriggerComponent,
    IconWithTooltipComponent],
})
export class VirtualScrollTriggerComponent {
  readonly totalRows = input<number>(0);
  readonly loadedRowCount = input.required<number>();
  readonly pagination = input.required<PaginationDTO>();
  readonly lastLoadedPage = input.required<PaginationDTO | undefined>();
  readonly hasMorePages = computed(() => this.totalRows() > this.loadedRowCount());
  readonly lastLoadedPageWasEmpty = computed(() => {
    const lastLoadedPage: PaginationDTO | undefined = this.lastLoadedPage();
    return lastLoadedPage && lastLoadedPage.pageSize < 1;
  });
  readonly pageChanged = output<PageChanged>();
  readonly loading = input.required<boolean>();

  onVirtualScrollButton(): void {
    this.trigger();
  }

  trigger(): void {
    const nextPage = this.determineNextPage();
    console.log('infinite scroll: triggering page ' + nextPage.pageNr);
    this.pageChanged.emit(nextPage);
  }

  private determineNextPage(): PaginationDTO {
    // find out the last successfully loaded page or use the initial pagination
    const lastLoadedPage = this.lastLoadedPage()
      ? this.lastLoadedPage()?.pageNr || 0
      : this.pagination().pageNr;
    const pageToLoad = lastLoadedPage + 1;
    return {
      pageNr: pageToLoad,
      pageSize: this.pagination().pageSize,
    };
  }
}
