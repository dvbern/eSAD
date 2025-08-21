import {DatePipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {EinladungJson} from '@dvb/data-access-backend';
import {
  CardCollapsibleComponent,
  CardComponent,
  ColWidthComponent,
  EmptyTableMessageComponent,
  IconComponent,
  IconOnlyButtonComponent,
  SortableComponent,
  TableComponent,
} from '@dvb/ui-components';
import {TableModel} from '@dvb/util-angular';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {firstValueFrom} from 'rxjs';
import {AnlassKinderStore} from '../../../anlass-kinder.store';
import {ConfirmEinladungRemoveComponent} from '../confirm-einladung-remove/confirm-einladung-remove.component';
import {getUseCaseGechlecht} from '../kinder.util';

@Component({
  selector: 'feat-anlass-bearbeiten-anlassdaten-kinder-einladungen',
  templateUrl: './einladungen-liste.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FormsModule,
    SortableComponent,
    TableComponent,
    TranslocoPipe,
    DatePipe,
    IconComponent,
    ReactiveFormsModule,
    IconOnlyButtonComponent,
    CardCollapsibleComponent,
    CardComponent,
    EmptyTableMessageComponent,
    ColWidthComponent,
  ],
})
export class EinladungenListeComponent {

  readonly store = inject(AnlassKinderStore);
  readonly #ngModal = inject(NgbModal);

  selectedItemTable = new TableModel<EinladungJson>(
    {rawListSignal: this.store.einladungen},
    {frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll'});

  async removeEinladung(kind: EinladungJson): Promise<void> {
    const ngbModalRef = this.#ngModal.open(ConfirmEinladungRemoveComponent, {size: 'm'});
    ngbModalRef.componentInstance.einladungData.set(kind);
    const result = await firstValueFrom(ngbModalRef.closed, {defaultValue: undefined});
    if (result) {
      this.store.removeEinladung(kind);
    }
  }

  protected readonly getUseCaseGechlecht = getUseCaseGechlecht;
}
