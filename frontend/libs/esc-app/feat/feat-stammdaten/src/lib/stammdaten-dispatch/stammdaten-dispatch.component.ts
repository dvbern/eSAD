import { CommonModule } from '@angular/common';
import {ChangeDetectionStrategy, Component} from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';

@Component({
  selector: 'feat-stammdaten-dispatch',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, RouterLink, RouterOutlet],
  templateUrl: './stammdaten-dispatch.component.html',
  styles: `
    :host {
      display: block;
    }
  `
})
export class StammdatenDispatchComponent {}
