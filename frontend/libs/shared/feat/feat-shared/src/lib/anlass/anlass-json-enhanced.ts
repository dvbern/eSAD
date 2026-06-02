import {AnlassJson} from "@dvb/data-access-backend";
import {ISODateTimeString} from "@dvb/data-access-types";

export interface AnlassJsonEnhanced extends AnlassJson {
  fullTitle: string;
  dateTime?: ISODateTimeString
}

