const nx = require('@nx/eslint-plugin');
const baseConfig = require('../../../../eslint.config.js');

module.exports = [
  {
    ignores: ['src/lib/backend/openapi'],
  },

  // FIXME: we want to only ignore generated files, but I could not get this to work :(
  // ...baseConfig,
  // {
  //   files: ['**/*.json'],
  //   rules: {
  //     '@nx/dependency-checks': [
  //       'error',
  //       {ignoredFiles: ['{projectRoot}/eslint.config.{js,cjs,mjs}']},
  //     ],
  //   },
  //   languageOptions: {parser: require('jsonc-eslint-parser')},
  // },
  // ...nx.configs['flat/angular'],
  // ...nx.configs['flat/angular-template'],
  // {
  //   files: ['**/*.ts'],
  //   rules: {
  //     '@angular-eslint/directive-selector': [
  //       'error',
  //       {
  //         type: 'attribute',
  //         prefix: 'dacc',
  //         style: 'camelCase',
  //       },
  //     ],
  //     '@angular-eslint/component-selector': [
  //       'error',
  //       {
  //         type: 'element',
  //         prefix: 'dacc',
  //         style: 'kebab-case',
  //       },
  //     ],
  //   },
  // },
  // {
  //   files: ['**/*.html'],
  //   // Override or add rules here
  //   rules: {},
  // },
];
