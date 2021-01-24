# Grave Keeper for 1.12.2
[![Grave Keeper Curse statistics](http://cf.way2muchnoise.eu/grave-keeper.svg)](http://minecraft.curseforge.com/projects/grave-keeper)

A grave modification for Minecraft with a special attention to integration and balance with other mods. Currently in progress.

If you would like to help, find an issue and then fork the repository. If you can fix it, submit a pull request and we will accept it! This is valid even if you dont know how to code, modifications to textures, resources, wikis, and everything else are up for improvment.

See mcmod.info for credits.

See the official Discord [here](https://discord.gg/0ZanfS3S9yu3Wf2M).

## Installation

1.  Download GraveKeeper.jar from the [Curse website](https://www.curseforge.com/minecraft/mc-mods/grave-keeper) and put it in your mods folder.
2.  Get some soulbound items, die, enjoy.

## Developping

To setup you development environment:
1.  From the GraveKeeper mod folder, type:
```shell
./gradlew setupDecompWorkspace
```
2.  Start IdeaJ.
3.  Import the gradle project.
4.  Respect existing coding style.
5.  Create run configuration using gradle, select the gradle project, enter the task `runClient` or `runServer`.
