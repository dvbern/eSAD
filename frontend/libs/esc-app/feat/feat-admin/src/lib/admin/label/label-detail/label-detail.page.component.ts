
import {ChangeDetectionStrategy, Component, computed, effect, inject, untracked} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {LabelSaveJson, LabelTyp} from '@dvb/data-access-backend';
import {
  BenutzerStore,
  LabelsStore,
  PageComponent,
  Path,
  TenantSettingsStore,
  TranslatedStringEditComponent,
  TranslatedStringPipe,
} from '@dvb/feat-shared';
import {
  CardComponent,
  ChildFormLayoutDirective,
  FormButtonsComponent,
  FormControlComponent,
  FormErrorMessageComponent,
  FormLayoutComponent,
  NgSelectDirective,
} from '@dvb/ui-components';
import {DirtyMarker} from '@dvb/util-angular';
import {hasLabelEditBerechtigung, isNullish} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {LabelDetailForm} from './label-detail.form';

@Component({
  selector: 'feat-label-detail.page',
  imports: [
    PageComponent,
    TranslocoPipe,
    ReactiveFormsModule,
    FormControlComponent,
    FormLayoutComponent,
    CardComponent,
    TranslatedStringEditComponent,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    FormErrorMessageComponent,
    ChildFormLayoutDirective,
    FormButtonsComponent
],
  templateUrl: './label-detail.page.component.html',
  styleUrl: './label-detail.page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LabelDetailPageComponent implements DirtyMarker {
  readonly tenantSettingsStore = inject(TenantSettingsStore);
  readonly store = inject(LabelsStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly #router = inject(Router);
  readonly #activatedRoute = inject(ActivatedRoute);
  readonly labelTypen = Object.values(LabelTyp);
  readonly #trasnloco = inject(TranslocoService);
  readonly #translatedStringPipe = inject(TranslatedStringPipe);
  protected readonly Path = Path;

  pageTitle = computed(() => {
    if (this.store.selectedIsNew()) {
      return this.#trasnloco.translate('label.detail.page.title.create');
    } else {
      return this.#trasnloco.translate('label.detail.page.title',
        { name: this.#translatedStringPipe.transform(this.store.selectedEntity()?.data.name) });
    }
  });

  canEditLabel = this.benutzerStore.createHasRequiredRoleSig(hasLabelEditBerechtigung());

  public form: LabelDetailForm = new LabelDetailForm({
    fb: inject(NonNullableFormBuilder),
    tenantSettingsStore: this.tenantSettingsStore,
    labelsStore: this.store,
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
      });
    });
  }

  isDirty(): boolean {
    return this.form.isDirty() || this.store.saveLoader.isLoading();
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const payload: LabelSaveJson = this.form.valueOnValid();
      if (this.store.selectedIsNew()) {
        this.store.createLabel(
          {
            newLabel: payload,
            onSuccess: () => this.resetFormAndGoToList(),
          },
        );
      } else {
        this.store.updateLabel(
          {
            labelToUpdate: payload,
            onSuccess: () => this.resetFormAndGoToList(),
          },
        );
      }

    }
  }

  private resetFormAndGoToList(): void {
    this.form.reset(); // reset form sicne we successfully saved, this prevents unsaved changes dialog
    this.#router.navigate([Path.RELATIVE_ONE_UP], { relativeTo: this.#activatedRoute });
  }

}
