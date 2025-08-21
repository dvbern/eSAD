import {expect, Page} from '@playwright/test';
import {getButtonLogin, getInputPassword, getInputUsername} from '../locators/login.locators';

export async function loginAsUser(page: Page, user: { name?: string, password?: string }) {
  // Go to the login page and change the language to German
  await page.goto('');
  await page.getByRole('link', {name: 'DE'}).click();
  // Assert the title
  await expect(page.getByRole('heading', {name: 'Anmelden'})).toBeVisible();
  // Login
  await getInputUsername(page).fill(user.name ?? '');
  await getInputPassword(page).fill(user.password ?? '');
  await getButtonLogin(page).click();
  // Assert the title
  await expect(page).toHaveTitle('eSchool');
  await expect(page.getByRole('heading', {name: 'Kinder'})).toBeVisible({timeout: 30000});
}

