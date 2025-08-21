import {NotizSaveJson} from '@dvb/data-access-backend';

export interface NotizenSaveParams {
  notiz: NotizSaveJson;
  fileUploads: File[] | undefined;
}
