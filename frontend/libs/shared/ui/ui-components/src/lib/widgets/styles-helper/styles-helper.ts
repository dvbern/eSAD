// StylesHelper:
// Globale CSS-Klassen und -Variablen

// prefix: see _css-var.scss
export const cssVarPrefix = '--svui-';

export function readCssVar(name: string): string {
  return `var(${cssVarPrefix}${name})`;
}

// StyleClasses which are defined and used in multiple components:
// svui classes
export const classCardContentFullWidth = 'svui_card__content--full-width';
export const classChildForm = 'svui_child-form';
export const classFormLayoutWidthDouble = 'svui_form-layout--double';
export const classFormLayoutWidthFull = 'svui_form-layout--full';
export const classFormLayoutWidthHalf = 'svui_form-layout--half';
export const classFormLayoutWidthNormal = 'svui_form-layout--normal';
export const classFormLayoutWidthOneAndHalf = 'svui_form-layout--one_and_half';
export const classInput = 'svui_input';
export const classListItemSecondary = 'svui_list-item__secondary';
export const classListItemTitle = 'svui_list-item__title';
export const classTextError = 'svui_text--error';

// bootstrap classes
export const classBootstrapInvalid = 'is-invalid';
export const classBootstrapVisuallyHidden = 'visually-hidden';
