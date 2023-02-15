# Rakus
Rakus is a bot developed for [Galaxio](https://github.com/EntelectChallenge/2021-Galaxio), the 2021 Entelect Challenge. It uses greedy algorithm, which is outlined below.

## Greedy Algorithm Overview
The greedy algorithm is mostly implemented in `BotState.java`
in the directory `src/main/java/Rakus`. In this file, a custom
data structure called `BotState` is implemented. Each `BotState`
consists of two lambda expressions, each implementing a functional
interface.

The first lambda expression is used to determine its `BotState` score
based on the state of the game. The second is used to specify what
action will the bot do should its `BotState` for the current game tick
(a tick is like a single turn in the game).

The `getNextState` method is used to determine which `BotState` has
the highest score and returns it. The action associated with the chosen
`BotState` is set to be executed in the `computeNextPlayerAction` method.
However, no matter what, the `BotState` `ANY` is always executed.
The `BotState` `ANY` contains every actions that should be done in
every tick.

## Prerequisites
- [Oracle OpenJDK 11 or later](https://www.oracle.com/java/technologies/downloads/)
- [.Net Core 3.1](https://dotnet.microsoft.com/en-us/download/dotnet/3.1)
- [Apache Maven 3.9.0 or later](https://maven.apache.org/download.cgi)

## Directory Structure
```text
├── doc             # Contains report for the project
├── src             # Contains source code for the program
├── target          # Contains executable file after building from source
├── Dockerfile
└── pom.xml
```

## Building from Source and Running the Game
1. Make sure you have downloaded the [`starter-pack.zip`](https://github.com/EntelectChallenge/2021-Galaxio/releases/tag/2021.3.2)
2. [Download](https://github.com/noelsimbolon/Tubes1_Rakus/archive/refs/heads/main.zip) this repository as a ZIP file, extract it,
   and extract the `starter-pack.zip` to the root directory of this repository (the same directory as `src`)
3. Set the number of bots (the `BotCount` field) you want to run in `appsettings.json` in the folder `runner-publish` and `engine-publish` inside the
   `starter-pack` folder
4. Open the `runner-publish` folder and execute the following command.

    ```shell
    start "" dotnet GameRunner.dll
    ```
5. Open the `engine-publish` folder and execute the following command.

    ```shell
    start "" dotnet Engine.dll
    ```
6. Open the `logger-publish` folder and execute the following command.

    ```shell
    start "" dotnet Logger.dll
    ```
7. Run as many bots as the specified value in the `BotCount` field you have set.\
   To run a template reference bot, open the `reference-bot-publish` folder
   and execute the following command.

   ```shell
    start "ReferenceBot" dotnet ReferenceBot.dll
    ```
   To run the Rakus bot, you need to build the bot from source first, using Maven.

   ```shell
    start "" mvn clean package
    ```
   Then, open the `target` folder and run the JAR file using `java`.

   ```shell
    start "Rakus" java -jar Rakus.jar
    ```

For your convenience, everything above can be done with the following batch script.
The script runs 3 reference bots and a single Rakus bot.
```shell
:: Run this script from the root folder

echo "Building JAR file with Maven..."
start "" mvn clean package

@echo off
:: Game Runner
cd ./starter-pack/runner-publish/
start "" dotnet GameRunner.dll

:: Game Engine
cd ../engine-publish/
timeout /t 1
start "" dotnet Engine.dll

:: Game Logger
cd ../logger-publish/
timeout /t 1
start "" dotnet Logger.dll

:: Three default bots
cd ../reference-bot-publish/
timeout /t 3
start "ReferenceBot" dotnet ReferenceBot.dll
timeout /t 3
start "ReferenceBot" dotnet ReferenceBot.dll
timeout /t 3
start "ReferenceBot" dotnet ReferenceBot.dll
timeout /t 3

:: Rakus bot
cd ../../target/
start "Rakus" java -jar Rakus.jar

pause
```

To visualize what is actually happening in the game, you need to use the visualiser.
Extract `Galaxio-windows.zip` in the `starter-pack/visualiser` directory and run `Galaxio.exe`.
Next, open the `Options` menu and copy the `logger-publish` path to `Log Files Location`, then save.
Open the `Load` menu, select the JSON file you want to load from the `Game Log` dropdown menu.
Finally, click `Start`.

## Authors
| Name              | GitHub                                                |
|-------------------|-------------------------------------------------------|
| Noel Simbolon     | [noelsimbolon](https://github.com/noelsimbolon)       |
| Jericho Sebastian | [JerichoFletcher](https://github.com/JerichoFletcher) |
| Moh. Aghna Abyan  | [AghnaAbyan](https://github.com/AghnaAbyan)           |
