import {ApplicationConfig, ErrorHandler} from '@angular/core';
import {AppErrorHandlerService} from '@dvb/util-angular';

export function provideAppErrorHandler(): ApplicationConfig['providers'] {
  return [
    {
      provide: ErrorHandler,
      useClass: AppErrorHandlerService,
    },
  ];

}
