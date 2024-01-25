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