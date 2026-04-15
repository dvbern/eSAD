import { patternBackendEntityId } from '../../../util/url-e2e.utils';
import {WithSubpaths} from "../../../page-objects/with-subpahts";

export class NotizenDetailPageObject extends WithSubpaths<'impfausweis' | 'view'> {

  selector = 'feat-notiz-detail';

  paths = {
    impfausweis: `/anlass/${patternBackendEntityId}/notizen/impfausweis`,
    view: '',
  };

  elements = {
    buttonAddFile: () => this.page.getByTestId('button-upload-file'),
    buttonConfirmAdd: () => this.page.getByTestId('button-confirm'),
  }
}
