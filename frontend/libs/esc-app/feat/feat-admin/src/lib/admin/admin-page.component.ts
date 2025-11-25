
import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {RouterLink} from '@angular/router';
import {BenutzerStore} from '@dvb/feat-shared';
import {Path} from '@dvb/feat-shared';
import {PageComponent} from '@dvb/feat-shared';
import {
  ListComponent,
  ListItemComponent,
  ListItemSecondaryDirective,
  ListItemTitleDirective,
} from '@dvb/ui-components';
import {
  hasAerztePageViewBerechtigung,
  hasBenutzerViewBerechtigung,
  hasBriefvorlagenViewBerechtigung,
  hasDuplikateBerechtigung,
  hasImpfstoffePageViewBerechtigung,
  hasLabelPageViewBerechtigung,
  hasListViewBerechtigung,
  hasSystemAdminViewBerechtigung,
} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';

@Component({
  selector: 'feat-admin',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    PageComponent,
    ListComponent,
    ListItemComponent,
    ListItemTitleDirective,
    ListItemSecondaryDirective,
    TranslocoPipe,
    RouterLink
],
  templateUrl: './admin-page.component.html',
  styleUrl: './admin-page.component.scss',
})
export class AdminPageComponent {
  protected readonly Path = Path;

  benutzerStore = inject(BenutzerStore);

  canViewLabel = this.benutzerStore.createHasRequiredRoleSig(hasLabelPageViewBerechtigung());
  canViewList = this.benutzerStore.createHasRequiredRoleSig(hasListViewBerechtigung());
  canViewBenutzer = this.benutzerStore.createHasRequiredRoleSig(hasBenutzerViewBerechtigung());
  canViewSystemAdmin = this.benutzerStore.createHasRequiredRoleSig(hasSystemAdminViewBerechtigung());
  canViewImpfstoffe = this.benutzerStore.createHasRequiredRoleSig(hasImpfstoffePageViewBerechtigung());
  canViewBriefvorlage = this.benutzerStore.createHasRequiredRoleSig(hasBriefvorlagenViewBerechtigung());
  canViewDuplikate = this.benutzerStore.createHasRequiredRoleSig(hasDuplikateBerechtigung());
  canViewAerzte = this.benutzerStore.createHasRequiredRoleSig(hasAerztePageViewBerechtigung());
}
