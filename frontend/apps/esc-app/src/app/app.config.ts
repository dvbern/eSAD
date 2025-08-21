import {ApplicationConfig} from '@angular/core';
import {provideCore} from '@dvb/feat-shared';
import {appRoutes} from './app.routes';

export const appConfig: ApplicationConfig = {
    providers: [provideCore(appRoutes)],
};
