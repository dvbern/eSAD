import {expect, test as setup} from '@playwright/test';
import {authStatePath, TestUserRole, testUsers} from '../../util/env.util';

/*
 * Resets the database to the seed data before the tests run, through the developer endpoint
 * DELETE /api/db. That endpoint is refused in production and unless app.allow-db-reset is set (it
 * is in the dev profile), and it always resets tenant BS - so this only runs against the local BS
 * stack. Set E2E_RESET_DB=false to keep the data of an earlier run, e.g. to inspect it.
 */
setup('reset the database to the seed data', async ({playwright, baseURL}) => {
  const {hostname} = new URL(baseURL ?? '');
  setup.skip(process.env['E2E_RESET_DB'] === 'false', 'E2E_RESET_DB=false');
  setup.skip(!hostname.endsWith('.localhost'), `${hostname} is not the local stack`);
  setup.skip(!hostname.startsWith('local-esc-bs.'), 'DELETE /api/db only resets tenant BS');
  setup.setTimeout(120_000);

  const apiAs = (storage: TestUserRole) =>
    playwright.request.newContext({baseURL, ignoreHTTPSErrors: true, storageState: authStatePath(storage)});

  const support = await apiAs('ESC_SUPPORT');
  try {
    const response = await support.delete('api/db', {timeout: 120_000});

    // eslint-disable-next-line playwright/no-standalone-expect -- setup assertion runs in a Playwright project setup hook
    expect(
      response.status(),
      `DELETE /api/db answered ${response.status()}: ${await response.text()}\n`
        + 'Is the API running with app.allow-db-reset=true? Skip the reset with E2E_RESET_DB=false.',
    ).toBe(204);
  } finally {
    await support.dispose();
  }

  // The reset also removes the Benutzer of the test accounts. The app creates a Benutzer on its
  // first request, and the parallel tests would all make that first request at once and collide on
  // the unique e-mail - so each account gets its Benutzer back here, one after the other.
  const configured = (Object.keys(testUsers) as TestUserRole[]).filter(storage => testUsers[storage].name);
  for (const storage of configured) {
    const api = await apiAs(storage);
    try {
      const response = await api.get('api/benutzer/self');

      // eslint-disable-next-line playwright/no-standalone-expect -- setup assertion runs in a Playwright project setup hook
      expect(response.status(), `GET /api/benutzer/self as ${storage}: ${await response.text()}`).toBe(200);
    } finally {
      await api.dispose();
    }
  }
});
