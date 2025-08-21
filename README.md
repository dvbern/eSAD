<!-- TOC -->
* [Workspace Overview](#workspace-overview)
  * [Commit Guidelines](#commit-guidelines)
    * [Structure of the Commit Message](#structure-of-the-commit-message)
    * [Header Format](#header-format)
    * [Scope Format](#scope-format)
    * [Types](#types)
    * [Body and Footer](#body-and-footer)
    * [Commit Configuration](#commit-configuration)
<!-- TOC -->

# Workspace Overview

Welcome to the **workspace**! Currently, this workspace contains only the **frontend** directory. Below is a summary of
the relevant documentation:

| **Component**           | **Description**                                                                   | **Link**                                            |
|-------------------------|-----------------------------------------------------------------------------------|-----------------------------------------------------|
| Frontend README         | Overview of the frontend application, setup instructions, and usage guidelines.   | [Frontend README ](frontend/README.md)              |
| Frontend Library README | Details about the libraries used within the frontend, their structure, and usage. | [Frontend Library README ](frontend/libs/README.md) |
| Frontend Tools README   | Explanation of the tools available in the frontend project, including plugins.    | [Frontend Tools README ](frontend/tools/README.md)  |
| Backend README          | Overview of the api application, setup instructions and usage guidelines          | [Backend README ](api/README.md)                    |

---

## Commit Guidelines

This project uses [**Conventional Commits**](https://www.conventionalcommits.org/) and follows [**Angular Commit
Guidelines**](https://github.com/angular/angular/blob/main/CONTRIBUTING.md#-commit-message-format) to ensure a clear and
consistent commit history.

To further support these guidelines, the [Conventional Commits](https://plugins.jetbrains.com/plugin/13389-conventional-commit) plugin is used,
which provides features such as commit message templates, validation, and auto-completion.

### Structure of the Commit Message

A commit message should consist of three parts:

1. **Header**: A brief summary of the change (maximum 72 characters).
2. **Body (optional)**: A more detailed description of the changes.
3. **Footer (optional)**: Additional information such as references to issues or tickets.

### Header Format

The header should follow this format:

```
<type>(<scope>): [REFERENCE-1234] <short summary>
```

### Scope Format

The scope must be an entry from the projects.json file located in the root.
If no scope is provided, it means the commit affects all parts.
To update the scopes, run the fallowing command:

```npm run g:commitConfig```

This command generates the conventionalcommit.json, which is used by the [Conventional Commits](https://plugins.jetbrains.com/plugin/13389-conventional-commit) plugin.

**Examples:**

- `feat(esc-app): [REFERENCE-1234] add ability to add items to the cart`
- `fix(esc-app): [REFERENCE-1234] fix issue with user login`
- `docs(ui-buttons): [REFERENCE-1234] update documentation for ui buttons`
- `feat: [REFERENCE-1234] improve overall performance`

### Types

| **Type**     | **Description**                                                                            |
|--------------|--------------------------------------------------------------------------------------------|
| **build**    | Changes that affect the build system or external dependencies (e.g., Gulp, Webpack, npm)   |
| **chore**    | Changes to auxiliary tools, libraries, or build process that don't affect source code      |
| **ci**       | Changes to CI configuration files and scripts (e.g., GitLab CI, CircleCI)                  |
| **docs**     | Documentation-only changes                                                                 |
| **feat**     | A new feature                                                                              |
| **fix**      | A bug fix                                                                                  |
| **perf**     | Changes made to improve performance                                                        |
| **refactor** | Code refactoring that neither fixes a bug nor adds a feature                               |
| **revert**   | Reverts a previous commit                                                                  |
| **style**    | Code style changes (e.g., formatting, missing semicolons) that do not affect functionality |
| **test**     | Adding or updating tests                                                                   |

### Body and Footer

- **Body**: Use the body to provide detailed information about why the change was made and what it does.
- **Footer**: The footer can contain information about breaking changes and deprecations

### Commit Configuration

This project uses custom utility scripts (`commit-config-utils.js`) to manage commit scopes and enforce the commit message guidelines.

The getScopes function dynamically loads scopes from the root projects.json file, ensuring that only valid project names can be used in commit messages.
`commitlint.config.js` then enforces these scopes using scope-enum rules, which restrict commit messages to the predefined scopes

The generateConventionalCommitConfig function generates the `conventionalcommit.json` configuration file.
This function is called by running: ```npm run g:commitConfig```

> ❗The conventionalcommit.json file should not be modified directly, as it is generated automatically!
