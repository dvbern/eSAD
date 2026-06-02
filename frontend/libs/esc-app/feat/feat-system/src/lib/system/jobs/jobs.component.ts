import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {SystemAdminStore} from '@dvb/feat-shared';
import {ButtonComponent, HeadingDirective} from '@dvb/ui-components';

@Component({
  selector: 'feat-jobs',
  imports: [HeadingDirective, ButtonComponent],
  templateUrl: './jobs.component.html',
  styles: ':host { display:block;}',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class JobsComponent {

  readonly #systemAdminStore = inject(SystemAdminStore);

  // Divers

  public runBenutzerInaktivieren(): void {
    this.#systemAdminStore.triggerBenutzerInaktivierenJob();
  }

  public runKinderInaktivieren(): void {
    this.#systemAdminStore.triggerKinderInaktivierenJob();
  }

  public runReloadDuplikate(): void {
    this.#systemAdminStore.triggerReloadDuplikate();
  }

  // Impfstoffe

  public runDeactivateImpfstoffChargenJob(): void {
    this.#systemAdminStore.triggerDeactivateImpfstoffChargenJob();
  }

  public runDeleteImpfstoffChargenJob(): void {
    this.#systemAdminStore.triggerDeleteImpfstoffChargenJob();
  }

  // DocumentQueue

  public runDocumentQueue(): void {
    this.#systemAdminStore.triggerDocumentQueueJob();
  }

  public runDocumentQueueCleanUp(): void {
    this.#systemAdminStore.triggerDocumentQueueCleanUpJob();
  }

  // DossierQueue/ ArchivierungsQueue

  public runDossierQueueProcessingJob(): void {
    this.#systemAdminStore.triggerDossierQueueProcessingJob();
  }

  public runDossierQueueCleanupJob(): void {
    this.#systemAdminStore.triggerDossierQueueCleanupJob();
  }

  public runArchivierungJob(): void {
    this.#systemAdminStore.triggerArchivierungJob();
  }

  public runArchivierungDeleteJob(): void {
    this.#systemAdminStore.triggerArchivierungDeleteJob();
  }

  public runArchiveDownloadEmailReminderJob(): void {
    this.#systemAdminStore.triggerArchiveDownloadEmailReminderJob();
  }

  // FileStore health check
  public runFilestoreHealthcheck(): void {
    this.#systemAdminStore.triggerFilestoreHealthcheck();
  }
}
