# tools

## wavcheck.py

Vets incoming recordings before they are committed as a pack.

```sh
python3 -I tools/wavcheck.py takes/*.wav        # summary per file
python3 -I tools/wavcheck.py -v takes/line.wav  # plus an amplitude envelope
```

Reports format, duration, peak and average level, clipping, lead and tail silence, the noise
floor measured from the silent parts only, and any pause inside a line long enough to read as
lag in game.

What to look for:

- **clipped samples above zero** — the one fault that cannot be repaired. Ask for a re-record.
- **peak above about -1 dBFS** — no headroom; the next take will clip.
- **mid-line gaps** — a pause over ~300 ms makes the plugin feel like it is stuttering.
- **noise floor above about -45 dBFS** — audible hiss behind the voice.

Loudness differences between files do not matter; the pack is normalised as a whole.
