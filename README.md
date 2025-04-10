# EMSE-ABSD JADE World of Agents (Maven version)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://gitlab.com/upm-emse-absd/jade-skeleton-gradle/-/blob/main/LICENSE)

## Introduction

JADE Project for ABSD groups ready to be imported from Git as Gradle-build project in Intellij IDEA.
The project includes both platform and tribe containers with a naive tribe agent trying to
communicate with the platform agent. The project also includes the basic functionality to load
agents from other tribes as .jar imports.

## Requirements
- Java 1.8 (JDK 17 recommended)
- JUnit 5.8.1
- Maven 3.8.0
- Lombok 1.18.22 (optional for reduce Java verbosity, refer to [https://projectlombok.org](https://projectlombok.org))

## Structure

- `lib`: folder for libraries
- `out`: .jar artifacts to export the agents and run the game.
- `src.es.upm.emse.absd`: root module with the .java files to develop
    - `teamX`: module directory of each group.
    - `Main.java`: configurable main class that launches the project and imported agents from other teams.
    - `Utils.java`: Utility class to implement reusable functions.
- `test`: JUnit testbed.
- `tribes`: folder for importing tribe agents from other teams as libraries.

## Example of use

    > java -jar woa-team0.jar -h
    --------------------------------------
    --- EMSE-ABSD JADE World of Agents ---
    --------------------------------------
    Agents:
        + Platform:
            - AgPlatform: An agent that responds when its called by its name.
        + Tribe:
            - AgTribe: An agent that try to talk with his/her colleague.
    Usage: java -jar woa.jar [options]
        + options:
            -h, --help  Prints help
            -d, -debug  Run JADE agents from the compilation
            -b, -build  Run agents from generated jars

## Copyright and licence

Copyright (c) Jose María Barambones under the [MIT License](https://gitlab.com/upm-emse-absd/skeletons-and-examples/jade-woa-skeleton-maven/-/blob/master/LICENSE).