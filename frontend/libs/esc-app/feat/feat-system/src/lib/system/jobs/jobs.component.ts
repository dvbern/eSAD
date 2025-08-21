import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {SystemAdminStore} from '@dvb/feat-shared';
import {ButtonComponent, HeadingDirective} from '@dvb/ui-components';

@Component({
  selector: 'feat-jobs',
  imports: [CommonModule, HeadingDirective, ButtonComponent],
  templateUrl: './jobs.component.html',
  styles: ':host { display:block;}',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class JobsComponent {

  readonly #systemAdminStore = inject(SystemAdminStore);

  public runBenutzerInaktivieren(): void {
    this.#systemAdminStore.triggerBenutzerInaktivierenJob();
  }

  public runDocumentQueue(): void {
    this.#systemAdminStore.triggerDocumentQueueJob();
  }

  public runDocumentQueueCleanUp(): void {
    this.#systemAdminStore.triggerDocumentQueueCleanUpJob();
  }

  public runKinderInaktivieren(): void {
    this.#systemAdminStore.triggerKinderInaktivierenJob();
  }
  public runReloadDuplikate(): void {
    this.#systemAdminStore.triggerReloadDuplikate();
  }
}
