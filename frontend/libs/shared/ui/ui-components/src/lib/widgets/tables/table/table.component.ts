import {
  ChangeDetectionStrategy,
  Component,
  ComponentRef,
  effect,
  ElementRef,
  HostBinding,
  inject,
  input,
  OnInit,
  output,
  ViewContainerRef,
} from '@angular/core';
import { PageChanged, PaginationDTO, TableModelConfig } from '@dvb/util-angular';
import { requireValue } from '@dvb/util-shared';
import { TableWrapperComponent } from '../table-wrapper/table-wrapper.component';

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'table[svuiTable]',
  standalone: true,
  templateUrl: './table.component.html',
  styleUrl: './table.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TableComponent implements OnInit {
  @HostBinding('class.table') readonly bootstrapStyles = true;

  readonly tableConfig = input<TableModelConfig | undefined>();
  readonly downloadPossible = input<boolean>(false);

  readonly totalRows = input<number>(); // filtered length (unpaginated)
  readonly loadedRowCount = input<number>();
  readonly lastLoadedPage = input<PaginationDTO | undefined>();
  readonly pagination = input<PaginationDTO | undefined>();

  readonly loading = input.required<boolean>();

  readonly #elementRef = inject(ElementRef);
  readonly #viewContainerRef = inject(ViewContainerRef);

  #wrapperComponent?: ComponentRef<TableWrapperComponent>;

  readonly pageChanged = output<PageChanged>();
  readonly downloadContent = output();

  constructor() {
    // pass on our input signals
    effect(() => {
      const totalsRows = this.totalRows() ?? 0;
      const pagination = this.pagination();
      const downloadPossible = this.downloadPossible();
      const loading = this.loading();
      const tableConfig = this.tableConfig();
      const loadedRowCount = this.loadedRowCount() ?? 0;
      const lastLoadedPage = this.lastLoadedPage() ?? 0;

      // check required inputs
      if (tableConfig) {
        if (tableConfig.pageOrScrollOrNothing !== undefined) {
          requireValue(
            this.pagination,
            'missing pagination in table: for pagination or scroll, we need a pagination input',
          );
          requireValue(
            this.totalRows,
            'missing totalRows in table: for pagination or scroll, we need to know the total (filtered) size',
          );
        }
        if (tableConfig.frontendOrBackend === 'backend') {
          if (tableConfig.pageOrScrollOrNothing === 'scroll') {
            requireValue(
              this.loadedRowCount,
              'missing loadedRowCount in table: for backend infinite scroll, we need to know if there is more (totalRows and loadedRowCount)',
            );
            requireValue(
              this.lastLoadedPage,
              'missing lastLoadedPage in table: for backend infinite scroll, we need to know the last loaded page',
            );
          }
        }
      }

      // pass on input signals
      if (this.#wrapperComponent) {
        this.#wrapperComponent.setInput('totalRows' satisfies keyof TableWrapperComponent, totalsRows);
        this.#wrapperComponent.setInput('pagination' satisfies keyof TableWrapperComponent, pagination);
        this.#wrapperComponent.setInput('tableConfig' satisfies keyof TableWrapperComponent, tableConfig);
        this.#wrapperComponent.setInput('loadedRowCount' satisfies keyof TableWrapperComponent, loadedRowCount);
        this.#wrapperComponent.setInput('lastLoadedPage' satisfies keyof TableWrapperComponent, lastLoadedPage);
        this.#wrapperComponent.setInput('downloadPossible' satisfies keyof TableWrapperComponent, downloadPossible);
        this.#wrapperComponent.setInput('loading' satisfies keyof TableWrapperComponent, loading);
      }
    });
  }

  ngOnInit(): void {
    this.addTableWrapper();
  }

  private addTableWrapper(): void {
    const wrapperComponent = this.#viewContainerRef.createComponent(TableWrapperComponent, {
      projectableNodes: [[this.#elementRef.nativeElement]],
    });
    // do not pass the inputs here directly because it does not work properly -> use effect
    wrapperComponent.instance.pageChanged = this.pageChanged;
    wrapperComponent.instance.downloadContent = this.downloadContent;
    this.#wrapperComponent = wrapperComponent;
  }
}
