import {inject, Injectable, Type} from '@angular/core';
import {NgbModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import {catchError, filter, map, merge, Observable} from 'rxjs';
import {ConfirmDialogComponent} from './confirm-dialog.component';
import {AbstractDialogComponent} from './dialog.component';

export interface Outcome<T> {
  closed$: Observable<T>,
  dismissed$: Observable<unknown>,
  /**
   * Close the dialog with the specified outcome.
   */
  close: (outcome: T) => void,
}

export interface ConfirmConfig {
  question: string;
  consequence: string;
  decision: string;
  confirmCheckbox?: string;
  wayOut?: string;
}

export enum ConfirmOutcomeEnum {
  confirmed = 'confirmed',
}

@Injectable({providedIn: 'root'})
export class ConfirmService {
  readonly modalService = inject(NgbModal);

  public confirm(config: ConfirmConfig): Observable<ConfirmOutcomeEnum.confirmed> {
    return this.showDialog(ConfirmDialogComponent, config, undefined)
      .closed$
      .pipe(
        filter(outcome => outcome === ConfirmOutcomeEnum.confirmed),
        catchError(err => {
          console.error('Error in confirm dialog', err); // to make sure we see a log
          throw err; // rehthtrow because we might want to handle certain errors
        }),
      );
  }

  public makeDecision(config: ConfirmConfig): Observable<boolean> {
    const outcomeDialog = this.showDialog(ConfirmDialogComponent, config, undefined);
    return merge(
      outcomeDialog.closed$.pipe(map(result => result === ConfirmOutcomeEnum.confirmed)),
      outcomeDialog.dismissed$.pipe(map(() => false)));
  }

// typescript does not support type parameters that *automatically* infer S correctly in this case (afaik) :(
// eslint-disable-next-line @typescript-eslint/no-explicit-any
  private showDialog<OutcomePayload = never, S = any>(
    componentType: Type<AbstractDialogComponent<S, OutcomePayload>>,
    content: S,
    size: 'sm' | 'lg' | 'xl' | string | undefined,
  ): Outcome<OutcomePayload> {
    const modalRef: NgbModalRef = this.modalService.open(componentType, {
      centered: true,
      size: size,
      backdrop: true,
      keyboard: true,
    });

    const component = modalRef.componentInstance;
    component.config = content;
    // Please do not pass out NgbModalRef directly as it just uses 'any' anywhere instead of useful types :(
    // Add correctly typed properties to Outcome<T> if needed.
    return {
      closed$: modalRef.closed,
      dismissed$: modalRef.dismissed,
      close: outcome => modalRef.close(outcome),
    };
  }
}
