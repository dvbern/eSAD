import {ChangeDetectionStrategy, Component} from '@angular/core';

@Component({
  selector: 'svui-dashboard',
  standalone: true,
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: []
})
export class DashboardComponent {}
