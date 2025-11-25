
import {ChangeDetectionStrategy, Component, computed, inject, ViewChild} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {CardInCardComponent, IconComponent} from '@dvb/ui-components';
import {DirtyMarker} from '@dvb/util-angular';
import {TranslocoPipe} from '@jsverse/transloco';
import {AnlassStore} from '../../anlass.store';
import {UntersuchungArztBemerkungComponent} from './untersuchung-arzt-bemerkung/untersuchung-arzt-bemerkung.component';
import {
  UntersuchungArztBeurteilungComponent,
} from './untersuchung-arzt-beurteilung/untersuchung-arzt-beurteilung.component';
import {UntersuchungArztStore} from './untersuchung-arzt.store';

@Component({
  selector: 'feat-untersuchung-arzt',
  imports: [
    ReactiveFormsModule,
    CardInCardComponent,
    UntersuchungArztBeurteilungComponent,
    UntersuchungArztBemerkungComponent,
    IconComponent,
    TranslocoPipe
],
  templateUrl: './untersuchung-arzt.component.html',
  styleUrl: './untersuchung-arzt.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UntersuchungArztComponent implements DirtyMarker {

  @ViewChild(UntersuchungArztBeurteilungComponent) arztBeurteilungComponent?: UntersuchungArztBeurteilungComponent;
  @ViewChild(UntersuchungArztBemerkungComponent) arztBemerkungComponent?: UntersuchungArztBemerkungComponent;

  readonly anlassStore = inject(AnlassStore);
  readonly store = inject(UntersuchungArztStore);

  anlassConfig = computed(() => {
    if (this.anlassStore.selectedEntity() !== undefined) {
      return this.anlassStore.selectedEntity()?.anlassConfig;
    }
    return undefined;
  });

  isDirty(): boolean {
    return (this.arztBeurteilungComponent?.isDirty() ?? false)
      || (this.arztBemerkungComponent?.isDirty() ?? false);
  }

  loadDefault(): void {
    this.arztBeurteilungComponent?.loadDefault();
  }
}
