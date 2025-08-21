import { Schema } from '@nx/angular/src/generators/library/schema';
import { LibScope, LibType } from './generator';

export type YesNo = 'yes' | 'no';

export interface LibGeneratorSchema extends Schema {
  appName?: string;
  isGeneratedApi?: YesNo;
  name: string;
  scope: LibScope;
  type: LibType;
}
