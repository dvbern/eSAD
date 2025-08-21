import {BackendEntityId} from '@dvb/data-access-types';

export interface AnlassEinladungenId {
  anlassId: BackendEntityId;
  einladungIds: BackendEntityId[];
}
