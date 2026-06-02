import {ChangeDetectionStrategy, Component, computed, inject} from '@angular/core';
import {ActivatedRoute, RouterLink, RouterLinkActive} from '@angular/router';
import {BackendEntityId, toEinladungBackendEntityIdWithClass,} from '@dvb/data-access-types';
import {BenutzerStore, ConfirmService, EventBusStore, PageComponent, Path} from '@dvb/feat-shared';
import {CardComponent, CollapsibleMenuComponent,} from '@dvb/ui-components';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {
  NgbDropdownItem,
  NgbNav,
  NgbNavChangeEvent,
  NgbNavItem,
  NgbNavLink,
  NgbNavLinkBase,
} from '@ng-bootstrap/ng-bootstrap';
import {AnlassStore} from '../anlass.store';
import {anlassTitle} from '../anlass.util';
import {AnlassKinderStore} from '../anlasse-detail/anlass-kinder.store';
import {UntersuchungStore} from './untersuchung.store';
import {UntersuchungTabsComponent} from "./untersuchung-tabs/untersuchung-tabs.component";
import {hasUntersuchungKindDatenViewBerechtigung, requireValue} from "@dvb/util-shared";
import {AppNotificationStore} from '@dvb/util-angular';

@Component({
  selector: 'feat-anlass-detail-page',
  templateUrl: './untersuchung-page.component.html',
  styleUrl: './untersuchung-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    PageComponent,
    TranslocoPipe,
    NgbNav,
    NgbNavItem,
    NgbNavLink,
    RouterLink,
    RouterLinkActive,
    CollapsibleMenuComponent,
    NgbDropdownItem,
    NgbNavLinkBase,
    UntersuchungTabsComponent,
    CardComponent,
  ],
})
export class UntersuchungPageComponent {
  readonly anlassStore = inject(AnlassStore);
  readonly anlassKinderStore = inject(AnlassKinderStore);
  readonly untersuchungStore = inject(UntersuchungStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly transloco = inject(TranslocoService);
  readonly eventBusStore = inject(EventBusStore);
  protected readonly Path = Path;
  readonly activatedRoute = inject(ActivatedRoute);
  readonly #appNotificationStore = inject(AppNotificationStore);
  readonly #transloco = inject(TranslocoService);
  readonly confirmService = inject(ConfirmService);

  canViewUntersuchungKindDaten = this.benutzerStore.createHasRequiredRoleSig(hasUntersuchungKindDatenViewBerechtigung());

  pageTitle = computed(() => {
    const currentAnlassData = this.anlassStore.selectedEntity()?.data;
    return currentAnlassData
      ? anlassTitle(currentAnlassData, this.transloco)
      : this.transloco.translate('anlass.detail.page.title.edit');
  });

  isEinladungActive(id: BackendEntityId): boolean {
    return this.untersuchungStore.currentTabId() === id;
  }

  preventNav(event: NgbNavChangeEvent): void {
    // prevent ngbNav to switch tabs on click, we will set the active tab through store
    event.preventDefault();
  }

  async addTab(einladungId: BackendEntityId) {
    if (!this.anlassKinderStore.isEinladungMarkedForUntersuchung(einladungId)) {
      const alreadyOpenTabsIds = [...this.anlassKinderStore.selectedIdsForUntersuchung(), einladungId].join(',');

      const success = await this.untersuchungStore.addTab(einladungId, alreadyOpenTabsIds, this.activatedRoute);
      if (success) {
        this.anlassKinderStore.markEinladungForUntersuchung(einladungId);
      }
    }
  }

  closeTab(einladungId: BackendEntityId, event: MouseEvent): void {
    event.preventDefault();
    event.stopImmediatePropagation();
    void this.closeTabInternal(einladungId);
  }

  private async closeTabInternal(einladungId: BackendEntityId): Promise<void> {
    if (this.anlassKinderStore.isEinladungMarkedForUntersuchung(einladungId)) {
      const remainingIds = this.anlassKinderStore.selectedIdsForUntersuchung().filter(id => id !== einladungId);
      const alreadyOpenTabsIds = remainingIds.join(',');

      const success = await this.untersuchungStore.closeTab(einladungId, alreadyOpenTabsIds, this.activatedRoute);
      if (success) {
        this.anlassKinderStore.unmarkEinladungForUntersuchung(einladungId);
      }
    }
  }

  untersuchungAbschliessen(): void {
    const kindname = this.untersuchungStore.currentTab()?.label;
    const einladungId = requireValue(this.untersuchungStore.currentTabId());

    this.confirmService.confirm({
      question: this.transloco.translate('untersuchung.tab.abschliessen.question'),
      consequence: this.transloco.translate('untersuchung.tab.abschliessen.consequence'),
      decision: this.transloco.translate('untersuchung.tab.abschliessen.decision'),
    }).subscribe(() => {

        // Emit event to validate forms in all components
        this.eventBusStore.emit('UNTERSUCHUNG_ABSCHLIESSEN_REQUESTED', {einladungId: toEinladungBackendEntityIdWithClass(einladungId)});

        this.untersuchungStore.untersuchungAbschliessen({
          anlassId: requireValue(this.anlassStore.selectedEntity()).id,
          einladungId: toEinladungBackendEntityIdWithClass(einladungId),
          onSuccess: () => {
            void this.closeTabInternal(einladungId);
            this.#appNotificationStore.addSuccessNotification(
              this.#transloco.translate('untersuchung.tab.abschliessen.success', {
                kindname: kindname,
              }),
            );
          },
        });
      },
    );
  }
}
