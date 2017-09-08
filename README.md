# Poker game engine
This repository contains the engine for the poker games for the Riddles.io platform.

## Setting up

This guide assumes the following software to be installed and globally
accessible:

- Gradle 2.14
- JVM 1.8.0_91

## Opening this project in IntelliJ IDEA

- Select 'Import Project'
- Browse to project directory root
- Select build.gradle
- Check settings:
- * Use local gradle distribution
- * Gradle home: /usr/share/gradle-2.14
- * Gradle JVM: 1.8
- * Project format: .idea (directory based)

*Note: for other IDEs, look at online documentation*

## Building the engine

Use Gradle to build a .jar of the engine. Go to Tasks -> build -> jar.  
The .jar file can be found at `build/libs/`.

## Running 

Running is handled by the MatchWrapper. This application handles all communication between
the engine and bots and stores the results of the match. To run, firstly edit the 
`wrapper-commands.json` file. This should be pretty self-explanatory. Just change the command
fields to the right values to run the engine and the bots. In the example, the starterbot
is run twice, plus the command for the engine built in the previous step.
 
To run the MatchWrapper, use the following command (Linux):
````
java -jar match-wrapper*.jar "$(cat wrapper-commands.json)"
````
You can also use the run_wrapper.sh file, which contains this line.

*Note: if running on other systems, find how to put the content of wrapper-commands.json as
argument when running the match-wrapper.jar*