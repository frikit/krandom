"""Splits a JFR recording of SchemaOutputBenchmark into the cost categories of the 3.1 schema-speed spike.

Usage: python3 -I 2026-11-12-schema-speed-spike-jfr-split.py <profile.jfr>...

Record with JMH's JFR profiler and settings whose jdk.ExecutionSample period is 1 ms (jdk.NativeMethodSample
keeps its 20 ms), for example:
  -prof jfr:dir=<dir>;configName=<copy of profile.jfc with the Java period at 1 ms>;stackDepth=128

Each sample of a JMH worker thread is filed under the innermost of these frames. Java samples do not see native
code, so a native sample counts as many Java samples as its period is longer (20).
"""
import collections
import os
import re
import subprocess
import sys

JFR = os.path.join(os.environ.get("JAVA_HOME", "/usr"), "bin", "jfr")
NATIVE_WEIGHT = 20
MARKERS = [
    ("FieldLookup.valueConfig(", "config build"),
    (".reseedTo(", "reseed"),
    ("StreetAddressGenerator.loadSecondaryUnits(", "resource read"),
    ("ProviderDescriptor.create(", "rest of provider graph"),
    ("ProviderSchemaProjection.extract(", "generation"),
    ("Schema.randomForField(", "named stream"),
]


def samples(path, kind):
    text = subprocess.run([JFR, "print", "--events", kind, "--stack-depth", "256", path],
                          check=True, capture_output=True, text=True).stdout
    for block in text.split(kind + " {")[1:]:
        thread = re.search(r'sampledThread = "([^"]*)"', block)
        stack = block.split("stackTrace = [", 1)
        frames = []
        if len(stack) == 2:
            for line in stack[1].split("\n"):
                line = line.strip()
                if line.startswith("]"):
                    break
                if line and line != "...":
                    frames.append(line)
        yield (thread.group(1) if thread else ""), frames


def category(frames):
    if not any("Schema.generateAtIndex(" in f for f in frames):
        return "writing" if any(".schema.Schema." in f for f in frames) else "harness"
    best = None
    for marker, name in MARKERS:
        index = next((i for i, f in enumerate(frames) if marker in f), None)
        if index is not None and (best is None or index < best[0]):
            best = (index, name)
    return best[1] if best else "record"


for path in sys.argv[1:]:
    weights = collections.Counter()
    for kind, weight in (("jdk.ExecutionSample", 1), ("jdk.NativeMethodSample", NATIVE_WEIGHT)):
        for thread, frames in samples(path, kind):
            if "jmh-worker" in thread:
                weights[category(frames)] += weight
    total = sum(weights.values())
    print(path)
    for name, weight in weights.most_common():
        print(f"  {name:<24} {100 * weight / total:5.1f}%")
