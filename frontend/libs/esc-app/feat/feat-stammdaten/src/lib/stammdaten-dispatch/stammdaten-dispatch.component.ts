
import {ChangeDetectionStrategy, Component} from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';

@Component({
  selector: 'feat-stammdaten-dispatch',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, RouterOutlet],
  templateUrl: './stammdaten-dispatch.component.html',
  styles: `
    :host {
      display: block;
    }
  `
})
export class StammdatenDispatchComponent {}
