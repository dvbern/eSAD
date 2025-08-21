import {DatePipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, inject, input, output} from '@angular/core';
import {FormControl, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {KindSearchJson} from '@dvb/data-access-backend';
import {NgSelectDirective} from '@dvb/ui-components';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgLabelTemplateDirective, NgOptionTemplateDirective, NgSelectComponent} from '@ng-select/ng-select';
import {Subject} from 'rxjs';
import {KindSearchStore} from './kind-search.store';

@Component({
  selector: 'feat-kind-search',
  templateUrl: './kind-search.component.html',
  providers: [KindSearchStore],
  imports: [
    NgSelectComponent,
    NgSelectDirective,
    FormsModule,
    TranslocoPipe,
    NgOptionTemplateDirective,
    DatePipe,
    NgLabelTemplateDirective,
    ReactiveFormsModule,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class KindSearchComponent {
  myFormControl = input.required<FormControl<KindSearchJson | null>>();
  styleVariant = input<'default' | 'rounded'>('default');
  clearable = input<boolean>(true);
  placeholder = input.required<string>();

  readonly selected = output<KindSearchJson>();
  kindSearchStore = inject(KindSearchStore);

  working = computed(() => this.kindSearchStore.loader.isLoading());
  searchInput$ = new Subject<string>();

  constructor() {
    this.kindSearchStore.searchKinder(this.searchInput$);
  }

  trackByFn(item: KindSearchJson) {
    return item.id;
  }
}
