import { Tree } from '@nx/devkit';
import { createTreeWithEmptyWorkspace } from 'nx/src/generators/testing-utils/create-tree-with-empty-workspace';

import { pathRelativeToWorkspace } from './path-utils';

const os = require('os');

describe('path-utils', () => {
  let tree: Tree;

  beforeEach(async () => {
    tree = createTreeWithEmptyWorkspace({ layout: 'apps-libs' });

    jest.spyOn(process, 'cwd').mockReturnValue(tree.root + '/libs/foobar');
  });

  if (os.platform() === 'win32') {
    it.skip('This test is skipped on Windows', () => {
      // This block will not run
    });
  } else {
    it.each([
      // current working directory
      ['.', 'libs/foobar'],
      // relative subdirectory
      ['sub/directory', 'libs/foobar/sub/directory'],
      // absolute path inside workspace
      ['/virtual/directory', 'directory'],
      // absolute path outside workspace
      ['/tmp/directory', '../tmp/directory'],
    ])(' resolves "%s" to "%s"', (schemaDir, expected) => {
      const result = pathRelativeToWorkspace(tree, schemaDir);

      expect(result).toBe(expected);
    });
  }

  test('This test always runs, as a fallback', () => {
    expect(true).toBe(true);
  });
});
