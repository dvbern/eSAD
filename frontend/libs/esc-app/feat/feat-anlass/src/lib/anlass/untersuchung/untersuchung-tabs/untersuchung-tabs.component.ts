import {ChangeDetectionStrategy, Component, inject, input, output} from '@angular/core';
import {ActivatedRoute, RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {BenutzerStore, ConfirmService, Path} from '@dvb/feat-shared';
import {ButtonComponent, IconComponent, IconWithTooltipComponent,} from '@dvb/ui-components';
import {
  hasAerztlicheUntersuchungViewBerechtigung,
  hasGroesseGewichtViewBerechtigung,
  hasHoerenViewBerechtigung,
  hasImpfungenViewBerechtigung,
  hasLaeusekontrolleViewBerechtigung,
  hasMassnahmenViewBerechtigung,
  hasSehenViewBerechtigung,
  hasTriageViewBerechtigung,
  hasUntersuchungAbschlussViewBerechtigung,
  hasUntersuchungVorabklaerungenViewBerechtigung,
  isNullish,
} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NgbNav, NgbNavItem} from '@ng-bootstrap/ng-bootstrap';
import {AnlassStore} from '../../anlass.store';
import {UntersuchungStore} from '../untersuchung.store';

@Component({
  selector: 'feat-untersuchung-tabs',
  standalone: true,
  templateUrl: './untersuchung-tabs.component.html',
  styleUrl: './untersuchung-tabs.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    RouterOutlet,
    TranslocoPipe,
    NgbNav,
    NgbNavItem,
    RouterLink,
    RouterLinkActive,
    IconComponent,
    IconWithTooltipComponent,
    ButtonComponent,
  ],
})
export class UntersuchungTabsComponent {
  showLinkToDossier = input(false);
  canViewUntersuchungKindDaten = input(false);
  abschliessenLabelKey = input('untersuchung.tab.abschliessen');

  untersuchungAbschliessen = output();

  readonly anlassStore = inject(AnlassStore);
  readonly untersuchungStore = inject(UntersuchungStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly transloco = inject(TranslocoService);
  protected readonly Path = Path;
  readonly activatedRoute = inject(ActivatedRoute);
  readonly confirmService = inject(ConfirmService);

  canViewUntersuchungVorabklaerungen = this.benutzerStore.createHasRequiredRoleSig(hasUntersuchungVorabklaerungenViewBerechtigung());
  canViewUntersuchungImpfung = this.benutzerStore.createHasRequiredRoleSig(hasImpfungenViewBerechtigung());
  canViewUntersuchungSehen = this.benutzerStore.createHasRequiredRoleSig(hasSehenViewBerechtigung());
  canViewUntersuchungHoeren = this.benutzerStore.createHasRequiredRoleSig(hasHoerenViewBerechtigung());
  canViewUntersuchungGroesseGewicht = this.benutzerStore.createHasRequiredRoleSig(hasGroesseGewichtViewBerechtigung());
  canViewUntersuchungTriage = this.benutzerStore.createHasRequiredRoleSig(hasTriageViewBerechtigung());
  canViewUntersuchungAertzlicheUntersuchung = this.benutzerStore.createHasRequiredRoleSig(hasAerztlicheUntersuchungViewBerechtigung());
  canViewUntersuchungLaeusekontrolle = this.benutzerStore.createHasRequiredRoleSig(hasLaeusekontrolleViewBerechtigung());
  canViewUntersuchungMassnahmen = this.benutzerStore.createHasRequiredRoleSig(hasMassnahmenViewBerechtigung());
  canViewAbschluss = this.benutzerStore.createHasRequiredRoleSig(hasUntersuchungAbschlussViewBerechtigung());

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

  isAbschlussTabSaved(): boolean {
    const anlassConfig = this.anlassStore.selectedEntity()?.anlassConfig;
    const tabsSaved = this.untersuchungStore.tabsSaved();
    if (isNullish(tabsSaved) || isNullish(anlassConfig)) {
      return false;
    }
    if (anlassConfig?.showCardAbschlussAbschluss && anlassConfig.showCardAbschlussSignatur) {
      return tabsSaved?.abschlussTabSaved && tabsSaved?.signaturTabSaved;
    }
    if (anlassConfig?.showCardAbschlussAbschluss && !anlassConfig.showCardAbschlussSignatur) {
      return tabsSaved?.abschlussTabSaved;
    }
    if (!anlassConfig?.showCardAbschlussAbschluss && anlassConfig?.showCardAbschlussSignatur) {
      return tabsSaved?.signaturTabSaved;
    }
    return false;
  }
}
