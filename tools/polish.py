"""Prepare raw takes for release: trim dead air, shorten mid-line pauses, match loudness.

Usage: python3 -I tools/polish.py OUTDIR FILE [FILE ...]

Originals are never modified. Loudness is matched on average level across the whole batch,
with a peak ceiling, so one loud take cannot clip and one quiet take is not left behind.
"""
import array
import math
import os
import sys
import wave

FULL = 32768.0

SILENCE_DBFS = -45.0      # below this counts as silence
LEAD_MS = 30              # dead air kept at the start
TAIL_MS = 90              # kept at the end, so a word's decay is not clipped
MAX_GAP_MS = 220          # a pause inside a line is shortened to this
TARGET_RMS_DBFS = -21.0   # average level every clip is brought to
PEAK_CEILING_DBFS = -1.5  # never louder than this, so nothing clips


def db(x):
    return -99.0 if x <= 0 else 20 * math.log10(x / FULL)


def amp(decibels):
    return FULL * (10 ** (decibels / 20))


def read(path):
    with wave.open(path, "rb") as w:
        if w.getsampwidth() != 2:
            raise ValueError(f"{os.path.basename(path)} is not 16-bit PCM")
        samples = array.array("h")
        samples.frombytes(w.readframes(w.getnframes()))
        return samples, w.getframerate(), w.getnchannels()


def write(path, samples, rate, channels):
    with wave.open(path, "wb") as w:
        w.setnchannels(channels)
        w.setsampwidth(2)
        w.setframerate(rate)
        w.writeframes(samples.tobytes())


def speech_bounds(samples, threshold):
    first = next((i for i, s in enumerate(samples) if abs(s) > threshold), None)
    if first is None:
        return None, None
    last = next(i for i in range(len(samples) - 1, -1, -1) if abs(samples[i]) > threshold)
    return first, last


def shorten_gaps(samples, rate, threshold):
    """Shorten silences inside the line; a long pause mid-sentence reads as lag in game."""
    limit = int(MAX_GAP_MS / 1000 * rate)
    out = array.array("h")
    run = 0
    for s in samples:
        if abs(s) <= threshold:
            run += 1
            if run <= limit:
                out.append(s)
        else:
            run = 0
            out.append(s)
    return out


def polish(path, outdir):
    samples, rate, channels = read(path)
    threshold = amp(SILENCE_DBFS)
    name = os.path.basename(path)

    before_peak = db(max(max(samples), -min(samples)))
    before_rms = db(math.sqrt(sum(float(s) * s for s in samples) / len(samples)))
    before_len = len(samples) / rate

    first, last = speech_bounds(samples, threshold)
    if first is None:
        print(f"{name}: no speech found, left alone")
        return None

    lead = int(LEAD_MS / 1000 * rate)
    tail = int(TAIL_MS / 1000 * rate)
    trimmed = samples[max(0, first - lead):min(len(samples), last + tail + 1)]
    trimmed = shorten_gaps(trimmed, rate, threshold)

    peak = max(max(trimmed), -min(trimmed))
    rms = math.sqrt(sum(float(s) * s for s in trimmed) / len(trimmed))
    gain = min(amp(TARGET_RMS_DBFS) / rms, amp(PEAK_CEILING_DBFS) / peak)

    out = array.array("h", (max(-32768, min(32767, int(round(s * gain)))) for s in trimmed))

    target = os.path.join(outdir, name)
    write(target, out, rate, channels)

    after_peak = db(max(max(out), -min(out)))
    after_rms = db(math.sqrt(sum(float(s) * s for s in out) / len(out)))
    return {
        "name": name,
        "before": (before_len, before_peak, before_rms),
        "after": (len(out) / rate, after_peak, after_rms),
        "gain": db(gain * FULL) - db(FULL),
    }


def main():
    outdir, paths = sys.argv[1], sys.argv[2:]
    os.makedirs(outdir, exist_ok=True)
    rows = [r for r in (polish(p, outdir) for p in paths) if r]

    print(f"{'file':44}{'length':>16}{'peak':>16}{'average':>16}")
    for r in rows:
        bl, bp, br = r["before"]
        al, ap, ar = r["after"]
        print(f"{r['name'][:43]:44}"
              f"{bl:5.2f} -> {al:4.2f}s{'':2}"
              f"{bp:+6.1f} -> {ap:+5.1f}{'':2}"
              f"{br:+6.1f} -> {ar:+5.1f}")
    if rows:
        spread = max(r["after"][2] for r in rows) - min(r["after"][2] for r in rows)
        print(f"\nloudness spread after: {spread:.1f} dB across {len(rows)} clips")


if __name__ == "__main__":
    main()
