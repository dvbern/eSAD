import {ChangeDetectionStrategy, Component, input} from '@angular/core';

@Component({
    selector: 'svui-empty-list-message',
    templateUrl: './empty-list-message.component.html',
    styleUrl: './empty-list-message.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: []
})
export class EmptyListMessageComponent {
  readonly message = input<string>();

}
