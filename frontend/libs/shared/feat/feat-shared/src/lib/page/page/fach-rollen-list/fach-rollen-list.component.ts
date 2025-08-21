import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, input} from '@angular/core';
import {FachRolle} from '@dvb/data-access-backend';
import {TranslocoPipe} from '@jsverse/transloco';

@Component({
  selector: 'feat-page-fach-rollen-list',
  standalone: true,
  imports: [CommonModule, TranslocoPipe],
  templateUrl: './fach-rollen-list.component.html',
  styleUrl: './fach-rollen-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FachRollenListComponent {
  rollen = input.required<FachRolle[]>();
}
