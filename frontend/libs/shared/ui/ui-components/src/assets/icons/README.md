# How to add icons:

- Find a similar icon in an existing library: https://ng-icons.github.io/ng-icons/#/browse-icons
  1. Prio 1: Hero; prio 2: Bootstrap
  2. custom icon if nothing in Hero or Bootstrap:
     - Copy icon from Figma:
       - in Figma (edit mode), go to design system, Icons: https://www.figma.com/design/SYjj0AfsJHEruBMD0a2JEi/EDOKID-Design-System-NEW?node-id=15-2&node-type=canvas&t=YgyN4uaE9tp7lFTw-0
       - select the "For Devs", export as svg
     - copy to this directory
     - replace color with "**currentColor**" inside svg file
     - they should be 16x16
- add to [IconUseCase](../../lib/widgets/icons/icon/icon.component.ts)
- check on Playground: https://local-esc-bs.localhost:4200/playground/icons

# Overview:
https://local-esc-bs.localhost:4200/playground/icons

# Special:
- icons for special components (e.g. ng-select) can be added to subdirectories in order to be referenced from scss
