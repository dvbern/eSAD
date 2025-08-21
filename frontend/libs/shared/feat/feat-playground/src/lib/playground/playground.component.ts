import {ChangeDetectionStrategy, Component} from '@angular/core';
import {RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {PageComponent} from '@dvb/feat-shared';
import {InternalLink} from '@dvb/ui-components';

@Component({
    selector: 'feat-playground',
    templateUrl: './playground.component.html',
    styleUrl: './playground.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        PageComponent,
        RouterOutlet,
        RouterLink,
        RouterLinkActive,
    ]
})
export class PlaygroundComponent {
  backLink: InternalLink = {
    label: 'Kinder',
    routerLink: '.',
  };
}
