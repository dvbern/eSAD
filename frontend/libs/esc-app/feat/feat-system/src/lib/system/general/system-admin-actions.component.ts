
import {ChangeDetectionStrategy, Component} from '@angular/core';
import {KindNamenNormalisierungComponent} from '../kind-namen-normalisierung/kind-namen-normalisierung.component';
import {PlzImportComponent} from '../plzimport/plzimport.component';
import {S3FileDownloadComponent} from '../s3-file-download/s3-file-download.component';
import {TimetravelComponent} from '../timetravel/timetravel.component';
import {ServerConfigurationRequestComponent} from "../configuration-export/server-configuration-request.component";

@Component({
  selector: 'feat-system-admin-actions',
  imports: [
    PlzImportComponent,
    S3FileDownloadComponent,
    TimetravelComponent,
    ServerConfigurationRequestComponent,
    KindNamenNormalisierungComponent
  ],
  templateUrl: './system-admin-actions.component.html',
  styles: ':host { display:block;}',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SystemAdminActionsComponent {}
