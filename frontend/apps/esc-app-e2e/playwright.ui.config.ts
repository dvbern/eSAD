import {defineConfig, PlaywrightTestConfig} from '@playwright/test';
import {baseConfig, baseProjects} from "./playwright.base.conf";

/**
 * See https://playwright.dev/docs/test-configuration.
 */

const config: PlaywrightTestConfig = {
  ...baseConfig,
  fullyParallel: false,
  retries: 0,
  projects: baseProjects.map(project => ({
    ...project,
    // disable automatic dependencies for ui mode: re-running a single test must neither recreate its
    // Anlass nor wipe the database it works on (the db-reset project can still be run on its own)
    dependencies: project.dependencies?.some(dependency => ['anlass.create', 'db-reset'].includes(dependency))
      ? ['setup']
      : project.dependencies,
  }))
};
export default defineConfig(config);
