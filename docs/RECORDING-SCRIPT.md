# Recording script

Sixty short lines, with the tone each one wants and the filename to save it as.

Nothing here is fixed. Every event works with as few as one clip, and an event with no
recordings simply stays silent, so skipping lines or rewording them costs nothing.

There is a version of this script formatted for reading off a phone while recording, with
tick-boxes to track progress. Ask whoever set this up for the link.

## Before you start

| | |
| --- | --- |
| **Format** | `.wav`, 16-bit PCM, 44100 Hz, mono. Most recorders call this CD quality. Not MP3 — it will not play. |
| **Length** | Under four seconds a line. Shorter is better. |
| **Level** | Same distance from the mic and the same level for every line, start to finish. There is one volume control for all sixty, so a quietly recorded line stays quiet. |
| **Trim** | Cut the silence off the front of each file. Dead air at the start is what makes this kind of plugin feel broken. |
| **Naming** | Name each file after its line: `death-01.wav`, `quest-03.wav`. |
| **Sending** | All in one batch. Folders are not needed; they get sorted on the other end. |

One thing worth knowing before the first take: these play hundreds of times. The lines that
survive that are the under-played ones — small, warm, unhurried. A big performance is funny
twice and grating by the tenth time it fires. Keep almost everything low-key; the pet lines are
the one place to let go.

## Filing clips by hand

Only needed if you are installing them yourself rather than sending them on. Drop each clip into
the folder named in its heading below, under `~/.runelite/female-voiceover/` (Windows:
`%USERPROFILE%\.runelite\female-voiceover\`), then type `::vo reload` in game. `::vo test death`
auditions a take.

---

# Sitting one — the three you hear constantly

Nineteen lines, about fifteen minutes. The plugin is worth using with only these recorded.

## `death` — heard constantly

Fires on every death, including safe ones.

> In game: `Oh dear, you are dead!`

Dry, a little sympathetic, never gloating. The most-heard line by a wide margin, so keep it
small: a raised eyebrow rather than a laugh. Eight variations because of how often it fires.

| File | Line |
| --- | --- |
| `death-01.wav` | "Oh dear. You are dead." |
| `death-02.wav` | "Well. That happened." |
| `death-03.wav` | "And there goes the loot." |
| `death-04.wav` | "Hm. Not your finest work." |
| `death-05.wav` | "You'll get it next time. Probably." |
| `death-06.wav` | "That one's going to sting." |
| `death-07.wav` | "Back to Lumbridge, then." |
| `death-08.wav` | "Nothing unlucky about that one." |

## `quest` — sometimes

> In game: the quest completion scroll opens

Warm and genuine. A quest can be hours of work finishing, so this one earns a real note of
congratulation.

| File | Line |
| --- | --- |
| `quest-01.wav` | "Quest complete." |
| `quest-02.wav` | "Another one down." |
| `quest-03.wav` | "That's the quest finished. Nicely done." |
| `quest-04.wav` | "Quest points well earned." |
| `quest-05.wav` | "Look at that. Done." |

## `level-up` — often

> In game: `Congratulations, you've just advanced your Attack level.`

Light and quick. By default it only fires every tenth level, so it can land as a small nod
rather than a fanfare.

| File | Line |
| --- | --- |
| `level-up-01.wav` | "Level up." |
| `level-up-02.wav` | "Nice one." |
| `level-up-03.wav` | "Another level in the bank." |
| `level-up-04.wav` | "You're getting good at this." |
| `level-up-05.wav` | "That's a new level." |
| `level-up-06.wav` | "Progress." |

---

# Sitting two — the rarer moments

Forty-one lines. No rush: each event stays silent until it has a clip, so they can arrive
whenever.

## `pet` — very rarely

> In game: `You have a funny feeling like you're being followed.`

The rarest thing on this list; people hunt these for weeks. Genuine disbelief and delight. This
is the one place to go big, and worth the best takes.

| File | Line |
| --- | --- |
| `pet-01.wav` | "A pet! Finally!" |
| `pet-02.wav` | "No way. The pet dropped." |
| `pet-03.wav` | "You got the pet!" |
| `pet-04.wav` | "After all that... a pet." |
| `pet-05.wav` | "There it is! The pet!" |

## `level-99` — rarely

> In game: a skill reaches level 99

A 99 can be weeks of effort. These only play if they exist; otherwise a 99 gets an ordinary
level-up line. Real delight here.

| File | Line |
| --- | --- |
| `level-99-01.wav` | "Ninety-nine! Congratulations." |
| `level-99-02.wav` | "That's a skill cape. Go and get it." |
| `level-99-03.wav` | "Maxed. Genuinely well done." |
| `level-99-04.wav` | "Ninety-nine. All that work paid off." |

## `personal-best` — sometimes

> In game: `Fight duration: 1:23. Personal best: 1:20.`

Impressed and energetic. Someone just had their fastest run at something.

| File | Line |
| --- | --- |
| `personal-best-01.wav` | "New personal best!" |
| `personal-best-02.wav` | "That's a personal best. Fast." |
| `personal-best-03.wav` | "Quickest one yet." |
| `personal-best-04.wav` | "Personal best. Very nice." |

## `clue` — often

> In game: `You have completed 12 medium Treasure Trails.`

Curious, anticipating. About to find out what is in the reward casket.

| File | Line |
| --- | --- |
| `clue-01.wav` | "Clue complete." |
| `clue-02.wav` | "Treasure Trail finished." |
| `clue-03.wav` | "Let's see what's inside." |
| `clue-04.wav` | "That's the clue done." |

## `slayer` — often

> In game: `You have completed your task!`

Efficient, onto-the-next. Slayer is a grind and the player is already walking back for another
assignment.

| File | Line |
| --- | --- |
| `slayer-01.wav` | "Task complete." |
| `slayer-02.wav` | "Slayer task done." |
| `slayer-03.wav` | "That's the task finished. Go get another." |
| `slayer-04.wav` | "Done. Next one." |

## `diary` — sometimes

> In game: `Well done! You have completed all of the easy tasks in the Varrock area.`

Satisfied and matter-of-fact. Ticking a box on a long list.

| File | Line |
| --- | --- |
| `diary-01.wav` | "Diary complete." |
| `diary-02.wav` | "That's the diary done." |
| `diary-03.wav` | "Another set of tasks ticked off." |
| `diary-04.wav` | "All done in that area." |

## `combat-task` — often

> In game: `Congratulations, you've completed a hard combat task: Kill the Kalphite Queen.`

Brisk and approving. These arrive in runs of three or four, so keep them clipped.

| File | Line |
| --- | --- |
| `combat-task-01.wav` | "Combat task complete." |
| `combat-task-02.wav` | "That's another one down." |
| `combat-task-03.wav` | "Combat achievement. Nice." |
| `combat-task-04.wav` | "Ticked off." |

## `collection-log` — often

> In game: `New item added to your collection log: Abyssal whip`

Quietly pleased, understated. A good drop fills two or three slots at once, so these need to sit
politely next to each other.

| File | Line |
| --- | --- |
| `collection-log-01.wav` | "New collection log slot." |
| `collection-log-02.wav` | "That's a new one for the log." |
| `collection-log-03.wav` | "Another slot filled." |
| `collection-log-04.wav` | "The log's looking better." |

## `valuable-drop` — off by default

> In game: `Valuable drop: Dragon bones (2,900 coins)`

Very short and low-key, almost offhand. Fires often enough that it ships switched off, so these
are the least important four on the list.

| File | Line |
| --- | --- |
| `valuable-drop-01.wav` | "Valuable drop." |
| `valuable-drop-02.wav` | "Ooh. That's worth something." |
| `valuable-drop-03.wav` | "Nice drop." |
| `valuable-drop-04.wav` | "Worth picking up." |

## `login` — off by default

> In game: the player logs in

Relaxed, like greeting someone at the door. The first thing said each session, so nothing too
bright.

| File | Line |
| --- | --- |
| `login-01.wav` | "Welcome back." |
| `login-02.wav` | "Back for more, then." |
| `login-03.wav` | "Let's get to it." |
| `login-04.wav` | "Right. Where were we?" |
