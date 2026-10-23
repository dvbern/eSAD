import {libraryGenerator} from '@nx/angular/generators';
import {formatFiles, getProjects, Tree} from '@nx/devkit';
import * as fs from 'fs';
import {createTreeWithEmptyWorkspace} from 'nx/src/generators/testing-utils/create-tree-with-empty-workspace';
import * as path from 'path';
import prompts from 'prompts';
import libGenerator, {prefixMap} from './generator';
import {LibGeneratorSchema} from './schema';
import type {Mock} from 'vitest';

vi.mock('@nx/angular/generators', () => ({
    libraryGenerator: vi.fn(),
}));

vi.mock('@nx/devkit', () => ({
    formatFiles: vi.fn(),
    getProjects: vi.fn().mockReturnValue(new Map([['dummy-app', {}]])),
}));

vi.mock('prompts', () => ({
    default: vi.fn(),
}));

vi.mock('fs', async (importOriginal) => ({
    ...(await importOriginal<typeof import('fs')>()),
    promises: {
        readFile: vi.fn(),
        writeFile: vi.fn(),
    },
}));

describe('libGenerator', () => {
    let tree: Tree;
    let schema: LibGeneratorSchema;

    beforeEach(() => {
        vi.clearAllMocks();
        tree = createTreeWithEmptyWorkspace({layout: 'apps-libs'});
    });

    describe('normalize values', () => {
        it('dasherize name', async () => {
            schema = {
                name: 'DasIst ein Name',
                type: 'feat',
                scope: 'shared',
                directory: '',
            };

            const normalizedSchema = await libGenerator(tree, schema);

            expect(normalizedSchema.name)
                .toEqual('das-ist-ein-name');
        });

        it('dasherize appName', async () => {
            schema = {
                name: 'stammdaten',
                type: 'feat',
                scope: 'app-specific',
                directory: '',
            };

            (prompts as unknown as Mock)
                .mockResolvedValueOnce({appName: 'esc-app'});

            const normalizedSchema = await libGenerator(tree, schema);

            expect(normalizedSchema.appName).toEqual('esc-app');
        });
    });

    describe('library scope:shared', () => {
        it('should call libraryGenerator with correct parameters for shared library', async () => {
            schema = {
                name: 'test',
                type: 'feat',
                scope: 'shared',
                directory: '',
            };

            await libGenerator(tree, schema);

            expect(libraryGenerator).toHaveBeenCalledWith(tree, {
                name: 'test',
                directory: 'libs/shared/feat/feat-test',
                tags: 'type:feat,scope:shared',
                setParserOptionsProject: true,
                prefix: 'feat',
                routing: true,
                style: 'scss',
                buildable: true,
                importPath: '@dvb/feat-test',
            });

            expect(formatFiles).toHaveBeenCalledWith(tree);
        });

        it('should set correct prefix on lib type ui', async () => {
            schema = {
                name: 'buttons',
                type: 'ui',
                scope: 'shared',
                directory: '',
            };

            await libGenerator(tree, schema);

            expect(libraryGenerator).toHaveBeenCalledWith(tree, {
                name: 'buttons',
                directory: 'libs/shared/ui/ui-buttons',
                tags: 'type:ui,scope:shared',
                setParserOptionsProject: true,
                prefix: prefixMap[schema.type],
                routing: true,
                style: 'scss',
                buildable: true,
                importPath: '@dvb/ui-buttons',
            });

            expect(formatFiles).toHaveBeenCalledWith(tree);
        });
    });

    describe('generated API for data-access', () => {
        it('should prompt for isGeneratedApi if scope is shared and type is data-access', async () => {
            schema = {
                name: 'backend',
                type: 'data-access',
                scope: 'shared',
                directory: '',
            };

            (prompts as unknown as Mock).mockResolvedValueOnce({
                isGeneratedApi: 'yes',
            });

            await libGenerator(tree, schema);

            expect(prompts).toHaveBeenCalledWith(
                expect.objectContaining({
                    type: 'select',
                    name: 'isGeneratedApi',
                }),
            );
        });

        it('should not prompt for isGeneratedApi if scope is app-specific', async () => {
            schema = {
                name: 'backend',
                type: 'data-access',
                scope: 'app-specific',
                directory: '',
            };

            (prompts as unknown as Mock)
                .mockResolvedValueOnce({appName: 'esc-app'});

            await libGenerator(tree, schema);

            expect(prompts).not.toHaveBeenCalledWith(
                expect.objectContaining({
                    name: 'isGeneratedApi',
                }),
            );
        });

        it('should add generated:api tag when isGeneratedApi is yes', async () => {
            schema = {
                name: 'backend',
                type: 'data-access',
                scope: 'shared',
                directory: '',
            };

            (prompts as unknown as Mock).mockResolvedValueOnce({
                isGeneratedApi: 'yes',
            });

            await libGenerator(tree, schema);

            expect(libraryGenerator).toHaveBeenCalledWith(tree, {
                name: 'backend',
                directory: 'libs/shared/data-access/data-access-backend',
                tags: 'type:data-access,generated:api,scope:shared',
                setParserOptionsProject: true,
                prefix: prefixMap[schema.type],
                routing: true,
                style: 'scss',
                buildable: true,
                importPath: '@dvb/data-access-backend',
            });
        });

        it('should not add generated:api tag when isGeneratedApi is no', async () => {
            schema = {
                name: 'backend',
                type: 'data-access',
                scope: 'shared',
                directory: '',
            };

            (prompts as unknown as Mock).mockResolvedValueOnce({
                isGeneratedApi: 'no',
            });

            await libGenerator(tree, schema);

            expect(libraryGenerator).toHaveBeenCalledWith(tree, {
                name: 'backend',
                directory: 'libs/shared/data-access/data-access-backend',
                tags: 'type:data-access,scope:shared',
                setParserOptionsProject: true,
                prefix: prefixMap[schema.type],
                routing: true,
                style: 'scss',
                buildable: true,
                importPath: '@dvb/data-access-backend',
            });
        });
    });

    describe('library scope:app-specific', () => {
        it('should only show apps in appName prompt', async () => {
            schema = {
                name: 'something',
                type: 'feat',
                scope: 'app-specific',
                directory: '',
            };

            (getProjects as Mock).mockReturnValue(
                new Map([
                    ['test-app', {}],
                    ['example-app', {}],
                    ['example-app-e2e', {}],
                    ['stammdaten', {}],
                    ['some-other-lib', {}],
                ]),
            );

            (prompts as unknown as Mock)
                .mockResolvedValueOnce({appName: 'test-app'});

            await libGenerator(tree, schema);

            expect(prompts).toHaveBeenCalledWith(
                expect.objectContaining({
                    type: 'select',
                    name: 'appName',
                    message: 'Which app does it concern?',
                    choices: [
                        {title: 'test-app', value: 'test-app'},
                        {title: 'example-app', value: 'example-app'},
                    ],
                }),
            );
        });

        it('should call libraryGenerator with correct parameters for app-specific library', async () => {
            schema = {
                name: 'stammdaten',
                type: 'feat',
                scope: 'app-specific',
                directory: '',
            };

            (prompts as unknown as Mock)
                .mockResolvedValueOnce({appName: 'esc-app'});

            await libGenerator(tree, schema);

            expect(libraryGenerator).toHaveBeenCalledWith(tree, {
                name: 'stammdaten',
                directory: 'libs/esc-app/feat/feat-stammdaten',
                tags: 'type:feat,scope:esc-app',
                setParserOptionsProject: true,
                prefix: 'feat',
                routing: true,
                style: 'scss',
                buildable: true,
                importPath: '@dvb/feat-stammdaten',
            });

            expect(formatFiles).toHaveBeenCalledWith(tree);
        });

    });

    describe('addLibraryToCommitScopes', () => {
        const projectsPath = path.join(__dirname, '../../../../../../projects.json');

        it('should add a new library to projects.json', async () => {
            const schema: LibGeneratorSchema = {name: 'test-lib', type: 'util', scope: 'shared', directory: ''};
            const initialProjects = ['library1', 'library2'];
            const expectedProjects = [...initialProjects, 'util-test-lib'].sort();

            (fs.promises.readFile as Mock).mockResolvedValue(JSON.stringify(initialProjects));
            (fs.promises.writeFile as Mock).mockResolvedValue(Promise.resolve());

            await libGenerator(tree, schema);

            expect(fs.promises.writeFile).toHaveBeenCalledWith(
                projectsPath,
                JSON.stringify(expectedProjects, null, 2),
            );
        });

        it('should not add the library if it already exists in projects.json', async () => {
            const schema: LibGeneratorSchema = {name: 'existing-lib', type: 'util', scope: 'shared', directory: ''};
            const initialProjects = ['library1', 'util-existing-lib', 'library2'];

            (fs.promises.readFile as Mock).mockResolvedValue(JSON.stringify(initialProjects));

            await libGenerator(tree, schema);

            expect(fs.promises.writeFile).not.toHaveBeenCalled();
        });

        it('should sort the projects ', async () => {
            const schema: LibGeneratorSchema = {name: 'another-lib', type: 'feat', scope: 'shared', directory: ''};
            const initialProjects = ['z-library', 'a-library'];
            const expectedProjects = [...initialProjects, 'feat-another-lib'].sort();

            (fs.promises.readFile as Mock).mockResolvedValue(JSON.stringify(initialProjects));

            await libGenerator(tree, schema);

            expect(fs.promises.writeFile).toHaveBeenCalledWith(
                projectsPath,
                JSON.stringify(expectedProjects, null, 2),
            );
        });

    });
});
