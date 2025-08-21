import {inject, Injectable, TemplateRef} from '@angular/core';
import {BaseForm} from '@dvb/util-angular';
import {NgbModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import {Observable} from 'rxjs';
import {EditDialogComponent} from './edit-dialog.component';

export interface EditDialogOutcome<F extends BaseForm<any, any>> {
  editedForm$: Observable<F>,
  dismissed$: Observable<unknown>,
  /**
   * Close the dialog with the specified outcome.
   */
  close: (outcome: F) => void,
}

export interface EditDialogConfig<F extends BaseForm<any, any>> {
  heading: string;
  content: TemplateRef<any>;
  form: F;
  wayOut: string;
  save: string;
  size: 'sm' | 'lg' | 'xl' | string | undefined;
}



@Injectable({providedIn: 'root'})
export class EditDialogService {
  readonly modalService = inject(NgbModal);

  public edit<F extends BaseForm<any, any>>(config: EditDialogConfig<F>): Observable<EditDialogOutcome<F>> {

    const modalRef: NgbModalRef = this.modalService.open(EditDialogComponent, {
      centered: true,
      size: config.size,
      backdrop: true,
      keyboard: true,
    });

    const component = modalRef.componentInstance;
    component.config = config;
    return modalRef.closed;
  }

}
