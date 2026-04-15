import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { DeploymentIndicatorComponent, GlobalToastsComponent } from '@dvb/feat-shared';

@Component({
  imports: [RouterModule, DeploymentIndicatorComponent, GlobalToastsComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class AppComponent {
  protected title = 'esc-info-app';
}
