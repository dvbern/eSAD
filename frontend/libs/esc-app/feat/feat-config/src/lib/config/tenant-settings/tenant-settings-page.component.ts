import { ChangeDetectionStrategy, Component, effect, inject } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import {AgbResponsability, AppLanguages, EinladungStatus, FeatureEnabledSetting, LandingPageOption} from '@dvb/data-access-backend';
import { PageComponent, Path, TenantSettingsStore, TranslatedEnumViewValuePipe } from '@dvb/feat-shared';
import {
  CardComponent,
  FormButtonsComponent,
  FormControlComponent,
  FormErrorMessageComponent,
  FormLayoutComponent,
  HeadingDirective,
  NgSelectDirective, TextInputDirective,
} from '@dvb/ui-components';
import { AppNotificationStore, DetailMode } from '@dvb/util-angular';
import { isNullish } from '@dvb/util-shared';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import { NgOptionComponent, NgSelectComponent } from '@ng-select/ng-select';
import { TenantSettingsForm } from './tenant-settings.form';

@Component({
  selector: 'feat-tenant-config',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    // layout
    PageComponent,
    CardComponent,
    FormLayoutComponent,
    // form
    ReactiveFormsModule,
    FormControlComponent,
    FormErrorMessageComponent,
    FormButtonsComponent,
    // select
    NgSelectComponent,
    NgOptionComponent,
    NgSelectDirective,
    // misc
    TranslocoPipe,
    HeadingDirective,
    TranslatedEnumViewValuePipe,
    TextInputDirective,
  ],
  templateUrl: './tenant-settings-page.component.html',
  styles: ':host { display:block;}',
})
export class TenantSettingsPageComponent {
  protected readonly Path = Path;
  protected readonly DetailMode = DetailMode;

  readonly tenantSettingsStore = inject(TenantSettingsStore);
  readonly appNotificationStore = inject(AppNotificationStore);
  readonly #transloco = inject(TranslocoService);
  form: TenantSettingsForm = new TenantSettingsForm({ fb: inject(NonNullableFormBuilder) });

  // Options
  appLanguageOptions = Object.values(AppLanguages);
  agbResponsabilityOptions = Object.values(AgbResponsability);
  featureSettingOptions = Object.values(FeatureEnabledSetting);
  einladungStatusOptions = Object.values(EinladungStatus);
  landingPageOptions = Object.values(LandingPageOption);

  // Page state: this page is a plain settings editor, always in edit mode
  detailMode: DetailMode = DetailMode.EDIT;
  isCreate = false;
  canEdit = true;

  constructor() {
    effect(() => {
      const selectedEntity = this.tenantSettingsStore.settings();
      if (isNullish(selectedEntity)) {
        return;
      }
      this.form.reset(selectedEntity.data, this.detailMode);
    });
  }
  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const tenantSaveSettings = this.form.valueOnValid();
      this.tenantSettingsStore.updateTenantSettings({
        tenantsettingSaveJson: tenantSaveSettings,
        onSuccess: () => {
          this.appNotificationStore.addSuccessNotification(
            this.#transloco.translate('config.tenant.setting.save.success')
          );
        },
      });
    }
  }
}
