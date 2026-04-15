import {HttpHandlerFn, HttpRequest} from '@angular/common/http';
import {inject} from '@angular/core';
import {LanguageStore} from './language.store';


export function acceptLanguageInterceptorFn(req: HttpRequest<unknown>, next: HttpHandlerFn) {
  const API_URL = 'api';
  // check if we are making an api request
  if (!(req.url?.startsWith('/' + API_URL) || req.url?.startsWith(API_URL))) {
    return next(req);
  }

  const languageStore = inject(LanguageStore);
  const currentLocale = languageStore.activeLocale();

  if (currentLocale) {
    const browserLanguages = navigator.languages || [navigator.language];

    // Normalize language tags by removing quality values (e.g., "en;q=0.9" -> "en") for comparison
    const languages = [
      currentLocale,
      ...browserLanguages.filter(lang => {
        const normalizedLang = lang.split(';')[0].trim();
        return normalizedLang !== currentLocale;
      }),
    ];
    const acceptLanguageHeader = languages.join(', ');

    const modifiedReq = req.clone({
      headers: req.headers.set('Accept-Language', acceptLanguageHeader),
    });

    return next(modifiedReq);
  } else {
    return next(req);
  }

}
