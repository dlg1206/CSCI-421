# Additional Development Information

## CLI
### Interface
Main interface is `CLI.java`. No changes need to be made when adding commands.

### I/O
The `Console` object is available to use for printing standard messages / errors and getting user input

### Commands

#### Adding commands
1. Create a new Command object in `cli/cmd/commands` that extends `Command`
2. Add command keyword string to `CommandFactory` (cli/cmd/CommandFactory)
3. Implement methods

#### Exceptions
All commands extend `CommandExecption`. Add / use as needed

## Testing
Testing works by comparing the expected output vs the actual output of the cli. 

### Adding Tests
1. Create a testing function in the [`TestRunner`](../src/test/java/TestRunner.java) class
   * Each function **MUST** return either 1 ( if failed ) or 0 ( if passed ). This is needed for the CI.
   * See [Test Utils](#test-utils) for additional testing resources
2. Add the function the main method in `TestRunner`

Run `TestRunner` using the same arguments as main to test locally. The new tests will be run automagically in the CI.

### Test Utils
The following are additional testing resources available for tests

- [`MockCLI`](../src/test/java/mocks/MockCLI.java): A mock instance of a CLI that takes in input and captures the output.

  - The constructor creates a new CLI, database, catalog, and storage manager instances.

- [`StrBuilder`](../src/test/java/util/StrBuilder.java): Custom string build to be used to build complex expected outputs or queries.

- [`Tester`](../src/test/java/util/Tester.java): Handles string comparison and pretty formatting of the results