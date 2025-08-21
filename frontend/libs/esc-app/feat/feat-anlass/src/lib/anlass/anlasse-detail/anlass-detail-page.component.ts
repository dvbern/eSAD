import {ChangeDetectionStrategy, Component} from '@angular/core';
import {RouterOutlet} from '@angular/router';

@Component({
    selector: 'feat-anlass-detail-page',
    templateUrl: './anlass-detail-page.component.html',
    styleUrl: './anlass-detail-page.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        RouterOutlet,
    ]
})
export class AnlassDetailPageComponent {

}
