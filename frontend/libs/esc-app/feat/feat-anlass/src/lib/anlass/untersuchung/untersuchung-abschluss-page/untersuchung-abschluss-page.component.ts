import {ChangeDetectionStrategy, Component, computed, inject, ViewChild} from '@angular/core';
import {BenutzerStore} from '@dvb/feat-shared';
import {CardInCardComponent} from '@dvb/ui-components';
import {DirtyMarker} from '@dvb/util-angular';
import {isNotNullish} from '@dvb/util-shared';
import {AnlassStore} from '../../anlass.store';
import {UntersuchungAbschlussComponent} from "./untersuchung-abschluss/untersuchung-abschluss.component";
import {UntersuchungSignaturComponent} from "./untersuchung-signatur/untersuchung-signatur.component";

@Component({
  selector: 'feat-untersuchung-abschluss-page',
  imports: [
    CardInCardComponent,
    UntersuchungAbschlussComponent,
    UntersuchungSignaturComponent,
  ],
  templateUrl: './untersuchung-abschluss-page.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UntersuchungAbschlussPageComponent implements DirtyMarker {

  @ViewChild(UntersuchungAbschlussComponent) untersuchungAbschlussComponent?: UntersuchungAbschlussComponent;

  readonly anlassStore = inject(AnlassStore);
  readonly benutzerStore = inject(BenutzerStore);

  anlassConfig = computed(() => {
    if (this.anlassStore.selectedEntity() !== undefined) {
      return this.anlassStore.selectedEntity()?.anlassConfig;
    }
    return undefined;
  });

  isDirty(): boolean {
    return !!this.anlassConfig()?.showCardAbschlussAbschluss
        && isNotNullish(this.untersuchungAbschlussComponent)
        && this.untersuchungAbschlussComponent.isDirty();
  }
}
