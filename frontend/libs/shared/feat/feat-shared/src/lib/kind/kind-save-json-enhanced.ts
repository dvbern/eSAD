import {BenutzerOptionJson, KindSaveJson} from "@dvb/data-access-backend";

export interface KindSaveJsonEnhanced extends KindSaveJson {
  zustaendigArzt?: BenutzerOptionJson;
  zustaendigMPA?: BenutzerOptionJson;
}
