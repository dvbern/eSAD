import { ApplicationConfig } from '@angular/core';
import { providePublicCore } from '@dvb/feat-shared';

import { appRoutes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [providePublicCore(appRoutes)],
};
