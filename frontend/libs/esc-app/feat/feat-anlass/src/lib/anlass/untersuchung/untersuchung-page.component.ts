import {ChangeDetectionStrategy, Component, computed, inject} from '@angular/core';
import {ActivatedRoute, RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {BackendEntityId} from '@dvb/data-access-types';
import {BenutzerStore, ConfirmService, EventBusStore, PageComponent, Path} from '@dvb/feat-shared';
import {
  ButtonComponent,
  CardComponent,
  CollapsibleMenuComponent,
  IconComponent,
  IconWithTooltipComponent,
} from '@dvb/ui-components';
import {AppNotificationStore} from '@dvb/util-angular';
import {
  hasAerztlicheUntersuchungViewBerechtigung,
  hasGroesseGewichtViewBerechtigung,
  hasHoerenViewBerechtigung,
  hasImpfungenViewBerechtigung,
  hasMassnahmenViewBerechtigung,
  hasSehenViewBerechtigung,
  hasTriageViewBerechtigung,
  hasUntersuchungKindDatenViewBerechtigung,
  isNullish,
  requireValue,
} from '@dvb/util-shared';
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

@Component({
  selector: 'feat-anlass-detail-page',
  templateUrl: './untersuchung-page.component.html',
  styleUrl: './untersuchung-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    RouterOutlet,
    PageComponent,
    TranslocoPipe,
    NgbNav,
    NgbNavItem,
    NgbNavLink,
    RouterLink,
    RouterLinkActive,
    CardComponent,
    IconComponent,
    CollapsibleMenuComponent,
    NgbDropdownItem,
    NgbNavLinkBase,
    IconWithTooltipComponent,
    ButtonComponent,
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

  canViewUntersuchungKindDaten = this.benutzerStore.createHasRequiredRoleSig(
    hasUntersuchungKindDatenViewBerechtigung(),
  );
  canViewUntersuchungImpfung = this.benutzerStore.createHasRequiredRoleSig(hasImpfungenViewBerechtigung());
  canViewUntersuchungSehen = this.benutzerStore.createHasRequiredRoleSig(hasSehenViewBerechtigung());
  canViewUntersuchungHoeren = this.benutzerStore.createHasRequiredRoleSig(hasHoerenViewBerechtigung());
  canViewUntersuchungGroesseGewicht = this.benutzerStore.createHasRequiredRoleSig(hasGroesseGewichtViewBerechtigung());
  canViewUntersuchungTriage = this.benutzerStore.createHasRequiredRoleSig(hasTriageViewBerechtigung());
  canViewUntersuchungAertzlicheUntersuchung = this.benutzerStore.createHasRequiredRoleSig(
    hasAerztlicheUntersuchungViewBerechtigung(),
  );
  canViewUntersuchungMassnahmen = this.benutzerStore.createHasRequiredRoleSig(hasMassnahmenViewBerechtigung());

  pageTitle = computed(() => {
    const currentAnlassData = this.anlassStore.selectedEntity()?.data;
    return currentAnlassData
      ? anlassTitle(currentAnlassData, this.transloco)
      : this.transloco.translate('anlass.detail.page.title.edit');
  });

  isEinladungActive(id: BackendEntityId): boolean {
    return this.untersuchungStore.currentTabId() === id;
  }

  isHoerenTabSaved(): boolean {
    const anlassConfig = this.anlassStore.selectedEntity()?.anlassConfig;
    const tabsSaved = this.untersuchungStore.tabsSaved();
    if (isNullish(tabsSaved) || isNullish(anlassConfig)) {
      return false;
    }
    if (anlassConfig?.showTabHoerenAudiogramm && anlassConfig.showTabHoerenOtoskopie) {
      return tabsSaved?.hoerenAudiogrammTabSaved && tabsSaved?.hoerenOtoskopieTabSaved;
    }
    if (anlassConfig?.showTabHoerenAudiogramm && !anlassConfig.showTabHoerenOtoskopie) {
      return tabsSaved?.hoerenAudiogrammTabSaved;
    }
    if (!anlassConfig?.showTabHoerenAudiogramm && anlassConfig?.showTabHoerenOtoskopie) {
      return tabsSaved?.hoerenOtoskopieTabSaved;
    }
    return false;
  }

  preventNav(event: NgbNavChangeEvent): void {
    // prevent ngbNav to switch tabs on click, we will set the active tab through store
    event.preventDefault();
  }

  addTab(einladungId: BackendEntityId) {
    if (!this.anlassKinderStore.isEinladungMarkedForUntersuchung(einladungId)) {
      this.anlassKinderStore.markEinladungForUntersuchung(einladungId);

      const alreadyOpenTabsIds = this.anlassKinderStore.selectedIdsForUntersuchung().join(',');

      this.untersuchungStore.addTab(einladungId, alreadyOpenTabsIds, this.activatedRoute);
    }
  }

  closeTab(einladungId: BackendEntityId, event: MouseEvent): void {
    event.preventDefault();
    event.stopImmediatePropagation();
    this.closeTabInternal(einladungId);
  }

  private closeTabInternal(einladungId: BackendEntityId): void {
    if (this.anlassKinderStore.isEinladungMarkedForUntersuchung(einladungId)) {
      this.anlassKinderStore.unmarkEinladungForUntersuchung(einladungId);
      const alreadyOpenTabsIds = this.anlassKinderStore.selectedIdsForUntersuchung().join(',');
      this.untersuchungStore.closeTab(einladungId, alreadyOpenTabsIds, this.activatedRoute);
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
        this.eventBusStore.emit('UNTERSUCHUNG_ABSCHLIESSEN_REQUESTED', {einladungId: einladungId});

        this.untersuchungStore.untersuchungAbschliessen({
          anlassId: requireValue(this.anlassStore.selectedEntity()).id,
          einladungId: einladungId,
          onSuccess: () => {
            this.closeTabInternal(einladungId);
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
