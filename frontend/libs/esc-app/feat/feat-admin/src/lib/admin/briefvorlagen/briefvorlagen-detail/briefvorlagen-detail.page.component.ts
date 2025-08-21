import {CommonModule, Location} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, effect, inject, signal, untracked} from '@angular/core';
import {FormsModule, NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {BriefvorlageSaveJson, BriefvorlageType, FileDataJson} from '@dvb/data-access-backend';
import {BriefvorlagenStore} from '@dvb/feat-shared';
import {BenutzerStore} from '@dvb/feat-shared';
import {Path} from '@dvb/feat-shared';
import {PageComponent} from '@dvb/feat-shared';
import {TenantSettingsStore} from '@dvb/feat-shared';
import {FileDropComponent, UploadTableComponent} from '@dvb/feat-shared';
import {
  CardComponent,
  DashboardComponent,
  FormControlComponent,
  FormErrorMessageComponent,
  FormLayoutComponent,
  HeadingDirective,
  NgSelectDirective,
  SaveCancelButtonsComponent,
  TextInputDirective,
} from '@dvb/ui-components';
import {AppNotificationStore, DirtyMarker, TableModel} from '@dvb/util-angular';
import {hasBriefvorlageEditBerechtigung, isNullish, requireValue} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {BriefvorlageForm} from './briefvorlage-detail.form';

@Component({
  selector: 'feat-briefvorlagen-detail',
  imports: [
    CommonModule, CardComponent, FormControlComponent,
    FormLayoutComponent, FormsModule, NgOptionComponent, NgSelectComponent,
    NgSelectDirective, PageComponent, TranslocoPipe, TextInputDirective,
    ReactiveFormsModule, FileDropComponent, HeadingDirective, UploadTableComponent, SaveCancelButtonsComponent,
    DashboardComponent, FormErrorMessageComponent,
  ],
  templateUrl: './briefvorlagen-detail.page.component.html',
  styles: ':host { display:block;}',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BriefvorlagenDetailPageComponent implements DirtyMarker {

  readonly store = inject(BriefvorlagenStore);
  readonly #transloco = inject(TranslocoService);
  readonly briefvorlageTypen = Object.values(BriefvorlageType);
  readonly tenantSettingsStore = inject(TenantSettingsStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly #router = inject(Router);
  readonly #activatedRoute = inject(ActivatedRoute);
  readonly #location = inject(Location);
  readonly #appNotificationStore = inject(AppNotificationStore);
  protected readonly Path = Path;

  fileUpload: File | undefined;

  readonly acceptedFiletypes = signal('.doc, .docx');

  briefvorlageFiles = signal<FileDataJson[]>([]);
  uploadTable = new TableModel<FileDataJson>(
    {rawListSignal: this.briefvorlageFiles},
    {frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll'});

  canEditList = this.benutzerStore.createHasRequiredRoleSig(hasBriefvorlageEditBerechtigung());

  pageTitle = computed(() => {
    if (this.store.selectedIsNew()) {
      return this.#transloco.translate('briefvorlage.detail.page.title.create');
    } else {
      return this.#transloco.translate('briefvorlage.detail.page.title',
        {name: this.store.selectedEntity()?.data.name});
    }
  });

  public form = new BriefvorlageForm({
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
        this.briefvorlageFiles.set([selectedEntity.fileData]);
        this.form.reset(selectedEntity.data, mode);
      });
    });
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      if (this.store.selectedIsNew() && isNullish(this.fileUpload)) {
        this.#appNotificationStore.addDangerNotification(this.#transloco.translate('ui.validation-error-file.required'));
        return;
      }
      const payload: BriefvorlageSaveJson = this.form.valueOnValid();
      if (this.store.selectedIsNew()) {
        this.store.createBriefvorlagen({
          fileData: requireValue(this.fileUpload),
          briefvorlageSaveJson: payload,
          onSuccess: () => this.resetFormAndGoToList(),
        });
      } else {
        this.store.updateBriefvorlagen({
          fileData: this.fileUpload,
          briefvorlageSaveJson: payload,
          onSuccess: () => this.resetFormAndGoToList(),
        });
      }
    }
  }

  public onCancelClicked(): void {
    this.#location.back();
  }

  public onFilesSelected($event: File[]): void {
    this.fileUpload = $event[0];
  }

  public isDirty(): boolean | Promise<boolean> {
    return this.form.isDirty() || this.store.saveLoader.isLoading();
  }

  public downloadBriefvorlageFile(): void {
    this.store.downloadBriefvorlageFile({
      briefvorlageId: requireValue(this.store.selectedEntity()?.id),
    });
  }

  private resetFormAndGoToList(): void {
    this.form.reset();
    this.#router.navigate([Path.RELATIVE_ONE_UP], {relativeTo: this.#activatedRoute});
  }
}
