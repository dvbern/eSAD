import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {FileTyp} from '@dvb/data-access-backend';
import {DocumentQueueComponent, DocumentQueueStore} from '@dvb/feat-shared';
import {ButtonComponent, CardComponent, NgSelectDirective} from '@dvb/ui-components';
import {requireValue} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';

@Component({
  selector: 'feat-playground-reports',
  standalone: true,
  templateUrl: './playground-document-queue.component.html',
  styles: ':host { display:block;}',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule, CardComponent, DocumentQueueComponent, ButtonComponent, NgOptionComponent,
    NgSelectComponent, NgSelectDirective, TranslocoPipe,
  ],
})
export class PlaygroundDocumentQueueComponent {
  readonly documentQueueStore = inject(DocumentQueueStore);

  readonly documentQueueTypes = Object.values(FileTyp);
  readonly fb = inject(NonNullableFormBuilder);
  readonly formControl = this.fb.control<FileTyp | undefined>(undefined, Validators.required);

  triggerBatchJob() {
    this.documentQueueStore.forceTriggerBatchJob();
  }

  documentGeneriere(): void {
    if (this.formControl.valid) {
      this.documentQueueStore.createDocumentQueue({
        type: requireValue(this.formControl.value),
      });
    }
  }

  reload(): void {
    this.documentQueueStore.loadDocumentQueueListForCurrentBenutzer();
  }
}
