import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, effect, inject, untracked} from '@angular/core';
import {FormsModule, NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {EntityAktivStatus, KlasseJson, Klassenstufe, SchuleOptionJson} from '@dvb/data-access-backend';
import {BenutzerStore, KlassenStore, Path, SchulenOptionsStore} from '@dvb/feat-shared';
import {
  FormButtonsComponent,
  FormControlComponent,
  FormErrorMessageComponent,
  FormLayoutComponent,
  HeadingDirective,
  NgSelectDirective,
  TextInputDirective,
} from '@dvb/ui-components';
import {DirtyMarker} from '@dvb/util-angular';
import {
  fromISODateTimeString,
  hasListEditBerechtigung,
  isExternalObject,
  isNotNullish,
  isNullish
} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {KlassenDetailForm} from '../klassen-detail.form';

@Component({
  selector: 'feat-klassen-daten.page',
  imports: [
    CommonModule,
    FormLayoutComponent,
    FormsModule,
    ReactiveFormsModule,
    HeadingDirective,
    TranslocoPipe,
    FormControlComponent,
    TextInputDirective,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    FormButtonsComponent,
    FormErrorMessageComponent,
  ],
  templateUrl: './klassen-daten.component.html',
  styleUrl: './klassen-daten.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class KlassenDatenComponent implements DirtyMarker {
  readonly store = inject(KlassenStore);
  readonly schulenOptionsStore = inject(SchulenOptionsStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly #router = inject(Router);
  readonly #activatedRoute = inject(ActivatedRoute);
  protected readonly Path = Path;
  readonly klassenstufen = Object.values(Klassenstufe);
  readonly klasseAktivStati = Object.values(EntityAktivStatus);
  readonly #transloco = inject(TranslocoService);

  canEditList = this.benutzerStore.createHasRequiredRoleSig(hasListEditBerechtigung());

  schuleOptions = computed(
    () => this.schulenOptionsStore.entitiesActiveIncluding(this.store.selectedEntity()?.data.schule),
  );

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

  form = new KlassenDetailForm({
    fb: inject(NonNullableFormBuilder),
  });

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
          this.form.group.disable();
        } else if (selectedEntity.data.schule) {
          // Schule darf nicht mehr veraendert werden
          this.form.group.get('schule')?.disable();
        }
      });
    });
  }

  isDirty(): boolean {
    return this.form.isDirty() || this.store.saveLoader.isLoading();
  }

  compareFn(item: SchuleOptionJson, selected: SchuleOptionJson) {
    return item.id === selected.id;
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const payload = this.form.valueOnValid();
      if (this.store.selectedIsNew()) {
        this.store.createKlasse({
          klasse: payload,
          onSuccess: (result: KlasseJson) => {
            this.form.reset();
            this.#router.navigate([Path.RELATIVE_TWO_UP, result.id, Path.LEHRPERSONEN, Path.ALL], {
              relativeTo: this.#activatedRoute,
            });
          },
        });
      } else {
        this.store.updateKlasse({
          klasseToUpdate: payload,
          onSuccess: () => {
            this.form.reset();
            this.#router.navigate([Path.RELATIVE_ONE_UP], {relativeTo: this.#activatedRoute});
          },
        });
      }
    }
  }
}
