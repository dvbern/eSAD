import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, EventEmitter, inject, Input, Output} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {ButtonComponent} from '@dvb/ui-components';
import {BaseForm} from '@dvb/util-angular';
import {TranslocoService} from '@jsverse/transloco';
import {NgbModalModule} from '@ng-bootstrap/ng-bootstrap';
import {EditDialogConfig} from './edit-dialog.service';

@Component({
    selector: 'feat-dialogs-edit-dialog-content',
    templateUrl: './edit-dialog-content.component.html',
    imports: [
        CommonModule,
        NgbModalModule,
        ButtonComponent,
        ReactiveFormsModule,
    ],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class EditDialogContentComponent<F extends BaseForm<any, any>> {
  @Input({required: true}) public config!: EditDialogConfig<F>;

  @Output() dismiss = new EventEmitter();
  @Output() save = new EventEmitter<F>();

  translateService = inject(TranslocoService);

  get buttonConfirmText(): string {
    return this.config.save;
  }

  get buttonCancelText(): string {
    return this.config.wayOut || this.translateService.translate('common.confirm.cancel');
  }

  async submitIfValid(): Promise<void> {
    if (await this.config.form.validate()) {
      this.save.emit(this.config.form.valueOnValid());
    }
  }

}
