import { Injectable } from '@angular/core';
import { AppValidationErrorJson, ViolationJson } from '@dvb/data-access-backend';
import { ISODateString } from '@dvb/data-access-types';
import { ensureProps } from '@dvb/util-shared';
import { delay, Observable, of, throwError } from 'rxjs';

// FIXME: starter: sollte eigentlich alles vom Backend kommen, aber fuer Demo-Zwecke langts

export type Geschlecht = 'MANN' | 'FRAU' | 'SONSTIGES';
export const Geschlecht: Record<Geschlecht, Geschlecht> = {
  MANN: 'MANN',
  FRAU: 'FRAU',
  SONSTIGES: 'SONSTIGES',
} as const;

export interface MyStammdatenDTO {
  name: string;
  vorname: string;
  geburtsDatum?: ISODateString | undefined;
  geschlecht: Geschlecht;
}

@Injectable({ providedIn: 'root' })
export class FakeStammdatenService {
  loadById$(id: string): Observable<MyStammdatenDTO> {
    return of(
      ensureProps<MyStammdatenDTO>({
        name: 'Mustermann',
        vorname: `Max (${id})`,
        geburtsDatum: '2000-01-01',
        geschlecht: 'MANN',
      }),
    ).pipe(delay(300));
  }

  /**
   * Simulates varios error conditions:
   * <ul>
   *   <li>Every 3rd call produces an internal server error</li>
   *   <li>Name of length 1 produces a field validation error on &quot;name&quot;</li>
   *   <li>Invalid Geschlecht produces a form validation error</li>
   * </ul>
   */
  update$(stammdaten: MyStammdatenDTO): Observable<void> {
    // console.log('FakeStammdatenService.update: ', stammdaten);
    if (++submitFailureCounter % 3 === 0) {
      return throwError(() => new Error('An error has occurred'));
    }

    const violations: ViolationJson[] = [];
    if (stammdaten.name.length <= 1) {
      violations.push(
        ensureProps<ViolationJson>({
          path: 'name',
          key: 'size',
          message: 'Name muss mindestens 2 Zeichen lang sein',
        }),
      );
    }

    if (!Object.values(Geschlecht).includes(stammdaten.geschlecht)) {
      violations.push(
        ensureProps<ViolationJson>({
          path: '',
          key: 'invalid_geschlecht',
          message: 'Geschlecht ist ungültig: ' + stammdaten.geschlecht,
        }),
      );
    }
    if (violations.length > 0) {
      return throwError(() =>
        ensureProps<AppValidationErrorJson>({
          type: 'VALIDATION',
          violations,
        }),
      );
    }

    // simulate normal empty response
    return of(undefined).pipe(delay(300));
  }
}

let submitFailureCounter = 0;
