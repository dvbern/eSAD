import {ChangeDetectionStrategy, Component, inject, Input} from '@angular/core';
import {NgbActiveModal, NgbModalModule} from '@ng-bootstrap/ng-bootstrap';

@Component({
    selector: 'feat-dialogs-generic',
    template: '<ng-content></ng-content>',
    imports: [
        NgbModalModule,
    ],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export abstract class AbstractDialogComponent<Config, Outcome> {

    @Input() public config!: Config;

    activeModal = inject(NgbActiveModal);

    dismiss(): void {
        this.activeModal?.dismiss();
    }

    close(outcome: Outcome): void {
        this.activeModal?.close(outcome);
    }

}
