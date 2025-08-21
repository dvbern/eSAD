import {ChangeDetectionStrategy, Component} from '@angular/core';
import {EinladungenListeComponent} from './einladungen-liste/einladungen-liste.component';
import {KinderLsteComponent} from './kinder-liste/kinder-liste.component';

@Component({
    selector: 'feat-anlass-detail-anlassdaten-kinder',
    templateUrl: './kinder.component.html',
    styleUrl: './kinder.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        EinladungenListeComponent,
        KinderLsteComponent,
    ]
})
export class KinderComponent {
}
