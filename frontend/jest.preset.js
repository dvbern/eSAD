const nxPreset = require('@nx/jest/preset').default;

module.exports = {
  ...nxPreset,
  coverageReporters: ['lcov'],
  testTimeout: process.env['CI'] ? 20000 : 10000,
  moduleNameMapper: {
    ...nxPreset.moduleNameMapper,
    // 2024-04-22: required to make transloco work in tests
    // see:https://github.com/jsverse/transloco/issues/714#issuecomment-1896184902
    '^flat': 'node_modules/flat/index.js',
  },
};
