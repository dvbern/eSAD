import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, EventEmitter, inject, Input, Output} from '@angular/core';
import {ButtonComponent} from '@dvb/ui-components';
import {TranslocoService} from '@jsverse/transloco';
import {NgbModalModule} from '@ng-bootstrap/ng-bootstrap';
import {ConfirmConfig} from './confirm.service';

@Component({
    selector: 'feat-dialogs-confirm-content',
    templateUrl: './confirm-dialog-content.component.html',
    imports: [
        CommonModule,
        NgbModalModule,
        ButtonComponent,
    ],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ConfirmDialogContentComponent {
    @Input({required: true}) public config!: ConfirmConfig;

    @Output() dismiss = new EventEmitter();
    @Output() confirm = new EventEmitter();

    translateService = inject(TranslocoService);

    get buttonConfirmText(): string {
        return this.config.decision;
    }

    get buttonCancelText(): string {
        return this.config.wayOut || this.translateService.translate('global.confirm.wayOut');
    }

}
