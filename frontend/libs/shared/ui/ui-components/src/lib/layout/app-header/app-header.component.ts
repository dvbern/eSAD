import {ChangeDetectionStrategy, Component} from '@angular/core';

@Component({
    selector: 'svui-app-header',
    templateUrl: './app-header.component.html',
    styleUrl: './app-header.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: []
})
export class AppHeaderComponent {
}
