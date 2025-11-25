
import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {ConfirmService, DocumentQueueComponent, DocumentQueueStore, PageComponent} from '@dvb/feat-shared';
import {IconOnlyButtonComponent, PageHeaderButtonsComponent} from '@dvb/ui-components';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {SpssFormComponent} from './spss-form/spss-form.component';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush, selector: 'feat-reports',
  imports: [
    PageComponent,
    FormsModule,
    TranslocoPipe,
    ReactiveFormsModule,
    DocumentQueueComponent,
    SpssFormComponent,
    IconOnlyButtonComponent,
    PageHeaderButtonsComponent
],
  templateUrl: './reports.component.html',
})
export class ReportsComponent {

  readonly #confirmService = inject(ConfirmService);
  readonly #translationService = inject(TranslocoService);
  readonly documentQueueStore = inject(DocumentQueueStore);

  public deleteGeneratedDocsManually(): void {
    this.#confirmService.confirm({
      question: this.#translationService.translate('reports.action.manual.deletion.question', {}),
      consequence: this.#translationService.translate('reports.action.manual.deletion.consequence'),
      decision: this.#translationService.translate('reports.action.manual.deletion.decision'),
    }).subscribe(() => {
      const itemsToRemove = this.documentQueueStore.entities();
      const idsToDelete = itemsToRemove.map(item => item.id);
      this.documentQueueStore.deleteGeneratedDocsManually({
        list: [...idsToDelete],
      });
    });
  }
}
