import {ChangeDetectionStrategy, Component} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {
  ButtonComponent,
  HeadingDirective,
  IconComponent,
  IconOnlyButtonComponent,
  IconUseCases,
} from '@dvb/ui-components';
import {TranslocoPipe} from "@jsverse/transloco";

@Component({
  selector: 'feat-playground-icons',
  templateUrl: './playground-icons.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    IconComponent,
    IconOnlyButtonComponent,
    ButtonComponent,
    HeadingDirective,
    TranslocoPipe,
  ]
})
export class PlaygroundIconsComponent {

  useCases = IconUseCases;


  public buttons = ['primary', 'outline-primary', 'secondary', 'link', 'danger', 'success'];

}
