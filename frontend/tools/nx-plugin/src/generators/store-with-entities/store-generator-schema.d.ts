import {OverwriteStrategy} from '@nx/devkit';

export interface StoreGeneratorSchema {
  name: string;
  providedIn: string;
  attachToRoute: boolean;
  directory: string;
  overwriteStrategy: OverwriteStrategy;
}
