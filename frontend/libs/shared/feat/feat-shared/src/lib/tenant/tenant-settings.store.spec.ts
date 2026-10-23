import {TestBed} from '@angular/core/testing';
import {TenantEnum, TenantSettingsJson, TenantSettingsService} from '@dvb/data-access-backend';
import {AppErrorHandlerService} from '@dvb/util-angular';
import {of, throwError} from 'rxjs';
import type {Mock} from 'vitest';
import {TenantSettingsStore} from './tenant-settings.store';

describe('TenantSettingsStore', () => {
  let store: TenantSettingsStore;
  let tenantSettingsService: {getTenantSettings$: Mock};
  let appErrorHandlerService: {handleError: Mock};

  beforeEach(() => {
    tenantSettingsService = {getTenantSettings$: vi.fn()};
    appErrorHandlerService = {handleError: vi.fn()};

    TestBed.configureTestingModule({
      providers: [
        TenantSettingsStore,
        {provide: TenantSettingsService, useValue: tenantSettingsService},
        {provide: AppErrorHandlerService, useValue: appErrorHandlerService},
      ],
    });

    store = TestBed.inject(TenantSettingsStore);
  });

  describe('init', () => {
    it('should patch the state with the loaded tenant settings', async () => {
      const settings = {tenant: TenantEnum.BE} as TenantSettingsJson;
      tenantSettingsService.getTenantSettings$.mockReturnValue(of(settings));

      await store.init();

      expect(store.settings()).toBe(settings);
      expect(appErrorHandlerService.handleError).not.toHaveBeenCalled();
    });

    it('should report a failure via the AppErrorHandlerService without rejecting', async () => {
      const error = new Error('backend unavailable');
      tenantSettingsService.getTenantSettings$.mockReturnValue(throwError(() => error));

      await expect(store.init()).resolves.toBeUndefined();

      expect(store.settings()).toBeUndefined();
      expect(appErrorHandlerService.handleError).toHaveBeenCalledTimes(1);
      expect(appErrorHandlerService.handleError).toHaveBeenCalledWith(error);
    });
  });
});
