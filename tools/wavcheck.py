"""Report format, levels, silence and mid-line gaps for voice-over wavs.

Usage: python3 -I wavcheck.py FILE [FILE ...]
"""
import array
import math
import os
import sys
import wave

FULL = 32768.0
SILENCE_DBFS = -45.0
MIN_GAP_S = 0.12


def db(x):
    return -99.0 if x <= 0 else 20 * math.log10(x / FULL)


def envelope(samples, rate, step_s=0.05):
    step = max(1, int(step_s * rate))
    for i in range(0, len(samples), step):
        seg = samples[i:i + step]
        if not seg:
            break
        yield i / rate, db(max(max(seg), -min(seg)))


def gaps(samples, rate, threshold, after):
    """Stretches of near-silence inside the line, which read as lag in game."""
    found = []
    start = None
    for i, s in enumerate(samples):
        if abs(s) <= threshold:
            if start is None:
                start = i
        else:
            if start is not None and i - start > int(MIN_GAP_S * rate) and start > after:
                found.append((start, i))
            start = None
    return found


def report(path):
    with wave.open(path, "rb") as w:
        channels, width, rate, frames = (
            w.getnchannels(), w.getsampwidth(), w.getframerate(), w.getnframes())
        raw = w.readframes(frames)

    if width != 2:
        print(f"  {width * 8}-bit, not 16 — re-save as 16-bit PCM")
        return

    a = array.array("h")
    a.frombytes(raw)
    if not a:
        print("  empty file")
        return

    peak = max(max(a), -min(a))
    rms = math.sqrt(sum(float(s) * s for s in a) / len(a))
    clipped = sum(1 for s in a if s >= 32767 or s <= -32768)
    threshold = FULL * (10 ** (SILENCE_DBFS / 20))

    first = next((i for i, s in enumerate(a) if abs(s) > threshold), None)
    last = next((i for i in range(len(a) - 1, -1, -1) if abs(a[i]) > threshold), None)

    print(f"  format        {width * 8}-bit PCM, {channels}ch, {rate} Hz")
    print(f"  duration      {frames / rate:.2f} s")
    print(f"  peak          {db(peak):+.1f} dBFS")
    print(f"  average (RMS) {db(rms):+.1f} dBFS")
    print(f"  clipped       {clipped} samples")

    if first is None:
        print("  speech        none above -45 dBFS")
        return

    print(f"  lead silence  {first / rate * 1000:.0f} ms")
    print(f"  tail silence  {(len(a) - 1 - last) / rate * 1000:.0f} ms")

    # Measure the floor only where there is no speech. A quietest-window scan over a
    # short clip just finds the quietest syllable and reports it as hiss.
    quiet = array.array("h")
    quiet.extend(a[:first])
    quiet.extend(a[last + 1:])
    for gap_start, gap_end in gaps(a, rate, threshold, first):
        quiet.extend(a[gap_start:gap_end])
    if len(quiet) >= int(0.05 * rate):
        floor = math.sqrt(sum(float(s) * s for s in quiet) / len(quiet))
        print(f"  noise floor   {db(floor):+.1f} dBFS"
              f"  (from {len(quiet) / rate * 1000:.0f} ms of silence)")
    else:
        print("  noise floor   too little silence in the file to measure")

    found = gaps(a, rate, threshold, first)
    if found:
        listed = ", ".join(
            f"{(e - s) / rate * 1000:.0f} ms at {s / rate:.2f}s" for s, e in found)
        total = sum(e - s for s, e in found) / rate
        print(f"  mid-line gaps {listed}  (total {total:.2f} s)")
        print(f"  speech only   {(frames / rate) - total:.2f} s of {frames / rate:.2f} s")
    else:
        print(f"  mid-line gaps none over {int(MIN_GAP_S * 1000)} ms")

    if "-v" in sys.argv:
        print("  envelope:")
        for t, level in envelope(a, rate):
            print(f"    {t:5.2f}s {level:+6.1f} " + "#" * max(0, int((level + 70) / 2.2)))


def main():
    paths = [p for p in sys.argv[1:] if p != "-v"]
    for path in paths:
        print(os.path.basename(path))
        try:
            report(path)
        except (wave.Error, EOFError) as exc:
            print(f"  not a readable PCM wav: {exc}")
        print()


if __name__ == "__main__":
    main()
