const nx = require('@nx/eslint-plugin');
module.exports = [
  // see:
  // https://github.com/nrwl/nx/blob/master/packages/eslint-plugin/src/flat-configs/base.ts
  ...nx.configs['flat/base'],
  // see:
  // https://github.com/nrwl/nx/blob/master/packages/eslint-plugin/src/flat-configs/typescript.ts
  ...nx.configs['flat/typescript'],
  // see https://github.com/nrwl/nx/blob/master/packages/eslint-plugin/src/flat-configs/javascript.ts
  ...nx.configs['flat/javascript'],
  // see:
  // https://github.com/nrwl/nx/blob/master/packages/eslint-plugin/src/flat-configs/angular.ts
  // https://github.com/angular-eslint/angular-eslint/blob/main/packages/angular-eslint/src/configs/ts-recommended.ts
  ...nx.configs['flat/angular'],
  // see:
  // https://github.com/nrwl/nx/blob/master/packages/eslint-plugin/src/flat-configs/angular-template.ts
  // https://github.com/angular-eslint/angular-eslint/blob/main/packages/angular-eslint/src/configs/template-recommended.ts
  // https://github.com/angular-eslint/angular-eslint/blob/main/packages/angular-eslint/src/configs/template-accessibility.ts
  ...nx.configs['flat/angular-template'],
  {
    ignores: ['**/dist'],
  },
  {
    files: ['**/*.ts', '**/*.tsx', '**/*.js', '**/*.jsx'],
    rules: {
      '@angular-eslint/prefer-on-push-component-change-detection': ['error'],
      '@nx/enforce-module-boundaries': [
        'error',
        {
          enforceBuildableLibDependency: true,
          allow: ['^.*/eslint(\\.base)?\\.config\\.[cm]?js$'],
          depConstraints: [
            {
              sourceTag: 'type:feat',
              onlyDependOnLibsWithTags: ['type:feat', 'type:ui', 'type:data-access', 'type:util', 'generated:api'],
            },
            {
              sourceTag: 'type:ui',
              onlyDependOnLibsWithTags: ['type:ui', 'type:util'],
            },
            {
              sourceTag: 'type:data-access',
              onlyDependOnLibsWithTags: ['type:data-access', 'type:util', 'generated:api'],
            },
            {
              sourceTag: 'type:util',
              onlyDependOnLibsWithTags: ['type:util', 'generated:api'],
            },
            {
              sourceTag: 'scope:*-app',
              onlyDependOnLibsWithTags: ['scope:*-app', 'scope:shared'],
            },
          ],
        },
      ],
      '@typescript-eslint/no-unused-vars': [
        'warn',
        {
          argsIgnorePattern: '^_',
          varsIgnorePattern: '^_',
          caughtErrorsIgnorePattern: '^_',
        },
      ],
    },
  },
  {
    files: ['**/*.json'],
    rules: {
      '@nx/dependency-checks': ['error', { ignoredFiles: ['{projectRoot}/eslint.config.{js,cjs,mjs}'] }],
    },
    languageOptions: { parser: require('jsonc-eslint-parser') },
  },

];
