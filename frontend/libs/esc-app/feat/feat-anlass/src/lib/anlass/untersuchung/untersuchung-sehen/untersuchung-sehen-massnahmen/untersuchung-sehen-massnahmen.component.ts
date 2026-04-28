
import {ChangeDetectionStrategy, Component, DestroyRef, effect, inject} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {InfomaterialJson, InformationEltern, RequirementSetting,} from '@dvb/data-access-backend';
import {InfomaterialStore, TranslatedStringPipe} from '@dvb/feat-shared';
import {
  CheckboxDirective,
  FormControlComponent,
  FormLayoutComponent,
  KachelControlComponent,
  KachelFormSectionComponent,
  NgSelectDirective,
  SaveCancelButtonsComponent,
} from '@dvb/ui-components';
import {toEinladungBackendEntityIdWithClass} from '@dvb/data-access-types';
import {DirtyMarker} from '@dvb/util-angular';
import {compareFn, requireValue} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {AnlassStore} from '../../../anlass.store';
import {UntersuchungStore} from '../../untersuchung.store';
import {UntersuchungSehenStore} from './../untersuchung-sehen.store';
import {UntersuchungSehenMassnahmenForm} from "./untersuchung-sehen-massnahmen.form";

@Component({
  selector: 'feat-untersuchung-sehen-massnahmen',
  imports: [
    ReactiveFormsModule,
    SaveCancelButtonsComponent,
    TranslocoPipe,
    FormLayoutComponent,
    KachelFormSectionComponent,
    KachelControlComponent,
    FormControlComponent,
    CheckboxDirective,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    TranslatedStringPipe
],
  templateUrl: './untersuchung-sehen-massnahmen.component.html',
  styleUrl: './untersuchung-sehen-massnahmen.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UntersuchungSehenMassnahmenComponent implements DirtyMarker {

  protected readonly RequirementSetting = RequirementSetting;

  readonly anlassStore = inject(AnlassStore);
  readonly untersuchungStore = inject(UntersuchungStore);
  readonly store = inject(UntersuchungSehenStore);
  readonly infomaterialStore = inject(InfomaterialStore);

  form = new UntersuchungSehenMassnahmenForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
  });

  informationEltern = Object.values(InformationEltern);
  infomaterialOptions: InfomaterialJson[] | undefined;

  protected readonly compareInfomaterial = compareFn<InfomaterialJson>;

  constructor() {
    effect(() => {
      const sehenMassnahmen = this.store.sehenMassnahmen();
      this.form.resetForm(sehenMassnahmen?.data);
      this.form.setDisabled(!this.anlassStore.canUserEditAnlassInCurrentState());
      this.infomaterialOptions =
        this.infomaterialStore.patchInfomaterialOptions(sehenMassnahmen?.data?.abgegebenesInfomaterial);
    });
  }

  isDirty(): boolean {
    return this.form.isDirty() || this.store.saveLoader.isLoading();
  }

  abbrechen(): void {
    const sehen = this.store.sehenMassnahmen();
    if (sehen) {
      this.form.resetForm(sehen.data);
    }
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const anlassId = requireValue(this.anlassStore.selectedEntity()?.id);
      const einladungId = requireValue(this.untersuchungStore.currentTab()?.einladungID);
      const payload = this.form.valueOnValid();
      this.store.saveSehenMassnahmen({
        anlassId: anlassId,
        einladungId: toEinladungBackendEntityIdWithClass(einladungId),
        untersuchungSehenMassnahmenSaveJson: payload,
      });
    }
  }
}
