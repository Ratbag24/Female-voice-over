# Female Voice Over

A RuneLite plugin that speaks a recorded line when something happens in Old School RuneScape:
you die, you finish a quest, you level up, a pet drops.

Recordings are read from a folder on your own machine, so re-recording a line and hearing it in
game takes a file copy and `::vo reload` — no rebuild, no Java.

## What it announces

| Folder | Fires when |
| --- | --- |
| `death` | You die |
| `quest` | The quest completion scroll appears |
| `level-up` | A skill levels (thinned out by the **Announce levels** setting) |
| `level-99` | A skill hits 99, if you record separate lines for it |
| `diary` | You finish a set of achievement diary tasks |
| `combat-task` | You complete a combat achievement |
| `collection-log` | A new item is added to your collection log |
| `pet` | You get the funny feeling |
| `clue` | You finish a Treasure Trail |
| `slayer` | You finish a slayer task |
| `personal-best` | You set a new personal best |
| `valuable-drop` | A valuable drop is announced (off by default — it fires a lot) |
| `login` | You log in (off by default) |

Every event has its own on/off switch in the plugin's settings.

## Adding voice lines

The plugin creates its audio folder the first time it starts, with one directory per event and a
`README.txt` repeating the basics:

| OS | Folder |
| --- | --- |
| Windows | `%USERPROFILE%\.runelite\female-voiceover` |
| macOS / Linux | `~/.runelite/female-voiceover` |

Drop `.wav` files into the folder for the event they belong to:

```
~/.runelite/female-voiceover/
  death/
    01-oh-dear.wav
    02-again.wav
  quest/
    01-quest-complete.wav
```

- **Any number of clips per folder.** One is picked at random each time, and the same clip is
  never played twice in a row.
- **Filenames do not matter**, only which folder a clip is in.
- **Format:** `.wav`, 44100 Hz, 16-bit PCM. Mono is fine. `.au` and `.aiff` also work.
- Keep lines short — under about four seconds. A long line is still playing when the next thing
  happens.
- A single clip can sit beside the folders as `death.wav` instead of in `death/`.

[docs/RECORDING-SCRIPT.md](docs/RECORDING-SCRIPT.md) has lines to read for each event.

## In-game commands

| Command | Does |
| --- | --- |
| `::vo` | Lists what is loaded, and where from |
| `::vo reload` | Re-reads the audio folder after you add or change files |
| `::vo folder` | Prints the audio folder path |
| `::vo test death` | Plays a line for that event, ignoring its on/off switch |

`::vo test` takes any folder name from the table above.

## Shipping clips with the plugin

Clips can also travel inside the jar, so a user hears something without recording anything:

1. Commit the `.wav` files under `src/main/resources/com/femalevoiceover/sounds/<event>/`.
2. Add one line per file to `src/main/resources/com/femalevoiceover/sounds/index.txt`. A jar cannot
   be listed like a directory, so that index is how the plugin finds them.

Anything in a user's own folder replaces the bundled clips **for that event only** — someone can
re-record just the death lines and keep the rest.

Keep an eye on jar size if you intend to submit to the Plugin Hub; a short mono wav per line is
small, a full set of long stereo takes is not.

## Building

```sh
./gradlew build     # compile and run the tests
./gradlew run       # start RuneLite with the plugin loaded
```

Java 11 or newer. The first build downloads RuneLite from `repo.runelite.net`.

## How detection works

Most events are read from the game's own chat messages, which is why they keep working across
updates. Two are not:

- **Death** comes from the client's actor-death event, with the "Oh dear, you are dead!" message as
  a second route. Both arriving is fine — one line per event per three seconds.
- **Quest complete** comes from the quest completion interface opening (group 153). If Jagex ever
  renumbers that interface, quest lines stop firing and nothing else is affected.

Level ups are read from the "you've just advanced your" message rather than from skill changes,
because the client replays every skill on login and a replay is indistinguishable from a real level.

## Credits

Voice: *to be credited.*

## License

BSD 2-Clause. See [LICENSE](LICENSE).
