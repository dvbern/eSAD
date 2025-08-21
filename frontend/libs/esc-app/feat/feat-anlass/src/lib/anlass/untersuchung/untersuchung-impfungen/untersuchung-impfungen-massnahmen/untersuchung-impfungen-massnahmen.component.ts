import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, DestroyRef, effect, inject } from '@angular/core';
import { FormsModule, NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Router, UrlTree } from '@angular/router';
import { AnlassArt, AnlassSaveJson, AnlassTyp, InformationEltern } from '@dvb/data-access-backend';
import { BenutzerStore, Path } from '@dvb/feat-shared';
import {
  ButtonComponent,
  ButtonsRightContainerComponent,
  CheckboxDirective,
  FormControlComponent,
  FormLayoutComponent,
  HeadingDirective,
  NgSelectDirective,
  SaveCancelButtonsComponent,
  StandaloneControlComponent,
  TextareaDirective,
} from '@dvb/ui-components';
import { DirtyMarker } from '@dvb/util-angular';
import { hasImpfungenEditBerechtigung, requireValue } from '@dvb/util-shared';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import { NgOptionComponent, NgSelectComponent } from '@ng-select/ng-select';
import { AnlassStore } from '../../../anlass.store';
import { UntersuchungStore } from '../../untersuchung.store';
import { UntersuchungImpfungenStore } from '../untersuchung-impfungen.store';
import { UntersuchungImpfungenMassnahmenForm } from './untersuchung-impfungen-massnahmen.form';

@Component({
  selector: 'feat-untersuchung-impfungen-massnahmen',
  imports: [
    CommonModule,
    FormLayoutComponent,
    ReactiveFormsModule,
    CheckboxDirective,
    TranslocoPipe,
    FormControlComponent,
    NgSelectDirective,
    NgOptionComponent,
    NgSelectComponent,
    SaveCancelButtonsComponent,
    StandaloneControlComponent,
    HeadingDirective,
    ButtonComponent,
    ButtonsRightContainerComponent,
    TextareaDirective,
    FormsModule,
  ],
  templateUrl: './untersuchung-impfungen-massnahmen.component.html',
  styleUrl: './untersuchung-impfungen-massnahmen.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UntersuchungImpfungenMassnahmenComponent implements DirtyMarker {
  readonly impfungenStore = inject(UntersuchungImpfungenStore);
  readonly anlassStore = inject(AnlassStore);
  readonly untersuchungStore = inject(UntersuchungStore);
  readonly benutzerStore = inject(BenutzerStore);

  readonly #router = inject(Router);
  readonly #translocoService = inject(TranslocoService);

  informationEltern = Object.values(InformationEltern);

  canEdit = computed(
    () =>
      this.benutzerStore.createHasRequiredRoleSig(hasImpfungenEditBerechtigung())() &&
      this.anlassStore.canUserEditAnlassInCurrentState(),
  );

  readonly form = new UntersuchungImpfungenMassnahmenForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
  });

  constructor() {
    effect(() => {
      const massnahmen = this.impfungenStore.massnahmen();
      this.form.resetForm(massnahmen?.data);
      this.form.setDisabled(!this.canEdit());
    });
  }

  isDirty(): boolean {
    return this.form.isDirty() || this.impfungenStore.loader.isLoading();
  }

  abbrechen(): void {
    const massnahmen = this.impfungenStore.massnahmen();
    if (massnahmen) {
      this.form.resetForm(massnahmen.data);
    }
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const payload = this.form.valueOnValid();
      this.impfungenStore.saveMassnahmen({
        anlassId: requireValue(this.anlassStore.selectedEntity()).id,
        einladungId: requireValue(this.untersuchungStore.currentTab()).einladungID,
        untersuchungImpfungenMassnahmenSaveJson: payload,
      });
    }
  }

  public einzelFolgeterminErstellen(): void {
    this.impfungenStore.einzelTerminErstellen({
      anlassId: requireValue(this.anlassStore.selectedEntity()).id,
      einladungId: requireValue(this.untersuchungStore.currentTab()).einladungID,
      onSuccess: () => {
        const anlassTitel = this.#translocoService.translate('anlass.impfen.einzeltermin', {
          kind: this.untersuchungStore.currentTab()?.label,
        });
        const anlassPartial: Partial<AnlassSaveJson> = {
          titel: anlassTitel,
          anlassArt: AnlassArt.EINZEL_ANLASS,
          anlassTyp: AnlassTyp.IMPFTERMIN,
        };
        const url: UrlTree = this.#router.createUrlTree([Path.ANLASS, Path.CREATE], {
          queryParams: {
            anlassPartial: JSON.stringify(anlassPartial),
            kindId: this.untersuchungStore.currentTab()?.kindID,
          },
        });
        window.open(url.toString(), '_blank');
      },
    });
  }
}
