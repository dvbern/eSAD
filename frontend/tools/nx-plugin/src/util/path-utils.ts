import {Tree} from '@nx/devkit';

// FIXME: why does this not work when converting to an import, e.g.:
//  'import path from 'node:path';' ???
// noinspection ES6ConvertRequireIntoImport
const path = require('node:path');

/**
 * @param targetDir '.' for current working directory, or a relative or absolute path
 */
export function pathRelativeToWorkspace(tree: Tree, targetDir: '.' | string): string {
  if (targetDir === '.') {
    return path.relative(tree.root, process.cwd());
  }

  if (path.isAbsolute(targetDir)) {
    return path.relative(tree.root, targetDir);
  }

  return path.relative(tree.root, path.resolve(process.cwd(), targetDir));
}
