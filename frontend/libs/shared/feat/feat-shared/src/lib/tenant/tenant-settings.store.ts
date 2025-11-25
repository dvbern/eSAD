import {computed, inject, Injectable} from '@angular/core';
import {
  AppLanguages, EinladungStatus,
  TenantSettingSaveJson,
  TenantSettingsJson,
  TenantSettingsService,
} from '@dvb/data-access-backend';
import {AppErrorHandlerService, AppNotificationStore, Loader} from '@dvb/util-angular';
import {requireValue} from '@dvb/util-shared';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore, withComputed, withState} from '@ngrx/signals';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, pipe, switchMap, tap} from 'rxjs';

type TenantSettingsState = {
  settings: TenantSettingsJson | undefined;
};

const initialState: TenantSettingsState = {
  settings: undefined,
};

@Injectable({providedIn: 'root'})
export class TenantSettingsStore extends signalStore(
  {protectedState: false},
  // withDevtools('tenant-settings'),
  withState(initialState),
) {
  readonly tenantSettingsService = inject(TenantSettingsService);
  readonly appErrorHandlerService = inject(AppErrorHandlerService);
  readonly loader = new Loader();

  loadTenantSettings = rxMethod<void>(
    pipe(
      tap(() => patchState(this, {settings: undefined})),
      switchMap(() =>
        this.loader.tapLoading$(this.tenantSettingsService.getTenantSettings$(), error =>
          this.appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: json => patchState(this, {settings: json}),
        error: console.error,
      }),
    ),
  );

  updateTenantSettings = rxMethod<{
    tenantsettingSaveJson: TenantSettingSaveJson;
    onSuccess: () => void;
  }>(
    pipe(
      exhaustMap(params =>
        this.loader.tapLoading$(
          this.tenantSettingsService.updateTenantSettings$({
            tenantSettingSaveJson: params.tenantsettingSaveJson,
          }),
          error => this.appErrorHandlerService.handleError(error),
        ).pipe(
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

  availableEinladungStatus = computed(() => {
    return this.settings()?.data.einladungStatusValues ?? [];
  });

  hasAbmeldungsEinladungsStatus = computed(() => this.availableEinladungStatus()
    .includes(EinladungStatus.TEILNAHME_ABGESAGT));

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
