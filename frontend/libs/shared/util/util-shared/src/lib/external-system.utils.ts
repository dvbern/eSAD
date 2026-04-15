import {ExternalImportInfoJson, ImportTyp} from "@dvb/data-access-backend";
import {isNotNullish, Nullish} from "./nullish";


export function isExternalObject(objectWithExternalInfo: { externalImportInfoJson?: ExternalImportInfoJson } | Nullish): boolean{
  return isActiveInExternalSystem(objectWithExternalInfo)
    && isNotNullish(objectWithExternalInfo?.externalImportInfoJson)
    && [ImportTyp.ESCADA, ImportTyp.PUPIL].includes(objectWithExternalInfo.externalImportInfoJson.importTyp);
}


export function isActiveInExternalSystem(objectWithExternalInfo: { externalImportInfoJson?: ExternalImportInfoJson } | Nullish): boolean{
  return objectWithExternalInfo?.externalImportInfoJson?.aktivInExternalSystem ?? false;
}

