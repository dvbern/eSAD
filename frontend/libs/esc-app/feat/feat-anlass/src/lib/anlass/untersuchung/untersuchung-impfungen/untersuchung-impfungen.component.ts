import { ChangeDetectionStrategy, Component, computed, inject, viewChild } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NotizTyp, RequirementSetting, UntersuchungImpfstatusListSaveJson } from '@dvb/data-access-backend';
import { BackendEntityId, toEinladungBackendEntityIdWithClass } from '@dvb/data-access-types';
import { NavigationService } from '@dvb/feat-shared';
import { CardInCardComponent, LayoutTwoColumnsComponent } from '@dvb/ui-components';
import { DetailMode, DirtyMarker } from '@dvb/util-angular';
import { requireValue } from '@dvb/util-shared';
import { TranslocoService } from '@jsverse/transloco';
import { AnlassStore } from '../../anlass.store';
import { AnlassKinderStore } from '../../anlasse-detail/anlass-kinder.store';
import { UntersuchungStore } from '../untersuchung.store';
import { UntersuchungDoppelkontrolleImpfentscheidComponent } from './untersuchung-doppelkontrolle-impfentscheid/untersuchung-doppelkontrolle-impfentscheid.component';
import { UntersuchungImpfstatusComponent } from './untersuchung-impfstatus/untersuchung-impfstatus.component';
import { UntersuchungImpfungenChecklistComponent } from './untersuchung-impfungen-checklist/untersuchung-impfungen-checklist.component';
import { UntersuchungImpfungenHeaderComponent } from './untersuchung-impfungen-header/untersuchung-impfungen-header.component';
import { UntersuchungImpfungenInternListComponent } from './untersuchung-impfungen-intern-list/untersuchung-impfungen-intern-list.component';
import { UntersuchungImpfungenMassnahmenComponent } from './untersuchung-impfungen-massnahmen/untersuchung-impfungen-massnahmen.component';
import { UntersuchungImpfungenStore } from './untersuchung-impfungen.store';
import {
  UntersuchungImpfungenDataMinimalComponent
} from "./untersuchung-impfungen-data-minimal/untersuchung-impfungen-data-minimal.component";

@Component({
  selector: 'feat-untersuchung-impfungen',
  templateUrl: './untersuchung-impfungen.component.html',
  styleUrl: './untersuchung-impfungen.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    UntersuchungImpfstatusComponent,
    UntersuchungImpfungenHeaderComponent,
    CardInCardComponent,
    UntersuchungImpfungenInternListComponent,
    UntersuchungDoppelkontrolleImpfentscheidComponent,
    UntersuchungImpfungenChecklistComponent,
    UntersuchungImpfungenMassnahmenComponent,
    LayoutTwoColumnsComponent,
    UntersuchungImpfungenDataMinimalComponent
  ],
})
export class UntersuchungImpfungenComponent implements DirtyMarker {
  doppelkontrolleComponent = viewChild(UntersuchungDoppelkontrolleImpfentscheidComponent);
  impfstatusComponent = viewChild(UntersuchungImpfstatusComponent);
  impfChecklistComponent = viewChild(UntersuchungImpfungenChecklistComponent);
  impfMassnahmenComponent = viewChild(UntersuchungImpfungenMassnahmenComponent);
  impfungenDataMinimalComponent = viewChild(UntersuchungImpfungenDataMinimalComponent);

  readonly anlassStore = inject(AnlassStore);
  readonly untersuchungStore = inject(UntersuchungStore);
  readonly anlassKinderStore = inject(AnlassKinderStore);
  readonly untersuchungImpfungenStore = inject(UntersuchungImpfungenStore);

  readonly transloco = inject(TranslocoService);
  readonly navigationService = inject(NavigationService);

  protected readonly DetailMode = DetailMode;
  protected readonly RequirementSetting = RequirementSetting;

  einladung = computed(() => {
    const currentEinladungId: BackendEntityId | undefined = this.untersuchungStore.currentTab()?.einladungID;
    if (currentEinladungId) {
      return this.anlassKinderStore.einladungById(currentEinladungId);
    }
    return undefined;
  });

  isDirty(): boolean {
    return (
      this.impfstatusComponent()?.form.isDirty() ||
      this.doppelkontrolleComponent()?.form.isDirty() ||
      this.impfChecklistComponent()?.form.isDirty() ||
      this.impfMassnahmenComponent()?.form.isDirty() ||
      this.impfungenDataMinimalComponent()?.isDirty() ||
      this.untersuchungImpfungenStore.loader.isLoading()
    );
  }

  public impfausweisUnvollstaendigChanged(value: boolean): void {
    this.untersuchungImpfungenStore.changeImpfausweisUnvollstaendig({
      anlassId: requireValue(this.anlassStore.selectedEntity()).id,
      einladungId: requireValue(this.untersuchungStore.getWrappedEinladungId()),
      impfausweisUnvollstaendig: value,
    });
  }

  public impfausweisNichtVorhandenChanged(value: boolean): void {
    this.untersuchungImpfungenStore.changeImpfausweisNichtVorhanden({
      anlassId: requireValue(this.anlassStore.selectedEntity()).id,
      einladungId: requireValue(this.untersuchungStore.getWrappedEinladungId()),
      impfausweisNichtVorhanden: value,
    });
  }

  public impfausweisZweiPlusEinsSchemaChanged(value: boolean): void {
    this.untersuchungImpfungenStore.changeImpfausweisZweiPlusEinsSchema({
      anlassId: requireValue(this.anlassStore.selectedEntity()).id,
      einladungId: requireValue(this.untersuchungStore.getWrappedEinladungId()),
      impfausweisZweiPlusEinsSchema: value,
    });
  }

  public onSaveImpfstatusList(payload: UntersuchungImpfstatusListSaveJson): void {
    const anlassId = requireValue(this.anlassStore.selectedEntity()).id;
    this.untersuchungImpfungenStore.saveUntersuchungImpfstatusList({
      anlassId: anlassId,
      einladungId: requireValue(this.untersuchungStore.getWrappedEinladungId()),
      impfstatusList: payload,
    });
  }

  public gotoImpfausweis(): void {
    this.navigationService.navigateToEinladungNotiz(
      this.anlassStore.selectedEntity()?.id,
      this.untersuchungStore.currentTabId(),
      NotizTyp.IMPFAUSWEIS,
    );
  }
}
