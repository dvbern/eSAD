import { defineConfig } from 'vitest/config';

export default defineConfig(() => ({
  root: import.meta.dirname,
  cacheDir: '../../node_modules/.vite/tools/nx-plugin',
  test: {
    name: 'nx-plugin',
    watch: false,
    globals: true,
    environment: 'node',
    include: ['src/**/*.{test,spec}.ts'],
    reporters: ['default'],
    coverage: {
      enabled: true,
      reportsDirectory: '../../coverage/tools/nx-plugin',
      provider: 'v8' as const,
      reporter: ['lcov'],
    },
  },
}));
