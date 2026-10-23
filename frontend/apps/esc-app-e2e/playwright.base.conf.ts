import {devices, PlaywrightTestConfig} from '@playwright/test';
import {baseURL} from './src/util/env.util';

/**
 * See https://playwright.dev/docs/test-configuration.
 */

export const baseProjects: NonNullable<PlaywrightTestConfig['projects']> = [
  {
    name: 'setup',
    testDir: './src/e2e/setup',
    testMatch: 'auth.setup.ts',
  },
  {
    // Every run starts from the seed data, so it cannot trip over what an earlier run left behind.
    name: 'db-reset',
    testDir: './src/e2e/setup',
    testMatch: 'db-reset.setup.ts',
    dependencies: ['setup'],
  },
  {
    name: 'admin',
    testDir: './src/e2e/admin',
    use: {...devices['Desktop Chrome']},
    dependencies: ['db-reset'],
  },
  {
    name: 'anlass.create',
    testDir: './src/e2e/anlass',
    testMatch: '*-create.e2e.spec.ts',
    use: {...devices['Desktop Chrome'], locale: 'de-CH'},
    dependencies: ['db-reset'],
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
    dependencies: ['db-reset'],
  },
  {
    name: 'kinder',
    testDir: './src/e2e/kinder',
    use: {...devices['Desktop Chrome'], locale: 'de-CH'},
    dependencies: ['db-reset'],
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
    // 'on-first-retry' never records anything without retries, which is how the suite runs locally.
    trace: 'retain-on-failure',
    testIdAttribute: 'data-testid',
    ignoreHTTPSErrors: true,
    locale: 'de-CH',
  },
  projects: baseProjects
};
