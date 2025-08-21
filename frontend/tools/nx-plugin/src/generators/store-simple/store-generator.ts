import {formatFiles, generateFiles, joinPathFragments, names, Tree} from '@nx/devkit';
import {pathRelativeToWorkspace} from '../../util/path-utils';
import {StoreGeneratorSchema} from './store-generator-schema';

// noinspection JSUnusedGlobalSymbols
export default async function storeGenerator(tree: Tree, schema: StoreGeneratorSchema): Promise<void> {
  if (schema.attachToRoute) {
    throw Error('attachToRoute not yet implemented');
  }

  // must be relative to workspace root
  const targetDirectory = pathRelativeToWorkspace(tree, schema.directory);

  const name = names(schema.name);
  const nameSmall = name.fileName;
  const nameCapitalized = name.className;
  const storeClassName = name.className + 'Store';
  const stateClassName = name.className + 'State';
  const storeFileName = name.fileName + '.store';
  const providedIn = parseProvidedIn(schema.providedIn);

  const substitutions: Record<string, unknown> = {
    nameSmall,
    nameCapitalized,
    targetDirectory,
    storeClassName,
    stateClassName,
    storeFileName,
    providedIn,
    tmpl: '',
  };

  console.debug('input', schema, substitutions);

  generateFiles(
    tree,
    joinPathFragments(__dirname, './files'),
    targetDirectory,
    substitutions,
    {overwriteStrategy: schema.overwriteStrategy},
  );

  await formatFiles(tree);
}

function parseProvidedIn(providedIn: string): string {
  if (providedIn === '' || providedIn === 'null') {
    return 'null';
  }

  if (providedIn === 'type') {
    return 'YourFancyTypeHere';
  }

  return `'${providedIn}'`;
}

