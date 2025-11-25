
import {ChangeDetectionStrategy, Component, computed, inject} from '@angular/core';
import {AnlassSaveJson} from '@dvb/data-access-backend';
import {ISODateTimeString} from '@dvb/data-access-types';
import {anlassTitle} from '@dvb/feat-anlass';
import {Path} from '@dvb/feat-shared';
import {EmptyTableMessageComponent, ListComponent, TableComponent, TableLinkComponent} from '@dvb/ui-components';
import {formatTimeWithDateIfNotToday, isNotNullish} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {DashboardStore} from '../dashboard.store';

@Component({
    changeDetection: ChangeDetectionStrategy.OnPush,
    selector: 'feat-dashboard-letze-anlaesse',
    imports: [
    EmptyTableMessageComponent,
    ListComponent,
    TableComponent,
    TableLinkComponent,
    TranslocoPipe
],
    templateUrl: './letzte-anlaesse.component.html',
})
export class LetzteAnlaesseComponent {
    readonly dashboardStore = inject(DashboardStore);

    readonly transloco = inject(TranslocoService);

    anlassList = computed(() => this.dashboardStore.anlassAktivitaetHistory());

    protected readonly Path = Path;

    anlassTitle(anlass: AnlassSaveJson | undefined): string {
        if (isNotNullish(anlass)) {
            return anlassTitle(anlass, this.transloco);
        }
        return '';
    }

    getVisitDatum(visitTimestamp: ISODateTimeString): string {
      return formatTimeWithDateIfNotToday(visitTimestamp, true) ?? '';
    }
}

