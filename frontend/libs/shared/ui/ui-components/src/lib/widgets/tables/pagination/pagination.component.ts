import {ChangeDetectionStrategy, Component, computed, input, output} from '@angular/core';
import {PageChanged, PaginationDTO} from '@dvb/util-angular';
import {NgbPaginationConfig, NgbPaginationModule} from '@ng-bootstrap/ng-bootstrap';
import {IconComponent} from '../../icons/icon';

@Component({
  selector: 'svui-pagination',
  templateUrl: './pagination.component.html',
  styleUrl: './pagination.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgbPaginationModule, IconComponent],
})
export class PaginationComponent {
  readonly totalRows = input<number>();
  readonly pagination = input<PaginationDTO>();

  pageSize = computed(() => this.pagination()?.pageSize ?? 1);
  pageNr = computed(() => this.pagination()?.pageNr ?? 1);

  readonly pageChanged = output<PageChanged>();

  constructor(config: NgbPaginationConfig) {
    config.boundaryLinks = true;
    config.pageSize = this.pageSize();
    // maximum number of pages to display
    config.maxSize = 10;
  }

  onPageChange(page: number): void {
    const value = this.calculatePageChangedEvent(page);
    this.pageChanged.emit(value);
  }

  private calculatePageChangedEvent(page: number): PageChanged {
    const pageSize = this.pageSize();
    const value: PageChanged = {
      pageNr: page,
      pageSize,
    };

    return value;
  }
}
