import {inject} from '@angular/core';
import {CanDeactivateFn, GuardResult} from '@angular/router';
import {DirtyGuardService} from './dirty-guard.service';
import {isDirtyMarker} from './dirty-marker';

export function confirmOnDirty(): CanDeactivateFn<unknown> {
  return async (component: unknown): Promise<GuardResult> => {
    if (!isDirtyMarker(component)) {
      throw new Error(`Component does not implement DirtyGuard: ${typeof component}/${component?.constructor?.name}`);
    }

    if (!component.isDirty()) {
      return true;
    }

    const service = inject(DirtyGuardService);

    return await service.askForNavigationOverride();
  }
}
