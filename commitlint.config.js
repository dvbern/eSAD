const commitUtils = require("./scripts/commit-config-utils");

async function getConfig() {
  return {
    extends: ['@commitlint/config-conventional'],
    rules: {
      'scope-empty': [1, 'never'],
      'scope-enum': async () => [
        2, 'always', [...await commitUtils.getScopes()],
      ],
    },
  };
}

module.exports = getConfig();
