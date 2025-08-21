import {ChangeDetectionStrategy, Component} from '@angular/core';

@Component({
    selector: 'svui-card-in-card',
    standalone: true,
    templateUrl: './card-in-card.component.html',
    styles: ':host { display:block;}',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CardInCardComponent {
}
