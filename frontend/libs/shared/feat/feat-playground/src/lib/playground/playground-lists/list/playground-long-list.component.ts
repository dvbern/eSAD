import {JsonPipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, DestroyRef, effect, inject, input} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {
  CardComponent,
  SortableComponent,
  TableComponent,
  TableFilterComponent,
  TextInputDirective,
} from '@dvb/ui-components';

import {FrontendOrBackendLogic, PageOrScrollMode, TableModel} from '@dvb/util-angular';
import {isNotNullish} from '@dvb/util-shared';
import {PlaygroundListeFilterForm} from './playground-liste-filter.form';
import {MyDummyItem, PlaygroundListsStore} from './playground-liste.store';

interface TableModelVariant {
  storeInstance: PlaygroundListsStore;

  frontendBackend: FrontendOrBackendLogic;
  pageOrScroll: PageOrScrollMode;

  model: TableModel<MyDummyItem>;
  form: PlaygroundListeFilterForm;
}

@Component({
  selector: 'feat-playground-long-list',
  templateUrl: './playground-long-list.component.html',
  styleUrl: './playground-long-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    TableComponent,
    SortableComponent,
    ReactiveFormsModule,
    CardComponent,
    TableFilterComponent,
    TextInputDirective,
    JsonPipe,
  ],
})
export class PlaygroundLongListComponent {

  fb = inject(NonNullableFormBuilder);
  destroyRef = inject(DestroyRef);

  frontendOrBackend = input.required<FrontendOrBackendLogic>();
  pageOrScroll = input.required<PageOrScrollMode>();

  form = inject(NonNullableFormBuilder).group({
    test: inject(NonNullableFormBuilder).control<number>(2, Validators.required),
  });

  // Playground hack: the stores should not interfere, make one per variant
  store: PlaygroundListsStore | undefined;
  itemTable: TableModel<MyDummyItem> | undefined;
  tableFilterForm: PlaygroundListeFilterForm | undefined;

  // playground hack. the TableModel cannot be created after the inputs, we need to prepare them all.
  tableModelVariants: TableModelVariant[] = this.createTableModels();

  constructor() {

    effect(() => {

      const frontendOrBackend = this.frontendOrBackend();
      const pageOrScroll = this.pageOrScroll();

      if (isNotNullish(frontendOrBackend)
        && isNotNullish(pageOrScroll)
      ) {

        const variant = this.tableModelVariants
          .find(each => each.frontendBackend === frontendOrBackend
            && each.pageOrScroll === pageOrScroll);

        if (variant) {

          this.itemTable = variant.model;
          this.tableFilterForm = variant.form;
          this.store = variant.storeInstance;

          // initial load:
          if (frontendOrBackend === 'frontend') {
            variant.storeInstance.loadCompleteList()
          }
        }
      }
    });
  }

  createTableModels(): TableModelVariant[] {

    const models: TableModelVariant[] = [];
    const frontBack: FrontendOrBackendLogic[] = ['frontend', 'backend'];
    const pageOrScroll: PageOrScrollMode[] = ['paginate', 'scroll'];

    for (const backend of frontBack) {
      for (const pagination of pageOrScroll) {

        const store = new PlaygroundListsStore();

        const tableModel = this.createTableModel(backend, pagination, store);

        models.push({
          frontendBackend: backend,
          pageOrScroll: pagination,
          model: tableModel,
          form: new PlaygroundListeFilterForm({
            fb: this.fb,
            destroyRef: this.destroyRef,
            dummyItemTable: tableModel,
          }),
          storeInstance: store,
        });

      }
    }

    console.log('variants', models);

    return models;

  }

  createTableModel(
    frontendOrBackend: FrontendOrBackendLogic,
    paginateOrScroll: PageOrScrollMode,
    store: PlaygroundListsStore,
  ): TableModel<MyDummyItem> {
    const tableModel = new TableModel<MyDummyItem>(
      {
        rawListSignal: store.items,
        backendFilteredSize: store.filteredCount,
        backendLastLoadedPageSignal: store.currentLoadedPage,
        lazyLoadOnPaginateOrScroll: frontendOrBackend === 'backend'
          ? (displayConfig) => (
            store.loadPaginated({
              config: displayConfig,
              replaceList: paginateOrScroll === 'paginate',
            }))
          : undefined,
        lazyLoadOnSortOrFilter: frontendOrBackend === 'backend'
          ? (displayConfig) => store.loadPaginated({config: displayConfig, replaceList: true})
          : undefined,
      },
      {
        frontendOrBackend: frontendOrBackend,
        pageOrScrollOrNothing: paginateOrScroll,
        originalPageSize: 5,
      });

    tableModel.onColSort('ASC', 'name');
    return tableModel;
  }

}

