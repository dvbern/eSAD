import {expect, Page} from '@playwright/test';
import {getButtonLogin, getInputPassword, getInputUsername} from '../locators/login.locators';

export async function loginAsUser(page: Page, user: { name?: string, password?: string }) {
  if (!user.name || !user.password) {
    throw new Error(
      'Missing e2e credentials: for the local stack run scripts/provision-local-users.mjs, '
        + 'otherwise copy src/util/.env-template to src/util/.env and fill in the users.',
    );
  }

  await page.goto('');
  await expect(getInputUsername(page)).toBeVisible();

  await getInputUsername(page).fill(user.name);
  await getInputPassword(page).fill(user.password);
  await getButtonLogin(page).click();

  await acceptAgbIfAsked(page);

  // The dashboard is the first authenticated page. Waiting for one of its headings - rather than
  // for the app shell - makes a failed login fail here instead of somewhere deep inside a test.
  await expect(page.getByRole('heading', {name: 'Zuletzt geöffnete Kinder'})).toBeVisible({timeout: 30000});
}

/**
 * Tenants with AGB_MANAGED_BY_USER (BE) stop every user on the AGB page until they have accepted once. That
 * is a one-time step per user, not what any test is about, so the login takes it.
 */
async function acceptAgbIfAsked(page: Page) {
  const agbPage = page.locator('feat-agb-page');
  const dashboard = page.getByRole('heading', {name: 'Zuletzt geöffnete Kinder'});
  await expect(agbPage.or(dashboard)).toBeVisible({timeout: 30000});
  if (!(await agbPage.isVisible())) {
    return;
  }
  await agbPage.locator('input[type=checkbox]').check();
  // the page header's user menu is a button too
  await agbPage.locator('button[svuibutton=primary]').click();
  // a user who may edit their Organisation lands on it after accepting; the dashboard is one click away
  await expect(agbPage).toBeHidden();
  if (!(await dashboard.isVisible())) {
    await page.goto('');
  }
}
