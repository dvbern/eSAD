import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component} from '@angular/core';
import {NgbModalModule} from '@ng-bootstrap/ng-bootstrap';
import {ConfirmDialogContentComponent} from './confirm-dialog-content.component';
import {ConfirmConfig, ConfirmOutcomeEnum} from './confirm.service';
import {AbstractDialogComponent} from './dialog.component';

@Component({
    selector: 'feat-dialogs-confirm',
    templateUrl: './confirm-dialog.component.html',
    imports: [
        CommonModule,
        NgbModalModule,
        ConfirmDialogContentComponent,
    ],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ConfirmDialogComponent extends AbstractDialogComponent<ConfirmConfig, ConfirmOutcomeEnum> {

    confirm(): void {
        this.close(ConfirmOutcomeEnum.confirmed);
    }

}
