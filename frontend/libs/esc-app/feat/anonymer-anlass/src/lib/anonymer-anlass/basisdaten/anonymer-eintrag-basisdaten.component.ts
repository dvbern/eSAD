import {ChangeDetectionStrategy, Component, computed, DestroyRef, inject} from '@angular/core';
import {AppNotificationStore, DetailMode, DirtyMarker} from '@dvb/util-angular';
import {TranslocoPipe, TranslocoService} from "@jsverse/transloco";
import {
  ButtonComponent,
  FormControlComponent,
  FormFullWidthDirective,
  FormGroupComponent,
  FormLayoutComponent,
  HeadingDirective,
  NgSelectDirective,
  RadioDirective,
  SingleRadioWrapperComponent,
  TextInputDirective
} from "@dvb/ui-components";
import {NonNullableFormBuilder, ReactiveFormsModule} from "@angular/forms";
import {NgOptionComponent, NgSelectComponent} from "@ng-select/ng-select";
import {AnonymerEintragBasisdatenForm} from "./anonymer-eintrag-basisdaten.form";
import {hasAnlassCreateOrEditForNotAbgeschlossenBerechtigung, requireValue} from "@dvb/util-shared";
import {AnlassConfigOptionsStore, BenutzerStore, Path} from "@dvb/feat-shared";
import {AnonymerEintragStore} from "../anonymer-eintrag.store";
import {DatePipe} from "@angular/common";
import {AnonymerEintragJson, Geschlecht} from "@dvb/data-access-backend";
import {ActivatedRoute, Router} from "@angular/router";

@Component({
  selector: 'feat-anonymer-eintrag-basisdaten',
  templateUrl: './anonymer-eintrag-basisdaten.component.html',
  styleUrl: './anonymer-eintrag-basisdaten.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [
    TranslocoPipe,
    FormLayoutComponent,
    ReactiveFormsModule,
    HeadingDirective,
    FormControlComponent,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    TextInputDirective,
    ButtonComponent,
    DatePipe,
    FormGroupComponent,
    RadioDirective,
    SingleRadioWrapperComponent,
    FormFullWidthDirective
  ],
})
export class AnonymerEintragBasisdatenComponent implements DirtyMarker {

  readonly store = inject(AnonymerEintragStore);
  readonly anlassConfigOptionsStore = inject(AnlassConfigOptionsStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly #router = inject(Router);
  readonly #activatedRoute = inject(ActivatedRoute);
  readonly #appNotificationStore = inject(AppNotificationStore);
  readonly #transloco = inject(TranslocoService);

  protected readonly DetailMode = DetailMode;

  canEdit = this.benutzerStore.createHasRequiredRoleSig(hasAnlassCreateOrEditForNotAbgeschlossenBerechtigung());

  geschlechtOptions = Object.values(Geschlecht);
  anlassTypen = computed(() => Object.values(this.anlassConfigOptionsStore.getVisibleAnlassTypenAnonym()));

  public form = new AnonymerEintragBasisdatenForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
  });

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const createJson = this.form.valueOnValid();
      this.store.createAnonymerEintrag({
        createJson: requireValue(createJson),
        onSuccess: (result: AnonymerEintragJson) => {
          this.form.reset();
          this.#appNotificationStore.addSuccessNotification(
            this.#transloco.translate('statistik.basisdaten.hash.success', {hashvalue: result.hash})
          );
          this.#router.navigate([
              Path.RELATIVE_ONE_UP,
              result.anlassJson.id,
              Path.ANONYM_UNTERSUCHUNG,
              Path.ANLASS_UNTERSUCHUNG_EINLADUNG,
              result.id,
            ],
            {
              relativeTo: this.#activatedRoute,
            });
        },
      });
    }
  }

  isDirty(): boolean {
    return this.form.isDirty() || this.store.saveLoader.isLoading();
  }
}
