import { ChangeDetectionStrategy, Component, DestroyRef, effect, inject } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { InfomaterialJson, InformationEltern } from '@dvb/data-access-backend';
import { InfomaterialStore, Path, TranslatedStringPipe } from '@dvb/feat-shared';
import {
  CardInCardComponent,
  CheckboxDirective,
  FormControlComponent,
  FormLayoutComponent,
  KachelControlComponent,
  KachelFormSectionComponent,
  NgSelectDirective,
  SaveCancelButtonsComponent,
  StandaloneControlComponent,
  TextareaDirective,
  TitleComponent,
} from '@dvb/ui-components';
import {toEinladungBackendEntityIdWithClass} from '@dvb/data-access-types';
import { DirtyMarker } from '@dvb/util-angular';
import { compareFn, requireValue } from '@dvb/util-shared';
import { TranslocoPipe } from '@jsverse/transloco';
import { NgOptionComponent, NgSelectComponent } from '@ng-select/ng-select';
import { AnlassStore } from '../../anlass.store';
import { UntersuchungStore } from '../untersuchung.store';
import { UntersuchungMassnahmenForm } from './untersuchung-massnahmen.form';
import { UntersuchungMassnahmenStore } from './untersuchung-massnahmen.store';
import {
  UntersuchungPathologischeBefundeComponent,
} from './untersuchung-pathologische-befunde/untersuchung-pathologische-befunde.component';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'feat-untersuchung-massnahmen',
  imports: [
    FormLayoutComponent,
    ReactiveFormsModule,
    CheckboxDirective,
    KachelControlComponent,
    KachelFormSectionComponent,
    TranslocoPipe,
    FormControlComponent,
    NgSelectDirective,
    NgOptionComponent,
    NgSelectComponent,
    TranslatedStringPipe,
    SaveCancelButtonsComponent,
    TextareaDirective,
    StandaloneControlComponent,
    TitleComponent,
    UntersuchungPathologischeBefundeComponent,
    CardInCardComponent
],
  templateUrl: './untersuchung-massnahmen.component.html',
  styleUrl: './untersuchung-massnahmen.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UntersuchungMassnahmenComponent implements DirtyMarker {

  readonly store = inject(UntersuchungMassnahmenStore);
  readonly anlassStore = inject(AnlassStore);
  readonly untersuchungStore = inject(UntersuchungStore);
  readonly infomaterialStore = inject(InfomaterialStore);

  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  informationEltern = Object.values(InformationEltern);
  infomaterialOptions: InfomaterialJson[] | undefined;
  protected readonly compareInfomaterial = compareFn<InfomaterialJson>;

  readonly form = new UntersuchungMassnahmenForm({
      fb: inject(NonNullableFormBuilder),
      destroyRef: inject(DestroyRef),
    },
  );

  constructor() {
    effect(() => {
      const massnahmen = this.store.massnahmen();
      this.form.resetForm(massnahmen?.data);
      this.form.setDisabled(!this.anlassStore.canUserEditAnlassInCurrentState());
      this.infomaterialOptions =
        this.infomaterialStore.patchInfomaterialOptions(massnahmen?.data?.abgegebenesInfomaterial);
    });

    effect(() => {
      const anlass = this.anlassStore.selectedEntity();
      const configData = anlass?.anlassConfig.showTabMassnahmen;
      if (!configData) {
        this.router.navigate(['../', Path.ANLASS_UNTERSUCHUNG_EINLADUNG_DATEN],
          {
            relativeTo: this.route,
            replaceUrl: true,
            queryParamsHandling: 'preserve'
          });
      }
    });
  }

  isDirty(): boolean {
    return this.form.isDirty() || this.store.saveLoader.isLoading();
  }

  abbrechen(): void {
    const massnahmen = this.store.massnahmen();
    if (massnahmen) {
      this.form.resetForm(massnahmen.data);
    }
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const payload = this.form.valueOnValid();
      const anlassId = requireValue(this.anlassStore.selectedEntity()?.id);
      const einladungId = requireValue(this.untersuchungStore.currentTab()?.einladungID);
      this.store.saveMassnahmen({
        anlassId: anlassId,
        einladungId: toEinladungBackendEntityIdWithClass(einladungId),
        untersuchungMassnahmenSaveJson: payload,
      });
    }
  }
}
