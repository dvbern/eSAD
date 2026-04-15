import {ChangeDetectionStrategy, Component, computed, EventEmitter, inject, Input, Output, signal} from '@angular/core';
import {ButtonComponent, CheckboxDirective} from '@dvb/ui-components';
import {TranslocoService} from '@jsverse/transloco';
import {NgbModalModule} from '@ng-bootstrap/ng-bootstrap';
import {ConfirmConfig} from './confirm.service';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";

@Component({
  selector: 'feat-dialogs-confirm-content',
  templateUrl: './confirm-dialog-content.component.html',
  imports: [
    NgbModalModule,
    ButtonComponent,
    CheckboxDirective,
    ReactiveFormsModule,
    FormsModule
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ConfirmDialogContentComponent {
  @Input({required: true}) public config!: ConfirmConfig;

  @Output() dismiss = new EventEmitter();
  @Output() confirm = new EventEmitter();

  translateService = inject(TranslocoService);

  checkboxValue = signal(false);

  get buttonConfirmText(): string {
    return this.config.decision;
  }

  allowConfirm = computed(() => !this.config.confirmCheckbox || this.checkboxValue());

  get buttonCancelText(): string {
    return this.config.wayOut || this.translateService.translate('global.confirm.wayOut');
  }

}
