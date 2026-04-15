import {defineConfig, devices, PlaywrightTestConfig} from '@playwright/test';
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
    dependencies: // disable automatic dependencies for ui mode
      project.dependencies?.includes('anlass.create') ? ['setup'] :  project.dependencies,
  }))
};
export default defineConfig(config);
