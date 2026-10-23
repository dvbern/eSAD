import {expect, Response, test as base} from '@playwright/test';
import {testData} from '../util/test-data.util';

export const test = base.extend<{
  testData: typeof testData;
  failOnServerErrors: void;
}>({
  testData: async ({page: _page}, use) => {
    await use(testData);
  },

  /**
   * Fails the test when the API answers with a server error. Left alone, such an error shows up
   * only as some UI assertion timing out further down, with the cause buried in the API log - the
   * message carries the error id the API logged, so it can be looked up there.
   */
  failOnServerErrors: [async ({context}, use) => {
    const serverErrors: Promise<string>[] = [];
    context.on('response', response => {
      if (response.status() >= 500 && new URL(response.url()).pathname.startsWith('/api/')) {
        serverErrors.push(describeServerError(response));
      }
    });

    await use();

    // eslint-disable-next-line playwright/no-standalone-expect -- fixture assertion runs after the test lifecycle
    expect(await Promise.all(serverErrors), 'The API answered with a server error').toEqual([]);
  }, {auto: true}],
});

async function describeServerError(response: Response): Promise<string> {
  const body = await response.text().catch(() => '(body no longer available)');

  return `${response.request().method()} ${response.url()} -> ${response.status()}: ${body.slice(0, 300)}`;
}

export {expect} from '@playwright/test';
