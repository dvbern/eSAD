export type ButtonColor = 'primary' | 'secondary' | 'tertiary' | 'success' | 'outline-primary';
export type IconButtonType =
  | 'primary'
  | 'secondary'
  | 'tableRowAction'
  | 'inputAddOn'
  | 'success';
export type ButtonType = ButtonColor | IconButtonType;

export function bootstrapButtonColorStyleClass(color: ButtonType): string {
  switch (color) {
    case 'primary':
      return 'btn-primary';
    case 'outline-primary':
      return 'btn-outline-primary';
    case 'secondary':
      return 'btn-secondary';
    case 'tertiary':
    case 'inputAddOn':
    case 'tableRowAction':
      return 'btn-link';
    case 'success':
      return 'btn-success';
  }
}
