// eslint-disable-next-line @nx/enforce-module-boundaries
import {version} from './../../../../../../package.json';

if (!version) {
  throw Error('could not read version from package.json');
}

export function getAppVersion(): string {
  return `V${version}`;
}
