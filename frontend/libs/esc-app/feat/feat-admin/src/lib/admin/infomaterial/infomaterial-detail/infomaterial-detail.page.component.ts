import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, effect, inject, untracked} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {InfomaterialSaveJson} from '@dvb/data-access-backend';
import {
  BenutzerStore,
  InfomaterialStore,
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
  FormErrorMessageComponent,
  FormLayoutComponent,
} from '@dvb/ui-components';
import {DirtyMarker} from '@dvb/util-angular';
import {isNullish} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {InfomaterialForm} from './infomaterial-detail.form';

@Component({
  selector: 'feat-infomaterial-detail.page',
  imports: [
    CommonModule,
    PageComponent,
    CardComponent,
    FormLayoutComponent,
    FormErrorMessageComponent,
    ReactiveFormsModule,
    TranslatedStringEditComponent,
    TranslocoPipe,
    ChildFormLayoutDirective,
    FormButtonsComponent,
  ],
  templateUrl: './infomaterial-detail.page.component.html',
  styleUrl: './infomaterial-detail.page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InfomaterialDetailPageComponent implements DirtyMarker {
  readonly tenantSettingsStore = inject(TenantSettingsStore);
  readonly store = inject(InfomaterialStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly #router = inject(Router);
  readonly #activatedRoute = inject(ActivatedRoute);
  readonly #transloco = inject(TranslocoService);
  readonly #translatedStringPipe = inject(TranslatedStringPipe);
  protected readonly Path = Path;

  pageTitle = computed(() => {
    if (this.store.selectedIsNew()) {
      return this.#transloco.translate('infomaterial.detail.page.title.create');
    } else {
      return this.#transloco.translate('infomaterial.detail.page.title',
        {name: this.#translatedStringPipe.transform(this.store.selectedEntity()?.data.name)});
    }
  });

  public form: InfomaterialForm = new InfomaterialForm({
    fb: inject(NonNullableFormBuilder),
    tenantSettingsStore: this.tenantSettingsStore,
    infomaterialStore: this.store,
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
      const payload: InfomaterialSaveJson = this.form.valueOnValid();
      if (this.store.selectedIsNew()) {
        this.store.createInfomaterial({newInfomaterial: payload, onSuccess: () => this.resetFormAndGoToList()});
      } else {
        this.store.updateInfomaterial({infomaterialToUpdate: payload, onSuccess: () => this.resetFormAndGoToList()});
      }
    }
  }

  private resetFormAndGoToList(): void {
    this.form.reset();
    this.#router.navigate([Path.RELATIVE_ONE_UP], {relativeTo: this.#activatedRoute});
  }

}
