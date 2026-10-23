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
import {Path} from '../core/path';
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

  /**
   * Loads the tenant settings during app initialization.
   *
   * A failure must not abort the app bootstrap (the app can partially work with default settings),
   * but it must not be swallowed silently either: it is reported via the {@link AppErrorHandlerService}
   * (visible to the user and captured by Sentry).
   */
  async init(): Promise<void> {
    return firstValueFrom(this.tenantSettingsService.getTenantSettings$())
      .then(tenantSettings => {
        patchState(this, {settings: tenantSettings});
      })
      .catch(e => this.appErrorHandlerService.handleError(e));
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

  defaultLandingPage = computed<Path>(() => {
    const raw = this.settings()?.data.defaultLandingPage;
    return raw && raw in Path
      ? Path[raw as keyof typeof Path]
      : Path.DASHBOARD;
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

  // defensive copy + sort by `order`: the backend already sorts, but we must not rely on that.
  loginOptions = computed(() =>
    [...(this.settings()?.loginOptions ?? [])].sort((a, b) => a.order - b.order),
  );

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

  anonymeStatistikEnabled = computed(() => {
    return this.settings()?.data?.features.anonymeStatistikFeature.enabledForStage
  });

  nachkontrolleEnabled = computed(() => {
    return this.settings()?.data?.features.nachkontrolleFeature.enabledForStage
  });

  anlassSpracheSpalteEnabled = computed(() => {
    return this.settings()?.data?.features.anlassSpracheSpalteFeature.enabledForStage
  });

  anlassSpracheZusammenfassungEnabled = computed(() => {
    return this.settings()?.data?.features.anlassSpracheZusammenfassungFeature.enabledForStage
  });

  anlassInformationenEnabled = computed(() => {
    return this.settings()?.data?.features.anlassInformationenFeature.enabledForStage
  });

  anlassTitleRequiredEnabled = computed(() => {
    return this.settings()?.data?.features.anlassTitleRequiredFeature.enabledForStage
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
