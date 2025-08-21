import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, DestroyRef, effect, inject, untracked} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {BenutzerOptionJson, Geschlecht, Klassenstufe, Land} from '@dvb/data-access-backend';
import {BenutzerOptionsStore, BenutzerStore, EventBusStore} from '@dvb/feat-shared';
import {
  CheckboxDirective,
  FormControlComponent,
  FormGroupComponent,
  FormLayoutComponent,
  HeadingDirective,
  NgSelectDirective,
  RadioDirective,
  SaveCancelButtonsComponent,
  SingleCheckboxWrapperComponent,
  SingleRadioWrapperComponent,
  TextInputDirective,
} from '@dvb/ui-components';
import {DetailMode, DirtyMarker} from '@dvb/util-angular';
import {hasUntersuchungKindDatenEditBerechtigung, requireValue} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {AnlassStore} from '../../anlass.store';
import {UntersuchungStore} from '../untersuchung.store';
import {UntersuchungDatenForm} from './untersuchung-daten.form';
import {UntersuchungDatenStore} from './untersuchung-daten.store';

@Component({
  selector: 'feat-untersuchung-daten',
  templateUrl: './untersuchung-daten.component.html',
  styleUrl: './untersuchung-daten.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule, FormLayoutComponent, FormControlComponent,
    HeadingDirective, TranslocoPipe, TextInputDirective, ReactiveFormsModule, SingleRadioWrapperComponent,
    RadioDirective, FormGroupComponent, NgOptionComponent, NgSelectComponent, NgSelectDirective, CheckboxDirective,
    SingleCheckboxWrapperComponent, SaveCancelButtonsComponent,
  ],
})
export class UntersuchungDatenComponent implements DirtyMarker {
  readonly untersuchungStore = inject(UntersuchungStore);
  readonly datenStore = inject(UntersuchungDatenStore);
  readonly anlassStore = inject(AnlassStore);
  readonly eventBusStore = inject(EventBusStore);
  readonly destroyRef = inject(DestroyRef);
  readonly benutzerStore = inject(BenutzerStore);

  canEdit = computed(() => this.benutzerStore.createHasRequiredRoleSig(hasUntersuchungKindDatenEditBerechtigung())()
    && this.anlassStore.canUserEditAnlassInCurrentState());

  protected readonly DetailMode = DetailMode;
  protected readonly Geschlecht = Geschlecht;
  protected readonly klassenstufe = Object.values(Klassenstufe);
  protected readonly nationalitaeten = Object.values(Land);

  readonly benutzerOptionsStore = inject(BenutzerOptionsStore);
  geschlechtOptions = Object.values(Geschlecht);

  public form = new UntersuchungDatenForm(inject(NonNullableFormBuilder));

  constructor() {
    effect(() => {
      const durchfuehrungDaten = this.datenStore.durchfuehrungDaten();
      const kindDaten = this.datenStore.kindDaten();
      if (durchfuehrungDaten || kindDaten) {
        untracked(() => {
          this.form.resetForm(durchfuehrungDaten, kindDaten, DetailMode.EDIT);
        });
      }
      const anlassConfig = this.anlassStore.selectedEntity()?.anlassConfig;
      if (anlassConfig) {
        this.form.setValidators(anlassConfig);
      }
      // Falls Anlass Fertig können auch die Kind Daten bearbeitet werden.
      this.form.setDisabledForm(
        !this.canEdit(),
        this.anlassStore.isAbgeschlossen());
    });

    // Subscribe to untersuchung abschliessen events
    this.eventBusStore
      .on('UNTERSUCHUNG_ABSCHLIESSEN_REQUESTED')
      .pipe(takeUntilDestroyed())
      .subscribe(event => {
        const currentEinladungId = requireValue(this.untersuchungStore.currentTabId());

        // Only validate if this is the current tab being closed
        if (currentEinladungId === event.einladungId) {
          this.validateFormOnAbschliessen();
        }
      });
  }

  isDirty(): boolean {
    return this.form.isDirty() || this.datenStore.saveLoader.isLoading();
  }

  compareFn(item: BenutzerOptionJson, selected: BenutzerOptionJson) {
    return item.id === selected.id;
  }

  abbrechen(): void {
    const durchfuehrungDaten = this.datenStore.durchfuehrungDaten();
    const kindDaten = this.datenStore.kindDaten();
    this.form.resetForm(durchfuehrungDaten, kindDaten, DetailMode.EDIT);
  }

  async save(): Promise<void> {
    if (await this.form.validate()) {
      if (this.anlassStore.isAbgeschlossen()) {
        this.datenStore.saveDurchfuehrungAbgeschlossen({
          anlassId: requireValue(this.anlassStore.selectedEntity()).id,
          einladungId: requireValue(this.untersuchungStore.currentTab()).einladungID,
          untersuchungDurchfuehrungDataSaveJson: this.form.valueOnValidDurchfuehrungKind(),
        })
      } else {
        this.datenStore.saveDurchfuehrungDaten({
          anlassId: requireValue(this.anlassStore.selectedEntity()).id,
          einladungId: requireValue(this.untersuchungStore.currentTab()).einladungID,
          untersuchungDurchfuehrungSaveJson: this.form.valueOnValid(),
        });
      }
    }
  }

  /**
   * Validates the form when untersuchung abschliessen is called
   * This shows any validation errors to the user
   */
  private validateFormOnAbschliessen(): void {
    // Don't wait for the promise, we just want to trigger validation
    this.form.validate().then(() => {
      // Form validation triggered, errors will be displayed if any
    });
  }
}
