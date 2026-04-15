import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, DestroyRef, effect, inject, untracked} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {BenutzerOptionJson, EntityAktivStatus, QuartierOptionJson, SchuleJson,} from '@dvb/data-access-backend';
import {
  AdresseComponent,
  AdresseForm,
  BenutzerOptionsStore,
  BenutzerStore,
  Path,
  QuartiereOptionsStore,
  SchulenStore,
} from '@dvb/feat-shared';
import {
  FormButtonsComponent,
  FormControlComponent,
  FormErrorMessageComponent,
  FormFullWidthDirective,
  FormLayoutComponent,
  HeadingDirective,
  NgSelectDirective,
  TextareaDirective,
  TextInputDirective,
} from '@dvb/ui-components';
import {DetailMode, DirtyMarker} from '@dvb/util-angular';
import {
  fromISODateTimeString,
  hasListEditBerechtigung,
  isExternalObject,
  isNotNullish,
  isNullish
} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {SchulenStammdatenForm} from './schulen-detail-stammdaten.form';

@Component({
  selector: 'feat-schulen-detail-stammdaten',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    HeadingDirective,
    TranslocoPipe,
    FormControlComponent,
    TextInputDirective,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    TextareaDirective,
    FormLayoutComponent,
    FormButtonsComponent,
    FormErrorMessageComponent,
    AdresseComponent,
    FormFullWidthDirective,
  ],
  templateUrl: './schulen-detail-stammdaten.component.html',
  styleUrl: './schulen-detail-stammdaten.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SchulenDetailStammdatenComponent implements DirtyMarker {
  readonly store = inject(SchulenStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly quartierStore = inject(QuartiereOptionsStore);
  readonly benutzerOptionsStore = inject(BenutzerOptionsStore);
  readonly #router = inject(Router);
  readonly #activatedRoute = inject(ActivatedRoute);
  readonly #transloco = inject(TranslocoService);
  readonly schuleAktivStati = Object.values(EntityAktivStatus);
  protected readonly Path = Path;

  canEditList = this.benutzerStore.createHasRequiredRoleSig(hasListEditBerechtigung());

  getImportSchnittstelle = computed(() => {
    const externalImportInfo = this.store.selectedEntity()?.externalImportInfoJson;
    return isNotNullish(externalImportInfo) ?
      this.#transloco.translate('ImportTyp.' + externalImportInfo.importTyp) :
      '';
  });

  isExternalObject = computed(() => isExternalObject(this.store.selectedEntity()));

  getDateLatestImportIfExternalObject() {
    if (this.isExternalObject()) {
      return fromISODateTimeString(this.store.selectedEntity()?.externalImportInfoJson?.timestampLatestImport);
    }
    return undefined;
  }

  public form = new SchulenStammdatenForm({
      fb: inject(NonNullableFormBuilder),
      adresseForm: new AdresseForm({
        fb: inject(NonNullableFormBuilder),
        destroyRef: inject(DestroyRef),
        withLand: false,
        parentIdentifier: 'schule-detail-stammdaten',
      }),
    },
  );

  constructor() {
    effect(() => {
      const selectedEntity = this.store.selectedEntity();
      const mode = this.store.detailMode();
      if (isNullish(selectedEntity)) {
        return;
      }
      untracked(() => {
        this.form.reset(selectedEntity.data, mode);
        if (this.isExternalObject()) {
          this.form.controlAt('schulhaus').disable();
          this.form.controlAt('schulstandort').disable();
          this.form.controlAt('schulkreis').disable();
        }
      });
    });
  }

  isDirty(): boolean {
    return this.form.isDirty() || this.store.saveLoader.isLoading();
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const payload = this.form.valueOnValid();
      if (this.store.selectedIsNew()) {
        this.store.createSchule({
          schule: payload,
          onSuccess: (result: SchuleJson) => {
            this.form.reset();
            this.#router.navigate([Path.RELATIVE_TWO_UP, result.id, Path.KONTAKTPERSONEN], {
              relativeTo: this.#activatedRoute,
            });
          },
        });
      } else {
        this.store.updateSchule({
          schule: payload,
          onSuccess: () => {
            this.form.reset();
            this.#router.navigate([Path.RELATIVE_ONE_UP, DetailMode.VIEW], {relativeTo: this.#activatedRoute});
          },
        });
      }
    }
  }

  compareQuartierFn(item: QuartierOptionJson, selected: QuartierOptionJson): boolean {
    return item.id === selected.id;
  }

  compareBenutzerFn(item: BenutzerOptionJson, selected: BenutzerOptionJson): boolean {
    return item?.id === selected?.id;
  }

  protected readonly EntityAktivStatus = EntityAktivStatus;
}
