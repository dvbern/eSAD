import {Locator, Page} from '@playwright/test';

export function getInputUsername(page: Page): Locator {
  return page.getByLabel('E-Mail bzw. Benutzername');
}

export function getInputPassword(page: Page): Locator {
  return page.getByLabel('Passwort');
}

export function getButtonLogin(page: Page): Locator {
  return page.getByRole('button', {name: 'Anmelden'})
}
