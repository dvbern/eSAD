import {defineConfig, devices, PlaywrightTestConfig} from '@playwright/test';

/**
 * See https://playwright.dev/docs/test-configuration.
 */

const baseURL = process.env['BASE_URL'] || 'https://local-esc-bs.localhost:4443/';
const config: PlaywrightTestConfig = {
  testDir: './src/e2e',
  fullyParallel: true,
  forbidOnly: !!process.env['CI'],
  retries: process.env['CI'] ? 2 : 0,
  workers: process.env['CI'] ? 1 : undefined,
  reporter: 'html',
  use: {
    baseURL,
    trace: 'on-first-retry',
    testIdAttribute: 'data-testid',
    ignoreHTTPSErrors: true,
  },

  projects: [
    {
      name: 'setup',
      testDir: './src/e2e/setup',
      testMatch: '**/*.setup.ts',
    },
    {
      name: 'admin',
      testDir: './src/e2e/admin',
      use: {...devices['Desktop Chrome']},
      dependencies: ['setup'],
    },
    {
      name: 'anlass',
      testDir: './src/e2e/anlass',
      use: {...devices['Desktop Chrome']},
      dependencies: ['setup'],
    },
    {
      name: 'e2e-full',
      testDir: './src/e2e',
      testMatch: '**/*.e2e.spec.ts',
      dependencies: ['setup'],
    },
  ],
};
export default defineConfig(config);
