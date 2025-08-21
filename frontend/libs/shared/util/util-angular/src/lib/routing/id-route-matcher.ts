import { UrlSegment } from '@angular/router';
import { isValidBackendEntityId } from '@dvb/data-access-types';

/**
 * Matches a route with a single segment that is a valid backend entity id.
 * It will be assigned to the `id` parameter.
 * @param url
 */
export function routeMatchesId(url: UrlSegment[]) {
  if (url.length === 1 && isValidBackendEntityId(url[0].path)) {
    return { consumed: url, posParams: { id: new UrlSegment(url[0].path, {}) } };
  }
  return null;
}
