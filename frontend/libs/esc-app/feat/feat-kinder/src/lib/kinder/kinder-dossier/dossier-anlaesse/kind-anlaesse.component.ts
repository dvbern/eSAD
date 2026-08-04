import {ChangeDetectionStrategy, Component, computed, inject} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {RouterLink} from '@angular/router';
import {AnlassArt, AnlassSaveJson, AnlassStatus, EinladungStatus} from '@dvb/data-access-backend';
import {BenutzerOptionsStore, BenutzerStore, Path, TimetravelStore} from '@dvb/feat-shared';
import {
  BadgeColor,
  BadgeComponent,
  EmptyListMessageComponent,
  FancyListItemComponent,
  IconOnlyButtonComponent,
  ListComponent,
} from '@dvb/ui-components';
import {fromISODateString, hasAnlassCreateOrEditForNotAbgeschlossenBerechtigung} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {DossierStore} from '../../dossier.store';
import {KindAnlaesseStore} from './kind-anlaesse-store.service';

@Component({
  selector: 'feat-kind-dossier-anlaesse-list',
  templateUrl: './kind-anlaesse.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    TranslocoPipe,
    FormsModule,
    ListComponent,
    BadgeComponent,
    FancyListItemComponent,
    EmptyListMessageComponent,
    IconOnlyButtonComponent,
    RouterLink,
  ],
})
export class KindAnlaesseComponent {

  readonly dossierStore = inject(DossierStore);
  readonly benutzerOptionsStore = inject(BenutzerOptionsStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly kindAnlaesseStore = inject(KindAnlaesseStore);
  readonly transloco = inject(TranslocoService);
  readonly timetravelStore = inject(TimetravelStore);

  canEditAnlaesse = computed(() =>
    this.benutzerStore.createHasRequiredRoleSig(hasAnlassCreateOrEditForNotAbgeschlossenBerechtigung())() &&
    this.dossierStore.isKindAktiv()
  );

  protected readonly Path = Path;
  protected readonly fromISODateString = fromISODateString;

  public isAbgeschlossen(status: AnlassStatus): boolean {
    return status == AnlassStatus.ABGESCHLOSSEN;
  }

  public einladungStatusColor(status: EinladungStatus): BadgeColor {
    switch (status) {
      case 'TEILNAHME_BESTAETIGT':
      case 'PRIVATARZT_BESTAETIGT':
      case 'PRIVATARZT_MUENDL_BESTAETIGT':
      case 'UNTERSUCHUNG_JA':
        return 'green';

      case 'TEILNAHME_ABGESAGT':
      case 'KEINE_ANMELDUNG':
        return 'red';

      case 'NACHZUEGLER':
      case 'PRIVATARZT_OFFEN':
        return 'orange';

      default:
        // exhaustiveness-check
        status satisfies never;
        return 'transparent';
    }
  }

  getPrefilledAnlass(): string {
    const anlassPartial: Partial<AnlassSaveJson> = {
      anlassArt: AnlassArt.EINZEL_ANLASS,
    };
    return JSON.stringify(anlassPartial);
  }
}
