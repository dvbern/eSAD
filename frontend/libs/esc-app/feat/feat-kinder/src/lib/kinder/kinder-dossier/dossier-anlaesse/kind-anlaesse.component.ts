import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {RouterLink} from '@angular/router';
import {AnlassArt, AnlassSaveJson, AnlassStatus, EinladungStatus} from '@dvb/data-access-backend';
import {BenutzerOptionsStore} from '@dvb/feat-shared';
import {anlassTitleBasic} from '@dvb/feat-anlass';
import {BenutzerStore} from '@dvb/feat-shared';
import {Path, TimetravelStore} from '@dvb/feat-shared';
import {
  BadgeColor,
  BadgeComponent,
  EmptyListMessageComponent,
  FancyListItemComponent,
  IconOnlyButtonComponent,
  ListComponent,
} from '@dvb/ui-components';
import {fromISODateString, hasAnlassEditBerechtigungForNotAbgeschlossen} from '@dvb/util-shared';
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

  canEditAnlaesse = this.benutzerStore.createHasRequiredRoleSig(hasAnlassEditBerechtigungForNotAbgeschlossen());

  protected readonly Path = Path;
  protected readonly fromISODateString = fromISODateString;
  protected readonly anlassTitleBasic = anlassTitleBasic;

  public isAbgeschlossen(status: AnlassStatus): boolean {
    return status == AnlassStatus.ABGESCHLOSSEN;
  }

  public einladungStatusColor(status: EinladungStatus): BadgeColor {
    switch (status) {
      case 'TEILNAHME_BESTAETIGT':
        return 'green';

      case 'TEILNAHME_ABGESAGT':
        return 'red';

      case 'NACHZUEGLER':
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
