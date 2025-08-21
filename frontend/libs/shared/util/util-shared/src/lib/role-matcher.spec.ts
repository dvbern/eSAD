/*
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

import { Berechtigung } from '@dvb/data-access-backend';
import { RoleMatcher, withAllOfBerechtigungen, withOneOfBerechtigungen, withOneOfMatchers } from './role-matcher';

describe('role-matcher', () => {
  describe('withOneOfMatchers', () => {
    // ARRANGE

    const roleMatchers: [RoleMatcher, ...RoleMatcher[]] = [
      withOneOfBerechtigungen(Berechtigung.SUPPORT),
      withAllOfBerechtigungen(Berechtigung.SYSTEM_ADMIN, Berechtigung.KOMPLETTER_EXPORT),
    ];

    // ARRANGE
    const tests: {
      matchers: [RoleMatcher, ...RoleMatcher[]];
      roles: Berechtigung[];
      expected: boolean;
      case: string;
    }[] = [
      {
        matchers: [() => false, () => false],
        expected: false,
        roles: [],
        case: 'all matchers resolve to false',
      },
      {
        matchers: [() => false, () => true],
        expected: true,
        roles: [],
        case: 'some matchers resolve to true',
      },
      {
        matchers: [() => true, () => true],
        expected: true,
        roles: [],
        case: 'all matchers resolve to true',
      },
      {
        matchers: [() => true, () => true],
        expected: true,
        roles: [],
        case: 'all matchers resolve to true',
      },
      {
        matchers: roleMatchers,
        expected: true,
        roles: [Berechtigung.SUPPORT, Berechtigung.SYSTEM_ADMIN, Berechtigung.KOMPLETTER_EXPORT],
        case: 'has rolles for all matchers',
      },
      {
        matchers: roleMatchers,
        expected: true,
        roles: [Berechtigung.SUPPORT],
        case: 'has rolles for one of matcher',
      },
      {
        matchers: roleMatchers,
        expected: true,
        roles: [Berechtigung.SYSTEM_ADMIN, Berechtigung.KOMPLETTER_EXPORT],
        case: 'has rolles for all of matcher',
      },
      {
        matchers: roleMatchers,
        expected: false,
        roles: [Berechtigung.KOMPLETTER_EXPORT],
        case: 'missing rolles for all of matcher',
      },
    ];

    it.each(tests)(
      'withOneOfMatchers should evaluate to $expected when $case and rolles = $roles',
      ({ matchers, roles, expected }) => {
        // ACT
        const actual = withOneOfMatchers(...matchers)(roles);

        // ASSERT
        expect(actual).toEqual(expected);
      },
    );
  });
});
