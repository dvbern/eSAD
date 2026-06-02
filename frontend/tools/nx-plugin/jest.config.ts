module.exports = {
  displayName: 'nx-plugin',
  preset: '../../jest.preset.js',
  transform: {
    '^.+\\.[tj]s$': ['ts-jest', { tsconfig: '<rootDir>/tsconfig.spec.json' }],
  },
  moduleFileExtensions: ['ts', 'js', 'html'],
  coverageDirectory: '../../coverage/tools/nx-plugin',
  transformIgnorePatterns: ['node_modules/(?!@angular|@ngrx|(?!.*\\\\.mjs$))'],
};
