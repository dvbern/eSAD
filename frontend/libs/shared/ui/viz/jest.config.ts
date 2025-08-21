// Diese Zeile war noetig, damit die jest-Tests laufen.
const esModules = ['@angular', 'd3', 'd3-array', 'd3-delaunay', 'jest-runtime', 'internmap', 'delaunator', 'robust-predicates'];

export default {
  displayName: 'viz',
  preset: '../../../../jest.preset.js',
  setupFilesAfterEnv: ['<rootDir>/src/test-setup.ts'],
  coverageDirectory: '../../../../coverage/libs/shared/ui/viz',
  transform: {
    '^.+\\.(ts|mjs|js|html)$': [
      'jest-preset-angular',
      {
        tsconfig: '<rootDir>/tsconfig.spec.json',
        stringifyContentPathRegex: '\\.(html|svg)$',
        isolatedModules: true
      },
    ],
  },
  transformIgnorePatterns: [
    // Diese Zeile war noetig, damit die jest-Tests laufen.
    `/node_modules/(?!.*\\.mjs$|${esModules.join('|')})`,
  ],
  snapshotSerializers: [
    'jest-preset-angular/build/serializers/no-ng-attributes',
    'jest-preset-angular/build/serializers/ng-snapshot',
    'jest-preset-angular/build/serializers/html-comment',
  ],
};
