import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, inject, Input} from '@angular/core';
import {BaseForm} from '@dvb/util-angular';
import {NgbActiveModal, NgbModalModule} from '@ng-bootstrap/ng-bootstrap';
import {EditDialogContentComponent} from './edit-dialog-content.component';
import {EditDialogConfig} from './edit-dialog.service';

@Component({
    selector: 'feat-dialogs-edit',
    templateUrl: './edit-dialog.component.html',
    imports: [
        CommonModule,
        NgbModalModule,
        EditDialogContentComponent,
    ],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class EditDialogComponent<F extends BaseForm<any, any>> {

  @Input() public config!: EditDialogConfig<F>;

  activeModal = inject(NgbActiveModal);

  dismiss(): void {
    this.activeModal?.dismiss();
  }

  save(formGroup: F): void {
    this.activeModal?.close(formGroup);
  }

}
