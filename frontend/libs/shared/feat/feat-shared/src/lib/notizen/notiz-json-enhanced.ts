import {NotizJson} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';

export interface NotizJsonEnhanced extends NotizJson {
  labelIds: Array<BackendEntityId>;
  hasAnhang: boolean;
  kindFullName?: string;
}
