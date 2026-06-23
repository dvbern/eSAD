import {NotizJson} from "@dvb/data-access-backend";
import { NotizJsonEnhanced } from './notiz-json-enhanced';
import {ensurePropsFlat, joinNotBlank} from "@dvb/util-shared";

export function enhanceNotizJson(notiz: NotizJson): NotizJsonEnhanced {
  return ensurePropsFlat<NotizJsonEnhanced>({
    labelIds: notiz.data.labels.map(each => each.id),
    id: notiz.id,
    entityInfo: notiz.entityInfo,
    titel: notiz.data.titel,
    status: notiz.data.status,
    verantwortlicher: notiz.data.verantwortlicher,
    labels: notiz.data.labels,
    kindFullName: joinNotBlank([notiz.kind.vorname, notiz.kind.name]),
    hasAnhang: notiz.data.files.length > 0
  });
}
