<!-- TOC -->
* [Frontend](#frontend)
  * [Main Components](#main-components)
    * [Apps:](#apps)
    * [Libraries:](#libraries)
    * [Tools:](#tools)
    * [Project Structure](#project-structure)
  * [Setup](#setup)
    * [Prerequisites](#prerequisites)
    * [Installation](#installation)
  * [NPM Scripts](#npm-scripts)
  * [Add New Projects](#add-new-projects)
  * [Nx Console](#nx-console)
  * [Useful links](#useful-links)
<!-- TOC -->

# Frontend

This repository is organized as a monorepo using [Nx](https://nx.dev/), which means multiple apps and libraries are stored in a single repository.
The structure promotes code sharing, consistent development practices, and better project scalability.

This project is a starter template that will be continuously expanded over time.

> ℹ️ This is an ongoing project. Features and functionality will be gradually added.

---
 
## Main Components

This monorepo is divided into several main components that help keep the project modular, maintainable, and scalable:

### Apps:

The main applications that users interact with. Each app has its own folder under `apps/`.
For example, `esc-app` is the primary application, and `esc-app-e2e` contains the end-to-end tests for it.

An App ist a very minimalistic Angular application that is used to bootstrap the application and lazy-load features ("feat"-libs), where the actual work is done.

### Libraries:

In this Nx monorepo, libraries are organized under the `libs/` folder.
Each library serves a specific purpose and can be reused across multiple applications.
Below are the different types of libraries and their name-prefixes: 

**Feature Libraries (feat):**
These libraries contain smart components and business logic.
They handle the core functionality and complex logic of an application.

**Data-Access Libraries (data-access):**
These libraries are responsible for communicating with external APIs, databases, or other backend systems.
They serve as the data layer for applications, abstracting the details of how data is fetched or saved.

**UI Libraries (ui):**
These libraries contain user interface (UI) components.
UI libraries hold dumb components (a.k.a. presentational components), meaning they focus purely on presentation without any business logic.
They are reusable, self-contained UI pieces, such as buttons, cards, or input fields.

**Utility Libraries (util):**
Utility libraries contain helper functions and reusable logic that can be shared across applications and features.
These libraries typically do not include UI components or application-specific logic but provide functions that support the development process.


> ℹ️ Detailed documentation for the libraries can be found here: [Library Documentation](libs/README.md)

### Tools:

The `tools/` folder contains custom Nx plugins, generators, and scripts that help automate tasks across the monorepo.
These tools enhance productivity by extending the functionality of Nx. 

Below is an overview of the available tools:

**Nx Plugins**:

Custom Nx plugins are used to generate libraries or components according to specific project needs.
This monorepo contains a custom Nx plugin specifically designed to generate new libraries according to the monorepo's structure and best practices.
It helps maintain consistency when creating new feature, data-access, UI, or utility libraries.
The custom plugin is located in the tools/nx-plugin/ folder.

> ℹ️ Detail documentation for the tools can be found here: [Tools Documentation](tools/README.md)

### Project Structure
```
frontend/
├── apps/
├── libs/                  
└── tools/
```

---

## Setup

To get started with this project, follow these steps to set up your development environment.

### Prerequisites
- Node.js
- NPM
- Nx CLI

#### Install node using NVM
Install nvm ([linux, mac](https://github.com/nvm-sh/nvm) / [windows](https://github.com/coreybutler/nvm-windows)), then:

```bash
nvm install
nvm use
```
#### Install node using asdf-vm
Install [asdf-vm](https://asdf-vm.com), then:
```bash
asdf install
```

#### Install nx cli
```bash
  npm install -g nx
```

### Installation

1. After cloning the repository, navigate to the frontend folder

```bash
cd taxme-npo/frontend
```

2. Install project dependencies:

Run the following command to install all required dependencies for the project:
```
bash npm install
```

---

## NPM Scripts

| Script                             | Description                                                                          |
|------------------------------------|--------------------------------------------------------------------------------------|
| `npm run lint`                     | Lint all projects in the workspace in parallel.                                      |
| `npm run lint:affected`            | Lint only the projects affected by changes in parallel.                              |
| `npm run test`                     | Run unit tests for all projects in parallel.                                         |
| `npm run test:affected`            | Run unit tests only for the projects affected by changes.                            |
| `npm run build`                    | Build all projects in production mode in parallel.                                   |
| `npm run build:affected`           | Build only the projects affected by changes.                                         |
| `npm run build:prod`               | Build the specified project for production (`--prod`).                               |
| `npm run build:affected:prod`      | Build only the affected projects for production (`--prod`).                          |
| `npm run serve`                    | Serve the default application.                                                       |
| `npm run serve:esc-app` | Serve the `esc-app` application.                                          |
| `npm run format`                   | Format the entire workspace using Prettier.                                          |
| `npm run format:affected`          | Format only the projects affected by changes since the `main` branch.                |
| `npm run graph`                    | Generate a dependency graph of the workspace.                                        |
| `npm run g:lib`                    | Generate a new library using the custom Nx plugin `lib`.                             |
| `npm run g:lib:dry`                | Perform a dry run to simulate generating a library using the custom Nx plugin `lib`. |

---

## Add New Projects

| **Action**                 | **Command**                                                    | **Description**                                                                                    |
|----------------------------|----------------------------------------------------------------|----------------------------------------------------------------------------------------------------|
| Generate a new application | `npx nx g @nx/angular:app my-app`                              | Uses the default Nx generator to create a new Angular application.                                 |
| Generate a new component   | `npx nx g @nx/angular:component my-component --project=my-app` | Uses the default Nx generator to create a new component within a specified application or library. |
| Generate a new library     | `npx nx g @dvb/nx-plugin:lib-generator mylib`                  | Uses the custom library generator to create a library following the project-specific structure.    |
| Dry run for a new library  | `npx nx g @dvb/nx-plugin:lib-generator mylib --dry-run`        | Simulates the library generation to preview changes without making any actual modifications.       |

---

## Nx Console

Nx Console is an editor extension that enriches your developer experience.
It lets you run tasks, generate code, and improves code autocompletion in your IDE.
It is available for VSCode and IntelliJ.

[Install Nx Console &raquo;](https://nx.dev/getting-started/editor-setup?utm_source=nx_project&utm_medium=readme&utm_campaign=nx_projects)

With Nx Console, you can easily access the DVB Generator to create libraries directly from the console interface.
This makes it more convenient to generate new libraries while working within your IDE.

---

## Code Style

Maintaining a consistent code style is essential for collaboration and code quality. 
In this project, we utilize **ESLint** as our linter to enforce coding standards and best practices.

### ESLint Configuration

The ESLint configuration in this project leverages the default recommendations provided by **Nx**, 
which in turn are based on the best practices from **Angular** and **ESLint** itself. 
This layered approach ensures that our coding standards are aligned with widely accepted conventions in the Angular ecosystem, 
making it easier for new developers to onboard and contribute effectively to the project.

|  **Recommendation**         | **Description**                                                                                                                                         | **Link**                                      |
|-----------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------|
| **Nx Recommendations**      | The default rulesets provided by Nx are designed to work seamlessly with Angular applications, offering a solid foundation for enforcing code quality and best practices. | [Nx ESLint Plugin](https://github.com/nrwl/nx/tree/master/packages/eslint-plugin) |
| **Angular Recommendations** | The configuration builds upon Angular's established conventions, ensuring that our code adheres to the expectations of Angular developers.                   | [Angular ESLint](https://github.com/angular-eslint/angular-eslint)                 |
| **ESLint Recommendations**  | By incorporating ESLint's default rules, we ensure that our code follows general JavaScript best practices, which helps in maintaining a clean and maintainable codebase. | [ESLint](https://eslint.org/docs/latest/)                                       |

### ESLint Configurations Used

The following ESLint configurations are used in the project, referencing their sources for clarity:

| **Config**                   | **Description**                                                                                 | **Link**                                                                                          |
|------------------------------|-------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------|
| `flat/base`                  | Base configuration provided by Nx.                                                             | [Base Config](https://github.com/nrwl/nx/blob/master/packages/eslint-plugin/src/flat-configs/base.ts) |
| `flat/typescript`            | TypeScript configuration provided by Nx.                                                       | [TypeScript Config](https://github.com/nrwl/nx/blob/master/packages/eslint-plugin/src/flat-configs/typescript.ts) |
| `flat/javascript`            | JavaScript configuration provided by Nx.                                                       | [JavaScript Config](https://github.com/nrwl/nx/blob/master/packages/eslint-plugin/src/flat-configs/javascript.ts) |
| `flat/angular`               | Angular configuration provided by Nx.                                                          | [Angular Config](https://github.com/nrwl/nx/blob/master/packages/eslint-plugin/src/flat-configs/angular.ts) |
| `flat/angular-template`      | Angular template configuration provided by Nx.                                                 | [Angular Template Config](https://github.com/nrwl/nx/blob/master/packages/eslint-plugin/src/flat-configs/angular-template.ts) |
| `template-recommended`       | Recommended configuration for Angular templates from angular-eslint.                           | [Template Recommended](https://github.com/angular-eslint/angular-eslint/blob/main/packages/angular-eslint/src/configs/template-recommended.ts) |
| `template-accessibility`     | Accessibility configuration for Angular templates from angular-eslint.                         | [Template Accessibility](https://github.com/angular-eslint/angular-eslint/blob/main/packages/angular-eslint/src/configs/template-accessibility.ts) |

---

## TypeScript Configuration

The project uses a **TypeScript configuration** defined in `tsconfig.base.json`. Notably, strict mode is enabled:

- **Strict Mode**: The `strict` option is turned on to enforce strict type-checking across the entire codebase. This helps catch potential errors at compile time, ensuring more robust and reliable code. It promotes best practices by requiring explicit type annotations, reducing the likelihood of runtime errors and improving overall code quality.

---


## Useful links

Learn more:

- [Learn more about this workspace setup](https://nx.dev/getting-started/tutorials/angular-monorepo-tutorial?utm_source=nx_project&utm_medium=readme&utm_campaign=nx_projects)
- [Learn about Nx on CI](https://nx.dev/ci/intro/ci-with-nx?utm_source=nx_project&utm_medium=readme&utm_campaign=nx_projects)
- [Releasing Packages with Nx release](https://nx.dev/features/manage-releases?utm_source=nx_project&utm_medium=readme&utm_campaign=nx_projects)
- [What are Nx plugins?](https://nx.dev/concepts/nx-plugins?utm_source=nx_project&utm_medium=readme&utm_campaign=nx_projects)

