import { ChangeDetectionStrategy, Component } from '@angular/core';
import { PageComponent } from '@dvb/feat-shared';
import {TranslocoPipe} from "@jsverse/transloco";
import { RouterModule } from '@angular/router';
import {IconOnlyButtonComponent} from "@dvb/ui-components";

@Component({
  selector: 'feat-publicinfo',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [PageComponent, TranslocoPipe, RouterModule, IconOnlyButtonComponent],
  templateUrl: './publicinfo-page.component.html',
  styleUrl: './publicinfo-page.component.scss',
})
export class PublicinfoPageComponent {}
