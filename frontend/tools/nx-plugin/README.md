<!-- TOC -->
* [Nx Plugin](#nx-plugin)
  * [Features](#features)
  * [Lib Generator](#lib-generator)
    * [Usage with command](#usage-with-command)
    * [Usage in Nx Console](#usage-in-nx-console)
    * [Types of Libraries](#types-of-libraries)
    * [Library Scopes](#library-scopes)
    * [Package Name Prefix](#package-name-prefix)
    * [Prefix Mapping](#prefix-mapping)
<!-- TOC -->

# Nx Plugin

This library was generated with [Nx](https://nx.dev) and serves as a custom generator.

## Features

- Quickly generate new libraries with a predefined structure.
- Ensure consistency and adherence to best practices across all libraries.

## Lib Generator

The library generator is designed to help create new libraries while prompting for necessary information based on the type and scope of the library.
Below are some key elements of the generator:

### Usage with command
```bash
npx nx g @dvb/nx-plugin:lib
```
### Usage in Nx Console

The Plugin can also be used directly from the Nx Console, providing a user-friendly interface for generating libraries.
You can access the plugin features through the Nx Console extension in your IDE (available for both VSCode and IntelliJ).

You have to type "dvb"

### Types of Libraries

- **LibType**: The generator supports the following library types:
  - `feat`: Features containing smart components and business logic.
  - `ui`: User Interface containing "dumb" components.
  - `util`: Libraries that contain helper functions or reusable logic.
  - `data-access`: Responsible for communicating with APIs or databases.

### Library Scopes

- **LibScope**: The generator allows for two types of library scopes:
  - `shared`: Libraries that can be shared across multiple applications.
  - `app-specific`: Libraries intended for use within a specific application.

### Package Name Prefix

The default package name prefix is set to:

```typescript
export const PACKAGE_NAME_PREFIX = 'dvb';
```

### Prefix Mapping

The following prefix mapping is used for different library types:

| **Library Type** | **Prefix** |
|------------------|------------|
| `feat`           | `feat`     |
| `ui`             | `svui`     |
| `util`           | `util`     |
| `data-access`    | `dacc`     |

---
