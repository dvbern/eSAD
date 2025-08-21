import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, inject} from '@angular/core';
import {ISODateTimeString} from '@dvb/data-access-types';
import {Path} from '@dvb/feat-shared';
import {EmptyTableMessageComponent, ListComponent, TableComponent, TableLinkComponent} from '@dvb/ui-components';
import {formatTimeWithDateIfNotToday} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';
import {DashboardStore} from '../dashboard.store';

@Component({
    changeDetection: ChangeDetectionStrategy.OnPush,
    selector: 'feat-dashboard-letze-kinder',
    imports: [
        CommonModule,
        ListComponent,
        TranslocoPipe,
        TableComponent,
        TableLinkComponent,
        EmptyTableMessageComponent,

    ],
    templateUrl: './letzte-kinder.component.html',
})
export class LetzteKinderComponent {
    readonly dashboardStore = inject(DashboardStore);


    kinderList = computed(() => this.dashboardStore.kinderAktivitaetHistory());

    protected readonly Path = Path;

    getVisitDatum(visitTimestamp: ISODateTimeString): string {
      return formatTimeWithDateIfNotToday(visitTimestamp, true) ?? '';
    }
}

