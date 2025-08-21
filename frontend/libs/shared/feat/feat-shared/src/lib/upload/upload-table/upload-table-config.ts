export interface UploadTableConfig {
  disableHeader: boolean;
  disableUploadButton: boolean;
  showViewAction: boolean;
  showDirectDownloadAction: boolean;
  showRemoveAction: boolean;
  columnSize: boolean;
  columnBelegUploadTyp: boolean;
  multiFileSelector: boolean;
  singleFileStore: boolean;
}

export const uploadTableDefaults: UploadTableConfig = {
  disableHeader: true,
  disableUploadButton: true,
  showViewAction: true,
  showRemoveAction: true,
  showDirectDownloadAction: true,
  columnSize: false,
  columnBelegUploadTyp: false,
  multiFileSelector: false,
  singleFileStore: false,
};
