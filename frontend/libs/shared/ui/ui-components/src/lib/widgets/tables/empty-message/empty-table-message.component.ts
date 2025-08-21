import {ChangeDetectionStrategy, Component, input} from '@angular/core';

@Component({
    // eslint-disable-next-line @angular-eslint/component-selector
    selector: 'tr[svui-empty-table-message]',
    templateUrl: './empty-table-message.component.html',
    styleUrl: './empty-table-message.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: []
})
export class EmptyTableMessageComponent {
  readonly message = input<string>();

}
