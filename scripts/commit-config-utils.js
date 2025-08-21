const fs = require('fs').promises;
const path = require('path');

async function getScopes() {
  const projectsPath = path.join(__dirname, '../projects.json');
  const scopes = JSON.parse(await fs.readFile(projectsPath, 'utf-8'));

  return scopes;
}

async function generateConventionalCommitConfig() {
  const scopes = await getScopes();
  const commonScopes = scopes.reduce((acc, scope) => {
    acc[scope] = {description: `Scope for ${scope}`};
    return acc;
  }, {});

  const config = {
    types: {
      "build": {"description": "Changes that affect the build system or external dependencies"},
      "chore": {"description": "Changes to auxiliary tools, libraries, or build process"},
      "ci": {"description": "Changes to CI configuration files and scripts"},
      "docs": {"description": "Documentation-only changes"},
      "feat": {"description": "A new feature"},
      "fix": {"description": "A bug fix"},
      "perf": {"description": "Changes made to improve performance"},
      "refactor": {"description": "Code refactoring that neither fixes a bug nor adds a feature"},
      "revert": {"description": "Reverts a previous commit"},
      "style": {"description": "Code style changes that do not affect functionality"},
      "test": {"description": "Adding or updating tests"},
    },
    commonScopes,
  };

  await fs.writeFile(path.join(__dirname, '../conventionalcommit.json'), JSON.stringify(config, null, 2));
}

module.exports = {
  getScopes,
  generateConventionalCommitConfig,
};

// Run `generateConventionalCommitConfig` if file is executed directly (npm run g:commitConfig)
if (require.main === module) {
  generateConventionalCommitConfig().catch(console.error);
}
