import {DatePipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, input} from '@angular/core';
import {TranslocoPipe} from '@jsverse/transloco';

export type EntityInfo = {
  timestampErstellt: string;
  timestampMutiert: string;
  userErstellt: string;
  userMutiert: string;
}

@Component({
    selector: 'svui-page-header-entity-info',
    templateUrl: './page-header-entity-info.component.html',
    styleUrl: './page-header-entity-info.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        TranslocoPipe,
        DatePipe,
    ]
})
export class PageHeaderEntityInfoComponent {
  entityInfo = input.required<EntityInfo | undefined>();
}
