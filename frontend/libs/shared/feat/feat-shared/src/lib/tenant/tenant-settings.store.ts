import {withDevtools} from "@angular-architects/ngrx-toolkit";
import {computed, inject, Injectable} from '@angular/core';
import {
  AppLanguages,
  EinladungStatus,
  TenantSettingSaveJson,
  TenantSettingsJson,
  TenantSettingsService,
} from '@dvb/data-access-backend';
import {AppErrorHandlerService, Loader} from '@dvb/util-angular';
import {requireValue} from '@dvb/util-shared';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore, withState} from '@ngrx/signals';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, firstValueFrom, pipe} from 'rxjs';

type TenantSettingsState = {
  settings: TenantSettingsJson | undefined;
};

const initialState: TenantSettingsState = {
  settings: undefined,
};

@Injectable({providedIn: 'root'})
export class TenantSettingsStore extends signalStore(
  {protectedState: false},
  withDevtools('tenant-settings'),
  withState(initialState),
) {
  readonly tenantSettingsService = inject(TenantSettingsService);
  readonly appErrorHandlerService = inject(AppErrorHandlerService);
  readonly loader = new Loader();

  async init(): Promise<void> {
    return firstValueFrom(this.tenantSettingsService.getTenantSettings$())
      .then(tenantSettings => {
        console.log('tenantSettings loaded', tenantSettings);

        patchState(this, {settings: tenantSettings});
      })
      .catch(e => console.error(e));
  }

  updateTenantSettings = rxMethod<{
    tenantsettingSaveJson: TenantSettingSaveJson;
    onSuccess: () => void;
  }>(
    pipe(
      exhaustMap(params =>
        this.loader
          .tapLoading$(
            this.tenantSettingsService.updateTenantSettings$({
              tenantSettingSaveJson: params.tenantsettingSaveJson,
            }),
            error => this.appErrorHandlerService.handleError(error),
          )
          .pipe(
            tapResponse({
              next: json => {
                patchState(this, {settings: json});
                params.onSuccess();
              },
              error: console.error,
            }),
          ),
      ),
    ),
  );

  supportedLanguages = computed(() => {
    return requireValue(this.settings()).data.supportedLanguages;
  });

  supportsLanguageDe = computed(() => {
    return this.settings()?.data.supportedLanguages.includes(AppLanguages.DE);
  });

  supportsLanguageFr = computed(() => {
    return this.settings()?.data.supportedLanguages.includes(AppLanguages.FR);
  });

  agbResponsibility() {
    return this.settings()?.data?.agbResponsability;
  }

  availableEinladungStatus = computed(() => {
    return this.settings()?.data.einladungStatusValues ?? [];
  });

  idEsadIdVisible = computed(() => {
    return this.settings()?.data.features.esadIdFeature.enabledForStage;
  });

  isMobileScannerVisible = computed(() => {
    return this.settings()?.data.features.mobileScannerFeature.enabledForStage;
  });

  isMassenimportVisible = computed(() => {
    return this.settings()?.data.features.massenimportFeature.enabledForStage;
  });

  isMassenbriefVisible = computed(() => {
    return this.settings()?.data.features?.massenbriefFeature?.enabledForStage;
  });

  nichtGeprueftGrundEnabled = computed(() => {
    return this.settings()?.data.features.nichtGeprueftGrundFeature.enabledForStage;
  });

  arztListeEnabled = computed(() => {
    return this.settings()?.data.features.arztListeFeature.enabledForStage;
  });

  adHocImpfstoffChargenEnabled = computed(() => {
    return this.settings()?.data.features.adHocImpfstoffChargenFeature.enabledForStage;
  });

  footerEnabled = computed(() => {
    return this.settings()?.data.features.footerFeature.enabledForStage;
  });

  hasAbmeldungsEinladungsStatus = computed(() =>
    this.availableEinladungStatus().includes(EinladungStatus.TEILNAHME_ABGESAGT),
  );

  benutzerverwaltungInternEnabled = computed(() => {
    return this.settings()?.data.features.benutzerverwaltungInternFeature.enabledForStage;
  });

  zentraleOrganisationEnabled = computed(() => {
    return this.settings()?.data.features.zentraleOrganisationFeature.enabledForStage;
  });

  archivierungEnabled = computed(() => {
    return this.settings()?.data.features.archivierungFeature.enabledForStage;
  });

  postfixDeKey = computed(() => {
    const supportedLangs = this.supportedLanguages();
    if (supportedLangs && supportedLangs.length > 1) {
      return 'postfix.de';
    }
    return '';
  });

  postfixFrKey = computed(() => {
    const supportedLangs = this.supportedLanguages();
    if (supportedLangs && supportedLangs.length > 1) {
      return 'postfix.fr';
    }
    return '';
  });
}
