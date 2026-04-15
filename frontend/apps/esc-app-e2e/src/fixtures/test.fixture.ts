import { test as base } from '@playwright/test';
import { testData } from '../util/test-data.util';

export const test = base.extend<{
  testData: typeof testData;
}>({
  testData: async ({ page: _page }, use) => {
    await use(testData);
  },
});

export { expect } from '@playwright/test';
