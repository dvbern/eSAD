import * as fs from 'fs';
import * as path from 'path';
import * as process from 'process';

// Function to sort an object by its keys
function sortObjectByKeys(obj: Record<string, any>): Record<string, any> {
  const sortedEntries = Object.entries(obj).sort(([keyA], [keyB]) => keyA.localeCompare(keyB));
  return Object.fromEntries(
    sortedEntries.map(([key, value]) => [
      key,
      value && typeof value === 'object' && !Array.isArray(value) ? sortObjectByKeys(value) : value,
    ]),
  );
}

// Normalize JSON files matching a regex
function normalizeJsonFiles(directory: string, languages: string): void {
  const files = fs.readdirSync(directory, { withFileTypes: true });

  // exclude .nx and dist directories
  const forbiddenDirectoryRegex = /(\.nx|dist|node_modules|\.angular)/;

  const regex = new RegExp('(apps|libs).*((' + languages + ')\\.json)+$');

  files.forEach(file => {
    const filePath = path.join(directory, file.name);

    if (file.isDirectory()) {
      // Recursively process subdirectories
      if (!forbiddenDirectoryRegex.test(filePath)) {
        normalizeJsonFiles(filePath, languages);
      }
    } else if (file.isFile() && regex.test(filePath) && file.name.endsWith('.json')) {
      try {
        // Read and parse the JSON file
        const data = fs.readFileSync(filePath, 'utf8');
        const json = JSON.parse(data);

        // Sort the JSON and overwrite the file
        const sortedJson = sortObjectByKeys(json);
        fs.writeFileSync(filePath, JSON.stringify(sortedJson, null, 2), 'utf8');
        console.log(`Normalized: ${filePath}`);
      } catch (error) {
        console.error(`Error processing ${filePath}:`, error);
      }
    }
  });
}

// Main function to parse arguments and run normalization
function main() {
  const args = process.argv.slice(2);
  if (args.length < 1) {
    console.error('Usage: ts-node normalizeJson.ts <file-path-regex>');
    process.exit(1);
  }

  const languages = args[0];
  const workspaceRoot = path.resolve(__dirname, '..'); // Adjust if needed

  normalizeJsonFiles(workspaceRoot, languages);
}

main();
