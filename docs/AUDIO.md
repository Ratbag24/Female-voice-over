# Audio notes

Reference for anything that goes wrong with sound. For what to record, see
[RECORDING-SCRIPT.md](RECORDING-SCRIPT.md).

## Supported formats

The plugin plays whatever `javax.sound.sampled` opens with no extra codecs on the classpath:

| Extension | Notes |
| --- | --- |
| `.wav` | Use this. 44100 Hz, 16-bit PCM. |
| `.au`, `.aif`, `.aiff` | Work, but there is no reason to prefer them. |

**MP3, OGG, FLAC and M4A do not play.** Nothing in a stock Java runtime decodes them. Converting is
one command:

```sh
ffmpeg -i line.mp3 -ar 44100 -ac 1 -c:a pcm_s16le line.wav
```

A `.wav` can still fail to open if it is 24-bit, 32-bit float, or a compressed variant wearing a
`.wav` extension. The error names the file and says to re-save it as 16-bit PCM; the command above
does that too.

## Nothing plays

Work down this list:

1. `::vo` — does the plugin see your clips? If a folder reads zero, the files are in the wrong place
   or have the wrong extension.
2. `::vo reload` after adding files. The folder is read on startup and on reload, not continuously.
3. `::vo test death` — plays regardless of the event's on/off switch, so it separates "detection
   never fired" from "audio is broken".
4. Check the event's switch and the volume in the plugin settings. Volume 0 is silent.
5. Check RuneLite's log for `Could not play` — it names the file and the reason.

## One line at a time

Only one clip plays at once. Two events landing together would otherwise talk over each other.
**Interrupt the current line** in the settings chooses which one wins: off (the default) lets the
first line finish and drops the second, on cuts the first off.

Separately, each event is limited to one line every three seconds. That is what stops a death being
announced twice when both the actor-death event and the "Oh dear, you are dead!" message arrive, and
what keeps a multi-item drop from firing the collection log line repeatedly.

## Clip selection

Per event, the plugin picks at random from the clips available and never plays the same file twice
in a row. With one clip installed that one plays every time; with two they alternate.

Clips in your own audio folder replace the bundled ones **for that event only**.
