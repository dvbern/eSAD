import {BackendEntityId} from '@dvb/data-access-types';

export interface NotizenFileParams {
  kindId: BackendEntityId,
  notizId: BackendEntityId,
  fileId: BackendEntityId,
  downloadDirectly?: boolean,
}
