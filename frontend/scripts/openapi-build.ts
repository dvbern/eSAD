import * as childProcess from 'child_process';
import * as fs from 'fs';
import { PathLike } from 'fs';
import * as path from 'path';

type ApiSpec = {
  name: string;
  contractPath: string;
  generatedRootDir: string;
  basePathOverride?: string;
  apis: 'all' | string[];
};

const specs: ApiSpec[] = [
  {
    name: 'backend',
    // path or url to openapi spec
    contractPath: '../api/openapi-schema/openapi.yaml',
    generatedRootDir: 'libs/shared/data-access/data-access-backend/src/lib/backend',
    // FIXME: starter: decide if you want to explicitly generate only specified APIs
    // Keep in mind: there is one file generated per OpenAPI-@Tag,
    // Names are CamelCase versions from OpenAPIConst Tags
    apis: 'all',
    // apis: [
    //   'administrations',
    //   'benutzer',
    //   'einstellungen',
    // ],
  },
  // {
  //   name: 'extended-example',
  //   contractPath: 'scripts/specs/fixme-starter-extended-example.openapi.yaml',
  //   generatedRootDir: 'libs/shared/data-access/data-access-extended-example/src/lib/extended-example',
  //   basePathOverride: '/rest',
  //   apis: 'all',
  // },
];

const dependencies = require('../package.json').dependencies;

const ngVersion = dependencies['@angular/core'].replace(/[^0-9.]/, '');
console.log('ngVersion', ngVersion);

function deleteFile(filePath: PathLike) {
  fs.unlinkSync(filePath);
}

function deleteOldFilesSync(directory: string, timestamp: Date) {
  if (!fs.existsSync(directory)) {
    return;
  }

  const files = fs.readdirSync(directory);
  for (const file of files) {
    const filePath = path.join(directory, file);
    const stats = fs.statSync(filePath);
    if (stats.mtime.getTime() < timestamp.getTime()) {
      deleteFile(filePath);
    }
  }
}

function copyFiles(directoryFrom: string, directoryTo: string) {
  if (!fs.existsSync(directoryFrom)) {
    throw Error(`directoryFrom does not exist: ${directoryFrom}`);
  }

  if (!fs.existsSync(directoryTo)) {
    fs.mkdirSync(directoryTo, { recursive: true });
  }

  const files = fs.readdirSync(directoryFrom);
  for (const file of files) {
    const filePath = path.join(directoryFrom, file);
    const filePathTo = path.join(directoryTo, file);
    fs.copyFileSync(filePath, filePathTo);
  }
}

const typesPath = '@dvb/data-access-types';
const typeMap = [
  // [OpenApi-Name/Format, Typescript-Name, Import-Path]
  ['AnyType', 'object', undefined],
  // ['set', 'FakeSet', typesPath],
  // *alle* Datum/DateTime Datentypen im Backend sind leider als DateTime definiert,
  // egal ob sie Time haben oder nicht :(
  ['date', 'ISODateString', typesPath],
  ['Date', 'ISODateTimeString', typesPath],
  ['DateTime', 'ISODateTimeString', typesPath],
  // ['timestamp', 'FIXME:implement', typesPath],
  ['url', 'BackendURL', typesPath],
  ['semantic-version', 'BackendSemver', typesPath],
  // ['local-time', 'BackendLocalTime', typesPath],
  // ['local-time-hhmm', 'BackendLocalTimeHHMM', typesPath],
  // ['zoned-date-time', 'BackendZonedDateTime', typesPath],
  //  ['email', 'BackendEmail', typesPath],
  // ['rest-includes', 'RestIncludes', typesPath],
  ['entity-id', 'BackendEntityId', typesPath],
  ['entity-id-with-class', 'BackendEntityIdWithClass', typesPath],
] as const;

/**
 * Post-processes generated service files to fix missing imports.
 * The OpenAPI generator sometimes omits imports for custom-mapped types
 * when multiple types share the same import path.
 */
function fixMissingImports(directory: string) {
  if (!fs.existsSync(directory)) {
    return;
  }

  const importableTypes = typeMap
    .filter(e => !!e[2])
    .map(e => ({ typeName: e[1], importPath: e[2]! }));

  for (const file of fs.readdirSync(directory).filter(f => f.endsWith('.ts'))) {
    const filePath = path.join(directory, file);
    let content = fs.readFileSync(filePath, 'utf-8');

    const usedTypes = importableTypes.filter(({ typeName }) => new RegExp(`\\b${typeName}\\b`).test(content));
    if (usedTypes.length === 0) {
      continue;
    }

    const escapedPath = usedTypes[0].importPath.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    const importRegex = new RegExp(`import\\s*\\{([^}]*)\\}\\s*from\\s*'${escapedPath}'\\s*;`);
    const match = importRegex.exec(content);
    const existingTypes = match ? match[1].split(',').map(t => t.trim()).filter(t => t.length > 0) : [];
    const allTypes = [...new Set([...existingTypes, ...usedTypes.map(t => t.typeName)])].sort();
    const newImport = `import { ${allTypes.join(', ')} } from '${usedTypes[0].importPath}';`;

    if (match) {
      content = content.replace(match[0], newImport);
    } else {
      const basePathIdx = content.indexOf("import { BASE_PATH");
      if (basePathIdx !== -1) {
        content = content.slice(0, basePathIdx) + newImport + '\n' + content.slice(basePathIdx);
      }
    }

    fs.writeFileSync(filePath, content, 'utf-8');
  }
}

async function generateOpenApi(spec: ApiSpec, outputDir: string) {
  const backendKeystoreFile = path.resolve(
    __dirname,
    '../../../quarkus-starter/developer-local-settings/config/certs/local-esc-bs.localhost.p12',
  );
  const env = {
    ...process.env,
    JAVA_OPTS: [
      `-Djavax.net.ssl.trustStore=${backendKeystoreFile}`,
      '-Djavax.net.ssl.trustStorePassword=changeit',
    ].join(' '),
  };

  const apisParam = spec.apis === 'all' ? '' : '=' + spec.apis.join(':');
  const typeMappingsArg = typeMap.map(e => `${e[0]}=${e[1]}`).join(',');
  const importMappingsArg = typeMap
    .filter(e => !!e[2])
    .map(e => `${e[1]}=${e[2]}`)
    .join(',');

  const cmd =
    'npx openapi-generator-cli generate' +
    ` -i ${spec.contractPath}` +
    ' -g typescript-angular' +
    ' --template-dir scripts/conf/openapi-templates' +
    ` --global-property models,apis${apisParam},supportingFiles` +
    ` -p basePath=${spec.basePathOverride ?? ''}` +
    ' -p supportsES6=true' +
    // Sortierung: uebernimmt Originalreihenfolge
    ' -p sortModelPropertiesByRequiredFlag=false' +
    ' -p sortParamsByRequiredFlag=false' +
    ' -p enumPropertyNaming=original' +
    ' -p stringEnums=false' +
    // Eindeutiges Naming fuer Service-Methoden und Parameter-Objekte
    ' -p removeOperationIdPrefix=true' +
    ' -p prefixParameterInterfaces=true' +
    ' -p fileNaming=kebab-case' +
    ' -p legacyDiscriminatorBehavior=false' +
    // sonst schneidet es bei einigen Enums den vordersten Teil einfach ab, wenn alle Eintraege das gleiche Prefix haben
    ' -p removeEnumValuePrefix=false' +
    ' -p useSingleRequestParameter=true' +
    ' -p taggedUnions=true' +
    ` --type-mappings ${typeMappingsArg}` +
    ` --import-mappings ${importMappingsArg}` +
    // For normalizer options see:
    // https://github.com/OpenAPITools/openapi-generator/blob/master/modules/openapi-generator/src/main/java/org/openapitools/codegen/OpenAPINormalizer.java
    ' --openapi-normalizer REF_AS_PARENT_IN_ALLOF=true' +
    ' --openapi-normalizer REMOVE_ANYOF_ONEOF_AND_KEEP_PROPERTIES_ONLY=true' +
    ' --openapi-normalizer SIMPLIFY_ANYOF_STRING_AND_ENUM_STRING=true' +
    ' --openapi-normalizer SIMPLIFY_BOOLEAN_ENUM=true' +
    ' --openapi-normalizer SIMPLIFY_ONEOF_ANYOF=true' +
    ' --openapi-normalizer KEEP_ONLY_FIRST_TAG_IN_OPERATION=true' +
    ' -o ' +
    outputDir;

  console.log('executing command: ', cmd);
  const child = childProcess.exec(cmd, { env });
  child.stdout?.on('data', data => {
    console.log(data.toString());
  });
  child.stderr?.on('data', data => {
    console.error(data.toString());
  });

  return new Promise<void>((resolve, reject) => {
    child.on('exit', code => (code === 0 ? resolve() : reject('exit code: ' + code)));
    child.on('error', reject);
  });
}

async function sleep(msec: number) {
  return new Promise(resolve => setTimeout(resolve, msec));
}

// async main
(async () => {
  specs.forEach(async spec => {
    const timestamp = new Date();
    // noinspection MagicNumberJS
    await sleep(100); // make sure timestamp ticks

    const generatorPath = `tmp/generated/${spec.name}`;
    const apiPath = path.join(generatorPath, 'api');
    const modelsPath = path.join(generatorPath, 'model');

    await generateOpenApi(spec, generatorPath);

    // Fix missing imports in generated service files before copying
    fixMissingImports(apiPath);

    const modelPath = `${spec.generatedRootDir}/openapi/model`;
    const servicePath = `${spec.generatedRootDir}/openapi/services`;

    deleteOldFilesSync(apiPath, timestamp);
    deleteOldFilesSync(servicePath, timestamp);
    deleteOldFilesSync(modelsPath, timestamp);
    deleteOldFilesSync(modelPath, timestamp);

    copyFiles(apiPath, servicePath);
    copyFiles(modelsPath, modelPath);
    // we can generate only a selection of model entities according to namespace for example.
    // That would allow us to split the model directly in the right packages. Lets see if we need it.
  });
})();
