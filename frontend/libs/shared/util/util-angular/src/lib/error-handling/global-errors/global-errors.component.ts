import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {AppErrorHandlerService} from '../app-error-handler.service';

@Component({
  selector: 'util-global-errors',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
  templateUrl: './global-errors.component.html',
  styles: `
    :host {
      display: block;
    }
  `
})
export class GlobalErrorsComponent {
  readonly service = inject(AppErrorHandlerService)
  protected readonly Error = Error;

  isError(e: unknown): e is Error {
    return e instanceof Error;
  }
}
