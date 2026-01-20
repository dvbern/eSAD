import {DatePipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, inject, input} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {DuplikatKindJson} from '@dvb/data-access-backend';
import {BooleanPipe} from '@dvb/feat-shared';
import {FormLayoutComponent, InfoItemComponent} from '@dvb/ui-components';
import {DetailMode} from '@dvb/util-angular';
import {formatTimeWithDateIfNotToday, isNullish} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'feat-duplikat-merge-kind-info',
  templateUrl: './duplikat-merge-kind-info.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FormsModule,
    TranslocoPipe,
    ReactiveFormsModule,
    FormLayoutComponent,
    InfoItemComponent,
    DatePipe,
    BooleanPipe,
  ],
})
export class DuplikatMergeKindInfoComponent {

  readonly activeModal = inject(NgbActiveModal);
  readonly #transloco = inject(TranslocoService);
  protected readonly DetailMode = DetailMode;

  kind = input<DuplikatKindJson>();

  public getExternalLastImportTimestamp(): string {
    const date = this.kind()?.timestampLatestImport;
    const importTyp = this.kind()?.importTyp;
    if (isNullish(date) || isNullish(importTyp)) {
      return '';
    }
    return this.#transloco.translate('ImportTyp.' + importTyp) + ' (' + formatTimeWithDateIfNotToday(date, true) + ')';
  }
}
