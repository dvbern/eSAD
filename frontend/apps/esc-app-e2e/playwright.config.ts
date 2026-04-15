import {defineConfig, devices, PlaywrightTestConfig} from '@playwright/test';
import {baseConfig} from "./playwright.base.conf";

/**
 * See https://playwright.dev/docs/test-configuration.
 */

export default defineConfig(baseConfig);
