import {devices, PlaywrightTestConfig} from '@playwright/test';

/**
 * See https://playwright.dev/docs/test-configuration.
 */

const baseURL = process.env['BASE_URL'] || 'https://local-esc-bs.localhost:4443/';

export const baseProjects: NonNullable<PlaywrightTestConfig['projects']> = [
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
    name: 'anlass.create',
    testDir: './src/e2e/anlass',
    testMatch: '*-create.e2e.spec.ts',
    use: {...devices['Desktop Chrome'], locale: 'de-CH'},
    dependencies: ['setup'],
  },
  {
    name: 'anlass',
    testDir: './src/e2e/anlass',
    testMatch: '*.e2e.spec.ts',
    testIgnore: '*-create.e2e.spec.ts',
    use: {...devices['Desktop Chrome'], locale: 'de-CH'},
    dependencies: ['anlass.create'],
  },
  {
    name: 'untersuchung',
    testDir: './src/e2e/untersuchung',
    use: {...devices['Desktop Chrome'], locale: 'de-CH'},
    dependencies: ['anlass.create'],
  },
  {
    name: 'notizen',
    testDir: './src/e2e/notizen',
    use: {...devices['Desktop Chrome'], locale: 'de-CH'},
    dependencies: ['setup'],
  }
];


export const baseConfig: PlaywrightTestConfig = {
  testDir: './src/e2e',
  fullyParallel: true,
  forbidOnly: !!process.env['CI'],
  retries: process.env['CI'] ? 2 : 0,
  workers: process.env['CI'] ? 5 : undefined,
  reporter: 'html',
  use: {
    baseURL,
    trace: 'on-first-retry',
    testIdAttribute: 'data-testid',
    ignoreHTTPSErrors: true,
    locale: 'de-CH',
  },
  projects: baseProjects
};
