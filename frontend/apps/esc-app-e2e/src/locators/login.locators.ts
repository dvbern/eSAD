import {Locator, Page} from '@playwright/test';

/*
 * Keycloak renders the login page with whichever theme the realm is configured with, and its
 * visible labels follow the realm locale. Anchoring on the form element ids instead of on German
 * labels keeps the login routine working across theme, Keycloak version and locale changes.
 */

export function getInputUsername(page: Page): Locator {
  return page.locator('#username');
}

export function getInputPassword(page: Page): Locator {
  return page.locator('#password');
}

export function getButtonLogin(page: Page): Locator {
  return page.locator('#kc-login');
}
