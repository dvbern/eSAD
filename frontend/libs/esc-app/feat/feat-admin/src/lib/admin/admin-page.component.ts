import {ChangeDetectionStrategy, Component, computed, inject} from '@angular/core';
import {RouterLink} from '@angular/router';
import {BenutzerStore, PageComponent, Path, TenantSettingsStore} from '@dvb/feat-shared';
import {
  ListComponent,
  ListItemComponent,
  ListItemSecondaryDirective,
  ListItemTitleDirective,
} from '@dvb/ui-components';
import {
  hasAerztePageViewBerechtigung,
  hasArchivierungBerechtigung,
  hasBenutzerViewBerechtigung,
  hasBriefvorlagenViewBerechtigung,
  hasDuplikateBerechtigung,
  hasImpfstoffePageViewBerechtigung,
  hasLabelPageViewBerechtigung,
  hasListViewBerechtigung, hasOrganisationVerwaltungViewBerechtigung,
  isNotNullish,
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
  tenantSettingsStore = inject(TenantSettingsStore);

  canViewLabel = this.benutzerStore.createHasRequiredRoleSig(hasLabelPageViewBerechtigung());
  canViewList = this.benutzerStore.createHasRequiredRoleSig(hasListViewBerechtigung());
  canViewBenutzer = this.benutzerStore.createHasRequiredRoleSig(hasBenutzerViewBerechtigung());
  canViewImpfstoffe = this.benutzerStore.createHasRequiredRoleSig(hasImpfstoffePageViewBerechtigung());
  canViewBriefvorlage = this.benutzerStore.createHasRequiredRoleSig(hasBriefvorlagenViewBerechtigung());
  canViewDuplikate = this.benutzerStore.createHasRequiredRoleSig(hasDuplikateBerechtigung());
  canViewArchivierung = computed(() => this.benutzerStore.createHasRequiredRoleSig(hasArchivierungBerechtigung())
    && this.tenantSettingsStore.archivierungEnabled());
  canViewOrganisation = this.benutzerStore.createHasRequiredRoleSig(hasOrganisationVerwaltungViewBerechtigung());

  canViewAerzte = computed(() => {
    return this.benutzerStore.createHasRequiredRoleSig(hasAerztePageViewBerechtigung())
      && this.tenantSettingsStore.arztListeEnabled();
  });

  hasOrganisation = computed(() => isNotNullish(this.benutzerStore.benutzer()?.currentOrganisationRollen));
}
