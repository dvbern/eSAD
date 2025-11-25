import {DatePipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, DestroyRef, effect, inject, untracked} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {AnlassArt, AnlassConfigOptionJson, AnlassTyp, BenutzerOptionJson} from '@dvb/data-access-backend';
import {
  AnlassConfigOptionsStore,BenutzerOptionsStore, BenutzerStore, KlassenOptionsStore,
  Path,
  TimetravelStore,
} from '@dvb/feat-shared';
import {
  FormButtonsComponent,
  FormControlComponent,
  FormErrorMessageComponent,
  FormLayoutComponent,
  HeadingDirective,
  NgSelectDirective,
  TextareaDirective,
  TextInputDirective,
} from '@dvb/ui-components';
import {
  compareFn,
  fromISODateString,
  hasAlleAnlaesseBerechtigungen,
  hasLaeuseAnlassBerechtigungen,
  isNullish,
  requireValue,
} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {AnlassStore} from '../../../anlass.store';
import {anlassTitlePrefix} from '../../../anlass.util';
import {AnlassKinderStore} from '../../anlass-kinder.store';
import {AnlassDatenForm} from './anlass-daten-form';

@Component({
  selector: 'feat-anlass-bearbeiten-anlassdaten-daten',
  templateUrl: './anlass-daten.component.html',
  styleUrl: './anlass-daten.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [DatePipe],
  imports: [
    FormLayoutComponent,
    ReactiveFormsModule,
    HeadingDirective,
    TranslocoPipe,
    FormControlComponent,
    TextInputDirective,
    FormButtonsComponent,
    FormErrorMessageComponent,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    TextareaDirective,
  ],
})
export class AnlassDatenComponent {

  readonly store = inject(AnlassStore);
  readonly benutzerOptionsStore = inject(BenutzerOptionsStore);
  readonly anlassKinderStore = inject(AnlassKinderStore);
  readonly #router = inject(Router);
  readonly #activatedRoute = inject(ActivatedRoute);
  readonly transloco = inject(TranslocoService);
  readonly timetravelStore = inject(TimetravelStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly anlassConfigOptionsStore = inject(AnlassConfigOptionsStore);

  public form = new AnlassDatenForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    klassenOptionsStore: inject(KlassenOptionsStore),
    anlassKinderStore: this.anlassKinderStore,
    anlassConfig: this.store.selectedEntity()?.anlassConfig,
    timetravelStore: inject(TimetravelStore),
    anlassConfigOptionsStore: this.anlassConfigOptionsStore,
    isNew: this.store.selectedIsNew()
  });

  anlassArten = Object.values(AnlassArt);

  #showLaeuseAnlass = this.benutzerStore.createHasRequiredRoleSig(hasLaeuseAnlassBerechtigungen());
  #showAlleAnlaesse = this.benutzerStore.createHasRequiredRoleSig(hasAlleAnlaesseBerechtigungen());
  anlassTypen = computed(() => {
    if(this.#showAlleAnlaesse()){
      return Object.values(this.anlassConfigOptionsStore.getVisibleAnlassTypen());
    }
    if(this.#showLaeuseAnlass()){
      return [ AnlassTyp.LAEUSEKONTROLLE ];
    }
    return [];
  });

  protected readonly Path = Path;
  protected readonly compareFnBenutzer = compareFn<BenutzerOptionJson>;
  protected readonly compareFnAnlassConfig = compareFn<AnlassConfigOptionJson>;

  anlassTitlePrefix = anlassTitlePrefix;

  constructor() {
    effect(() => {
      const selectedEntity = this.store.selectedEntity();
      const mode = this.store.detailMode();

      // fill with entity or reset to empty
      this.form.resetForm(selectedEntity, mode);

      untracked(() => {
        // prefill only once and only if no selected entity is present
        const prefilledAnlass = this.store.prefilledAnlass();
        if (prefilledAnlass && !selectedEntity){
          this.form.group.reset(prefilledAnlass);
        }
      });
    });
  }

  displaySchuljahr(schuljahr: number | undefined) {
    if (isNullish(schuljahr)) {
      return undefined;
    }
    return schuljahr.toString() + '/' + (schuljahr + 1).toString().substring(2)
  }

  getSchuljahrOptions(): number[] | undefined {
    const now = fromISODateString(this.timetravelStore.now()) || new Date();
    const schuljahr = now.getFullYear();
    const options = [];

    if (isNullish(schuljahr)) {
      return undefined;
    }
    for (let jahr = schuljahr - 5; jahr <= schuljahr + 5; jahr++) {
      options.push(jahr);
    }
    return options;
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const payload = this.form.valueOnValid();
      if (this.store.selectedIsNew()) {

        // create new Anlass
        this.store.createAnlass({
          anlass: payload,
          onSuccess: (result) => {
            // prefill Kind
            const prefillKindId = this.store.prefilledKindId();
            const anlassId = requireValue(result.id);

            this.store.resetPrefill();
            if (prefillKindId) {
              this.anlassKinderStore.addKinder({ anlassId: anlassId, kindIds: [prefillKindId] });
            }

            // reset form
            this.form.reset();
            // navigate
            this.#router.navigate(
              [Path.RELATIVE_ONE_UP, result.id, Path.ANLASS_BEARBEITUNG],
              { relativeTo: this.#activatedRoute })
          },
        });
      } else {
        // update existing Anlass
        this.store.updateAnlass({
          anlass: payload,
          onSuccess: () => {
            this.form.reset();
            this.#router.navigate([Path.RELATIVE_ONE_UP, Path.ANLASS_PLANUNG], {
              relativeTo: this.#activatedRoute,
            });
          },
        });
      }
    }
  }
}
