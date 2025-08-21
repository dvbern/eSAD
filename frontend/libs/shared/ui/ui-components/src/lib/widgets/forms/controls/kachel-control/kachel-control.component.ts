import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ControlWithTypeDirective } from '../control-with-type.directive';

@Component({
  selector: 'svui-kachel-control',
  standalone: true,
  templateUrl: './kachel-control.component.html',
  styleUrl: './kachel-control.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class KachelControlComponent extends ControlWithTypeDirective {}
