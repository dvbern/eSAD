import {BackendEntityId} from '@dvb/data-access-types';

export interface NotizenFileParams {
  notizId: BackendEntityId,
  fileId: BackendEntityId,
  downloadDirectly?: boolean,
}
