<div align="center">

# Liber Ivonis 伊波恩之书

*Minecraft Modpack Screen Hub*

</div>

![Hub](./img/hub.png)

## Introduction

In a modpack with many mods, we need to bind many keys only to open a certain screen; Liber Ivonis is a mod that provides a screen hub to collect those screens.

[ModernUI](https://www.curseforge.com/minecraft/mc-mods/modern-ui) is required to run this mod.

## Feature

- A Screen Hub to collect screens 
- Open with the Liber Ivonis item, key, or FTB Library button
- Integration of FTBQuests, Patchouli, Modonomicon, GuideME & Ageratum
- Support custom items & screens with a simple configuration
- Configurable color

## Configuration

Client: `config/liber_ivonis-client.toml`

```toml
[hub]
# Hub entries in display order
# Built-in: id|category
# Handbook: item:modid:item_id|title|category
# Screen: screen:class|method|title|category
# title & category support translation key, use `key:title.ageratum.guidebook`
# The params of method should be (Screen, Minecraft, ServerPlayer, Level) or no param
# The Return Type should be a sub-class of Screen
entries = [
    "controls|key:category.base", 
    "ftbquests|Task", # Also "screen:dev.ftb.mods.ftbquests.client.FTBQuestsClient|openGui|key:entry.liber_ivonis.ftbquests|Task"
    "patchouli|Handbook", 
    "modonomicon|Handbook", 
    "guideme|Handbook", 
    "ageratum|Handbook", 
    "advancement|Task", 
    "item:ageratum:guidebook|key:title.ageratum.guidebook|Handbook",
    "screen:com.github.einjerjar.mc.keymap.client.gui.screen.KeymapScreen|<init>|KeyMapping (Visual)|key:category.base", 
    "screen:xin.vanilla.narcissus.integration.ScreenHelper|openScreen|Waypoints|Narcissus", 
    "screen:xin.vanilla.narcissus.integration.ScreenHelper|openAccessListScreen|Access List|Narcissus"
]


[hub.colors]
# Hexadecimal colours in #AARRGGBB or #RRGGBB format. #RRGGBB is fully opaque.
background = "#FF0D0F15"
surface = "#FF171A23"
surfaceAlt = "#FF2E3547"
accent = "#FFF2F5F9"
muted = "#FFAEB8C6"
outline = "#FF7FD9F5"
disabledOutline = "#FF2B3244"
```

Common: `config/liber_ivonis-common.toml`

```toml
[gameplay]
giveBookOnStart = true
```
