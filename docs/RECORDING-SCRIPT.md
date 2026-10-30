# Recording script

Lines to read for each event. They are suggestions — swap in your own phrasing, the plugin only
cares which folder a file lands in.

## Before you start

- **Format:** `.wav`, 44100 Hz, 16-bit PCM, mono. Most recorders call this "CD quality".
- **Length:** under about four seconds per line. Shorter is better — these play over gameplay.
- **Level:** record at a comfortable, consistent level across every line. The plugin has one volume
  setting for everything, so a line recorded quietly stays quiet relative to the rest.
- **Room:** trim silence off the start of each file. A half-second of dead air before the line
  makes the plugin feel laggy.
- **Takes:** record three to five variations per event where you can. The plugin picks at random and
  never repeats the last one, so variety is what stops it getting old.

## Naming and filing

Filenames do not matter, only the folder. Numbering keeps them tidy:

```
death/01-oh-dear.wav
death/02-not-again.wav
death/03-thats-unfortunate.wav
```

Drop them in `~/.runelite/female-voiceover/<folder>/` (Windows:
`%USERPROFILE%\.runelite\female-voiceover\<folder>\`), then type `::vo reload` in game.

Audition a take with `::vo test death`.

---

## `death` — you died

Fires on every death, including safe ones. Will be heard often. Gentle teasing wears better than
outright mockery.

- "Oh dear. You are dead."
- "Well, that happened."
- "And there goes the loot."
- "Not your finest work."
- "You'll get it next time. Probably."

## `quest` — quest complete

The quest scroll just appeared. Warm and congratulatory.

- "Quest complete."
- "Another one down. Nicely done."
- "That's the quest finished."
- "Quest points well earned."

## `level-up` — a skill levelled

By default this only fires on every tenth level and on 99, so it can be a little more of an
occasion than you'd think.

- "Level up."
- "Nice one."
- "Another level in the bank."
- "Look at you, getting better at things."

## `level-99` — a skill hit 99

Optional. Record these and a 99 gets its own reaction instead of the usual level-up line.

- "Ninety-nine! Congratulations."
- "That's a skill cape. Go and get it."
- "Maxed. Genuinely well done."

## `diary` — achievement diary tasks finished

- "Diary complete."
- "That's the diary done."
- "Achievement unlocked."

## `combat-task` — combat achievement

- "Combat task complete."
- "That's another one ticked off."
- "Combat achievement. Nice."

## `collection-log` — new collection log slot

Fires on each new slot, so it can come in bursts on a good drop.

- "New collection log slot."
- "That's a new one for the log."
- "Collection log filled a little more."

## `pet` — the funny feeling

The rarest thing on this list. Worth your best take.

- "A pet! Finally!"
- "You have a funny feeling... you got the pet."
- "No way. The pet dropped."
- "After all that — a pet."

## `clue` — Treasure Trail finished

- "Clue complete."
- "Treasure Trail finished."
- "Let's see what's in it."

## `slayer` — slayer task finished

- "Task complete."
- "Slayer task done. Go get another."
- "That's the task finished."

## `personal-best` — new personal best

Enthusiastic. This is someone's good run.

- "New personal best!"
- "That's a personal best. Fast."
- "Personal best. Very nice."

## `valuable-drop` — a valuable drop

Off by default because it fires a lot. Keep these very short.

- "Valuable drop."
- "Ooh, that's worth something."
- "Nice drop."

## `login` — logging in

Off by default. A greeting as you load in.

- "Welcome back."
- "Back for more, then."
- "Let's get to it."
