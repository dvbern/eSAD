export type AlertType = 'success' | 'info' | 'warning' | 'danger';

export const AlertType = {
  danger: 'danger',
  info: 'info',
  success: 'success',
  warning: 'warning',
} as const;

// TODO EPONEU-289 stattdessen auch AppNotification verwenden?
export interface Alert {
  type: AlertType;
  message: string;
  /**
   * If true, the alert will be hidden after a certain time.
   * If false, the alert must be dismissed by the user.
   * Default is dependent on type: 'success' => true, false otherwise.
   * If a number is provided, it will be used as the timeout in milliseconds.
   */
  autoHide?: boolean | number;
}
