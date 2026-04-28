/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { TenantSettingsStore } from './tenant-settings.store';
import { Path } from '../core/path';

/**
 * Guard that ensures the "Anonyme Statistik" feature is enabled in the tenant settings.
 * If disabled, it redirects to the default landing page (Dashboard).
 */
export const requireStatistikEnabled: CanActivateFn = () => {
  const store = inject(TenantSettingsStore);
  const router = inject(Router);

  const isEnabled = store.anonymeStatistikEnabled();

  if (!isEnabled) {
    console.warn('Statistik is disabled, redirecting to default landing page');
    return router.createUrlTree([Path.DASHBOARD]);
  }

  return true;
};
