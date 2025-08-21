export interface AppNotification {

  id: string;

  summary: string;

  autohide: boolean;

  autohideMs?: number;

  alertType:  'success'| 'info'| 'warning'| 'danger'| 'primary'| 'secondary'| 'light'| 'dark';

}
