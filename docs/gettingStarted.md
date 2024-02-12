## Getting Started

To run the application:

**A.** Ensure Java, Maven and MySQL are installed.


**B.** Depending on your platform:



**For MacOS:**

1. Set up a new MySQL connection or make use of an existing one.
2. Export your MySQL USERNAME and PASSWORD as environment variables:

   Open the terminal and type the following commands to open the shell profile file:

   For bash shell:
    ```bash
    nano ~/.bashrc
    ```

   For zsh shell:
    ```bash
    nano ~/.zshrc
    ```

   Add these lines at the end of the file:
    ```bash
    export WATCHDOG_MYSQL_USERNAME="example_username"
    export WATCHDOG_MYSQL_PASSWORD="example_password"
    ```

   Replace "example_username" and "example_password" with your actual MySQL username and password. Press Ctrl + X to close the editor, followed by Y to save changes, and Enter to confirm the file name. To make these changes take effect, close and reopen your terminal.



**For Windows:**

1. Set up a new MySQL connection or make use of an existing one.
2. Export your MySQL USERNAME and PASSWORD as environment variables:

   Open Command Prompt as an administrator.

   To set the environment variables, use the setx command followed by the variable name and its value. For example:

    ```cmd
    setx WATCHDOG_MYSQL_USERNAME "example_username"
    setx WATCHDOG_MYSQL_PASSWORD "example_password"
    ```

   Replace "example_username" and "example_password" with your actual MySQL username and password.
   Close and reopen Command Prompt to make sure the changes take effect.


**C.** Ensure docker desktop is running and kubernetes is enabled in settings


**D.** Clone the repository.
```bash
    git clone https://github.com/zarkk01/docker-watchdog.git
```
**E.** Navigate to the root directory and run `mvn install`

**F.**  Run `java -jar target/watchdog.jar`

**G.** Enhance your workflow with Watchdog