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
import {test as setup} from '../../fixtures/test.fixture';
import {loginAsUser} from '../../routines/login.routines';
import {authStatePath, TestUserRole, testUsers} from '../../util/env.util';

setup.describe.configure({ mode: 'parallel' });

type TestUser = (typeof testUsers)[keyof typeof testUsers];

/*
 * One login per distinct account, not per storage state. A local .env may well point several of the
 * ESC_* slots at the same account, and logging that account in from several browsers at once makes
 * Keycloak reject all but one of them ("Invalid username or password").
 */
const loginsByAccount = new Map<string, { user: TestUser; storages: TestUserRole[] }>();
// a tenant does not need every role (BE has no Admin with Untersuchung rights), so only configured users log in
(Object.entries(testUsers) as [TestUserRole, TestUser][]).filter(([, user]) => user.name).forEach(([storage, user]) => {
  const account = user.name ?? storage;
  const login = loginsByAccount.get(account) ?? {user, storages: []};

  login.storages.push(storage);
  loginsByAccount.set(account, login);
});

loginsByAccount.forEach(({user, storages}) => {
  setup(`Create ${storages.join(' + ')} Auth`, async ({page, context}) => {
    await loginAsUser(page, user);

    for (const storage of storages) {
      await context.storageState({path: authStatePath(storage)});
    }
  });
});
