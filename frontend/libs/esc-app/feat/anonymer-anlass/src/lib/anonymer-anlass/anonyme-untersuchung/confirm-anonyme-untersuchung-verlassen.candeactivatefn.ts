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

import {inject} from '@angular/core';
import {CanDeactivateFn, GuardResult} from '@angular/router';
import {ConfirmService, Path} from '@dvb/feat-shared';
import {TranslocoService} from '@jsverse/transloco';
import {firstValueFrom} from 'rxjs';
import {AnonymeUntersuchungComponent} from './anonyme-untersuchung.component';
import {confirmOnDirty} from "@dvb/util-angular";

/**
 * CanDeactivate-Guard fuer die anonyme Untersuchung.
 *
 * Wenn die Untersuchung noch nicht abgeschlossen ist, wird der Benutzer mit einer
 * fallspezifischen Meldung gefragt, ob er die Seite wirklich verlassen möchte.
 * Im Gegensatz zum generischen {@link confirmOnDirty}-Guard geht es hier nicht
 * um ungespeicherte Formularänderungen, sondern um den Geschäftsstatus
 * "Untersuchung noch nicht abgeschlossen".
 */
export function confirmAnonymeUntersuchungVerlassen(): CanDeactivateFn<AnonymeUntersuchungComponent> {
  return async (component): Promise<GuardResult> => {
    if (component.isUntersuchungAbgeschlossen()) {
      return true;
    }

    const confirmService = inject(ConfirmService);
    const transloco = inject(TranslocoService);

    return await firstValueFrom(confirmService.makeDecision({
      question: transloco.translate('untersuchung.anonym.verlassen.question'),
      consequence: transloco.translate('untersuchung.anonym.verlassen.consequence'),
      decision: transloco.translate('untersuchung.anonym.verlassen.decision'),
    }));
  };
}


/*
 * prioritize confirmAnonymeUntersuchungVerlassen when completely leaving anonyme untersuchung
 * ignoring the form state of the current tab if not navigating between tabs
 */
export function confirmOnDirtyBetweenUntersuchungsTabs(): CanDeactivateFn<unknown> {
  return (component, route, state, nextState) => {
    if (!nextState.url.includes(Path.ANONYM_UNTERSUCHUNG)) {
      return true;
    }
    return confirmOnDirty()(component, route, state, nextState);
  };
}
