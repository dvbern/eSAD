/*
 *
 * Copyright (C) 2025 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {Berechtigung} from '@dvb/data-access-backend';
import {
  AtLeastOneBerechtigung,
  ensureType,
  withAllOfBerechtigungen,
  withAnyBerechtigung,
  withOneOfBerechtigungen,
} from '@dvb/util-shared';

describe('RequireRoleAuthGuard', () => {

  describe('requireRole', () => {
    describe('withAnyBerechtigung', () => {
      it.each(ensureType<Berechtigung[][]>([
        [],
        [Berechtigung.KINDER_SCHREIBEN_MEDIZINISCH],
        [Berechtigung.KINDER_LESEN_MEDIZINISCH],
        [Berechtigung.KINDER_SCHREIBEN_MEDIZINISCH, Berechtigung.KINDER_LESEN_MEDIZINISCH],
      ]))('returns true for any berechtigung: %p', (...actualRoles) => {
        const actual = withAnyBerechtigung()(actualRoles);

        expect(actual).toBe(true);
      });
    });

    describe('withAllOfBerechtigungen', () => {
      it.each(ensureType<[AtLeastOneBerechtigung, Berechtigung[]][]>([
        [[Berechtigung.KINDER_SCHREIBEN_MEDIZINISCH], [Berechtigung.KINDER_SCHREIBEN_MEDIZINISCH, Berechtigung.KINDER_LESEN_MEDIZINISCH]],
        [[Berechtigung.KINDER_SCHREIBEN_MEDIZINISCH, Berechtigung.KINDER_LESEN_MEDIZINISCH], [Berechtigung.KINDER_SCHREIBEN_MEDIZINISCH, Berechtigung.KINDER_LESEN_MEDIZINISCH]],
      ]))('returns true if user has all berechtigungen: %p (actualRoles: %p)', (requiredRoles, actualRoles) => {
        const actual = withAllOfBerechtigungen(...requiredRoles)(actualRoles);

        expect(actual).toBe(true);
      });

      it.each(ensureType<[AtLeastOneBerechtigung, Berechtigung[]][]>([
        [[Berechtigung.KINDER_SCHREIBEN_MEDIZINISCH], []],
        [[Berechtigung.KINDER_SCHREIBEN_MEDIZINISCH], [Berechtigung.KINDER_LESEN_MEDIZINISCH]],
        [[Berechtigung.KINDER_SCHREIBEN_MEDIZINISCH, Berechtigung.KINDER_LESEN_MEDIZINISCH], []],
        [[Berechtigung.KINDER_SCHREIBEN_MEDIZINISCH, Berechtigung.KINDER_LESEN_MEDIZINISCH], [Berechtigung.KINDER_LESEN_MEDIZINISCH]],
        [[Berechtigung.KINDER_SCHREIBEN_MEDIZINISCH, Berechtigung.KINDER_LESEN_MEDIZINISCH], [Berechtigung.KINDER_SCHREIBEN_MEDIZINISCH]],
      ]))('returns false if user does not have all berechtigungen: %p (actualRoles: %p)', (requiredRoles, actualRoles) => {
        const actual = withAllOfBerechtigungen(...requiredRoles)(actualRoles);

        expect(actual).toBe(false);
      });
    });

    describe('withOneOfBerechtigungen', () => {
      it.each(ensureType<[AtLeastOneBerechtigung, Berechtigung[]][]>([
        [[Berechtigung.KINDER_SCHREIBEN_MEDIZINISCH], [Berechtigung.KINDER_SCHREIBEN_MEDIZINISCH, Berechtigung.KINDER_LESEN_MEDIZINISCH]],
        [[Berechtigung.KINDER_SCHREIBEN_MEDIZINISCH, Berechtigung.KINDER_LESEN_MEDIZINISCH], [Berechtigung.KINDER_SCHREIBEN_MEDIZINISCH, Berechtigung.KINDER_LESEN_MEDIZINISCH]],
        [[Berechtigung.KINDER_SCHREIBEN_MEDIZINISCH], [Berechtigung.KINDER_SCHREIBEN_MEDIZINISCH]],
        [[Berechtigung.KINDER_SCHREIBEN_MEDIZINISCH, Berechtigung.KINDER_LESEN_MEDIZINISCH], [Berechtigung.KINDER_SCHREIBEN_MEDIZINISCH]],
        [[Berechtigung.KINDER_SCHREIBEN_MEDIZINISCH, Berechtigung.KINDER_LESEN_MEDIZINISCH], [Berechtigung.KINDER_LESEN_MEDIZINISCH]],
      ]))('returns true if user has at least one berechtigung: %p (actualRoles: %p)', (requiredRoles, actualRoles) => {
        const actual = withOneOfBerechtigungen(...requiredRoles)(actualRoles);

        expect(actual).toBe(true);
      });

      it.each(ensureType<[AtLeastOneBerechtigung, Berechtigung[]][]>([
        [[Berechtigung.KINDER_SCHREIBEN_MEDIZINISCH], []],
        [[Berechtigung.KINDER_SCHREIBEN_MEDIZINISCH, Berechtigung.KINDER_LESEN_MEDIZINISCH], []],
      ]))('returns false if user does not have any berechtigung: %p (actualRoles: %p)', (requiredRoles, actualRoles) => {
        const actual = withOneOfBerechtigungen(...requiredRoles)(actualRoles);

        expect(actual).toBe(false);
      });
    });
  });
});
