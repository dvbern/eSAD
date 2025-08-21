import {ChangeDetectionStrategy, Component, inject, OnInit} from '@angular/core';
import {NavigationEnd, NavigationStart, Router, RouterModule} from '@angular/router';
import {DeploymentIndicatorComponent, GlobalToastsComponent} from '@dvb/feat-shared';
import {DirtyGuardComponent} from '@dvb/util-angular';
import {filter} from 'rxjs';

@Component({
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        RouterModule,
        DirtyGuardComponent,
        DeploymentIndicatorComponent,
        GlobalToastsComponent,
    ],
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  readonly router = inject(Router);

  public ngOnInit(): void {
    this.router.events
      .pipe(filter(event => event instanceof NavigationStart || event instanceof NavigationEnd))
      .subscribe(e => {
        console.debug('edokid route event:', e);
      })
  }

}
