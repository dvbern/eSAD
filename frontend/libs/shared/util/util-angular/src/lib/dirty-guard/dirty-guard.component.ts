
import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {DirtyGuardModalComponent} from './dirty-guard-modal.component';
import {DirtyGuardService} from './dirty-guard.service';

@Component({
  selector: 'util-dirty-guard',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [],
  providers: [NgbModal],
  templateUrl: './dirty-guard.component.html',
  styles: `
    :host {
      display: block;
    }
  `,
})
export class DirtyGuardComponent {
  readonly service = inject(DirtyGuardService);
  readonly #ngbModal = inject(NgbModal);

  constructor() {
    this.service.showPopup.pipe(takeUntilDestroyed()).subscribe(showPopup => {
      if (showPopup) {
        this.#ngbModal.open(DirtyGuardModalComponent).closed.subscribe(decision => {
          if (decision) {
            this.service.allowNavigation();
          } else {
            this.service.preventNavigation();
          }
        })
      }
    })
  }
}
