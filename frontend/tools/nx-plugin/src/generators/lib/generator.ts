import { libraryGenerator } from '@nx/angular/generators';
import { formatFiles, getProjects, Tree } from '@nx/devkit';
import { dasherize } from '@nx/devkit/src/utils/string-utils';
import * as fs from 'fs';
import * as path from 'path';
import * as prompts from 'prompts';
import { LibGeneratorSchema, YesNo } from './schema';

export type LibType = 'feat' | 'ui' | 'util' | 'data-access';
export type LibScope = 'shared' | 'app-specific';

export const PACKAGE_NAME_PREFIX = 'dvb';
export const prefixMap: Record<LibType, string> = {
  feat: 'feat',
  ui: 'svui',
  util: 'util',
  'data-access': 'dacc',
};

/**
 * @return the normalized schema
 */
export default async function libGenerator(tree: Tree, schema: LibGeneratorSchema): Promise<LibGeneratorSchema> {
  const withNamesSetUp = await promptForNames(tree, schema);
  if (schema.scope === 'shared' && schema.type === 'data-access') {
    schema.isGeneratedApi = await promptForIsGeneratedApi();
  }

  const params = normalizeValues(withNamesSetUp);

  await libraryGenerator(tree, {
    name: params.name,
    directory: buildLibraryDirectory(params),
    tags: buildTags(params),
    setParserOptionsProject: true,
    prefix: prefixMap[params.type],
    style: 'scss',
    buildable: true,
    importPath: `@${PACKAGE_NAME_PREFIX}/${buildLibraryName(params)}`,
    routing: true,
  });

  if (!params.skipFormat) {
    await formatFiles(tree);
  }

  await addLibraryToCommitScopes(buildLibraryName(params));

  return params;
}

async function promptForNames(tree: Tree, params: LibGeneratorSchema): Promise<LibGeneratorSchema> {
  if (params.scope !== 'app-specific') {
    return params;
  }

  return {
    ...params,
    appName: await promptForAppName(tree),
  };
}

async function promptForAppName(tree: Tree): Promise<string> {
  const allProjects = getProjects(tree);
  const apps = Array.from(allProjects.keys())
    .filter(project => project.endsWith('-app'))
    .map(project => ({
      title: project,
      value: project,
    }));

  const response = await prompts({
    type: 'select',
    name: 'appName',
    message: 'Which app does it concern?',
    choices: apps,
  });

  return response.appName;
}

function buildTags(params: LibGeneratorSchema): string {
  return [buildTypeTag(params), buildScopeTag(params)].join(',');
}

function normalizeValues(schema: LibGeneratorSchema): LibGeneratorSchema {
  return {
    ...schema,
    name: dasherize(schema.name),
    appName: dasherize(schema.appName),
  };
}

function buildLibraryName(schema: LibGeneratorSchema): string {
  return [schema.type, schema.name].join('-');
}

function buildTypeTag(schema: LibGeneratorSchema): string {
  return schema.type === 'data-access' && schema.isGeneratedApi === 'yes'
    ? 'type:data-access,generated:api'
    : `type:${schema.type}`;
}

function buildScopeTag(schema: LibGeneratorSchema): string {
  return schema.scope === 'app-specific' ? `scope:${schema.appName}` : `scope:${schema.scope}`;
}

function buildLibraryDirectory(schema: LibGeneratorSchema): string {
  return schema.scope === 'shared'
    ? `libs/shared/${schema.type}/${buildLibraryName(schema)}`
    : `libs/${schema.appName}/${schema.type}/${buildLibraryName(schema)}`;
}

async function promptForIsGeneratedApi(): Promise<YesNo> {
  const response = await prompts({
    type: 'select',
    name: 'isGeneratedApi',
    message: 'Is this a generated API library?',
    choices: [
      { title: 'No', value: 'no' satisfies YesNo },
      { title: 'Yes', value: 'yes' satisfies YesNo },
    ],
  });

  return response.isGeneratedApi;
}

async function addLibraryToCommitScopes(libraryName: string): Promise<void> {
  const projectsPath = path.join(__dirname, '../../../../../../projects.json');
  const projectsJson = await fs.promises.readFile(projectsPath, 'utf-8');
  const projects: string[] = projectsJson?.length ? JSON.parse(projectsJson) : [];

  if (projects.includes(libraryName)) {
    return;
  }

  projects.push(libraryName);
  projects.sort((a: string, b: string) => a.localeCompare(b));

  await fs.promises.writeFile(projectsPath, JSON.stringify(projects, null, 2));
}
