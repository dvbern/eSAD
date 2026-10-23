import {ChangeDetectionStrategy, Component, computed, inject, ViewChild} from '@angular/core';
import {BenutzerStore} from '@dvb/feat-shared';
import {CardInCardComponent, IconComponent} from '@dvb/ui-components';
import {DirtyMarker} from '@dvb/util-angular';
import {hasHoerenEditBerechtigung, isNotNullish} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';
import {AnlassStore} from '../../anlass.store';
import {UntersuchungHoerenAudiogrammStore} from './untersuchung-hoeren-audiogramm/untersuchung-hoeren-audiogramm.store';
import {UntersuchungHoerenOtoskopieStore} from './untersuchung-hoeren-otoskopie/untersuchung-hoeren-otoskopie.store';
import {
  UntersuchungHoerenAudiogrammComponent,
} from './untersuchung-hoeren-audiogramm/untersuchung-hoeren-audiogramm.component';
import {
  UntersuchungHoerenOtoskopieComponent,
} from './untersuchung-hoeren-otoskopie/untersuchung-hoeren-otoskopie.component';

@Component({
  selector: 'feat-untersuchung-hoeren',
  imports: [
    UntersuchungHoerenAudiogrammComponent,
    UntersuchungHoerenOtoskopieComponent,
    CardInCardComponent,
    IconComponent,
    TranslocoPipe,
  ],
  templateUrl: './untersuchung-hoeren.component.html',
  styleUrl: './untersuchung-hoeren.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UntersuchungHoerenComponent implements DirtyMarker {
  @ViewChild(UntersuchungHoerenAudiogrammComponent) untersuchungHoerenAudiogrammComponent?: UntersuchungHoerenAudiogrammComponent;
  @ViewChild(UntersuchungHoerenOtoskopieComponent) untersuchungHoerenOtoskopieComponent?: UntersuchungHoerenOtoskopieComponent;

  readonly anlassStore = inject(AnlassStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly audiogrammStore = inject(UntersuchungHoerenAudiogrammStore);
  readonly otoskopieStore = inject(UntersuchungHoerenOtoskopieStore);

  anlassConfig = computed(() => {
    if (this.anlassStore.selectedEntity() !== undefined) {
      return this.anlassStore.selectedEntity()?.anlassConfig;
    }
    return undefined;
  });

  canEdit = computed(() => this.benutzerStore.createHasRequiredRoleSig(hasHoerenEditBerechtigung())()
    && this.anlassStore.canUserEditAnlassInCurrentState());

  /**
   * Whether the defaults of every form this Anlass shows have been fetched - before that, loadDefaults()
   * has nothing to apply and the click would be lost (see defaultsLoaded in withDefaultValues).
   */
  defaultsLoaded = computed(() => {
    const anlassConfig = this.anlassConfig();
    if (!anlassConfig) {
      return false;
    }
    return (!anlassConfig.showTabHoerenAudiogramm || isNotNullish(this.audiogrammStore.defaultHoerenAudiogrammInitial()))
      && (!anlassConfig.showTabHoerenOtoskopie || isNotNullish(this.otoskopieStore.defaultHoerenOtoskopieInitial()));
  });

  loadDefaults(): void {
    if (this.anlassConfig()?.showTabHoerenAudiogramm) {
      this.untersuchungHoerenAudiogrammComponent?.loadDefault();
    }
    if (this.anlassConfig()?.showTabHoerenOtoskopie) {
      this.untersuchungHoerenOtoskopieComponent?.loadDefault();
    }
  }

  isDirty(): boolean {
    if (this.anlassConfig()?.showTabHoerenAudiogramm
      && this.anlassConfig()?.showTabHoerenOtoskopie
      && isNotNullish(this.untersuchungHoerenAudiogrammComponent)
      && isNotNullish(this.untersuchungHoerenOtoskopieComponent)) {
      return this.untersuchungHoerenAudiogrammComponent.isDirty()
        || this.untersuchungHoerenOtoskopieComponent.isDirty();
    }
    if (!this.anlassConfig()?.showTabHoerenAudiogramm
      && this.anlassConfig()?.showTabHoerenOtoskopie
      && isNotNullish(this.untersuchungHoerenOtoskopieComponent)) {
      return this.untersuchungHoerenOtoskopieComponent.isDirty();
    }
    if (this.anlassConfig()?.showTabHoerenAudiogramm
      && !this.anlassConfig()?.showTabHoerenOtoskopie
      && isNotNullish(this.untersuchungHoerenAudiogrammComponent)) {
      return this.untersuchungHoerenAudiogrammComponent.isDirty();
    }
    return false;
  }

}
