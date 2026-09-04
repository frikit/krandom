# 2.3.0 field-stream qualification

Measured on 2026-09-04 on the same macOS arm64 host with JDK 21, JMH 1.37,
two forks, three one-second warmups and five one-second measurements per fork.
Both runs used `-Xms256m -Xmx256m` and the GC profiler. The same compiled benchmark
class ran against the released 2.2.0 jar and the 2.3.0 candidate jar. No other builds
or tests were launched by this release task during the measurements.

Workload: a seeded structural record containing a String, int, and List<Integer>, with
a fixed clock; the customized variant overrides only the name with a constant.

| Version | Policy | Customized | Throughput (ops/sec) | Allocated bytes/op |
| --- | --- | --- | ---: | ---: |
| 2.2.0 | LEGACY | false | 16,008 ± 281 | 572,303 |
| 2.2.0 | LEGACY | true | 101,327 ± 1,745 | 71,602 |
| 2.3.0 candidate | LEGACY | false | 15,892 ± 203 | 572,815 |
| 2.3.0 candidate | INDEPENDENT | false | 20,068 ± 254 | 439,250 |
| 2.3.0 candidate | LEGACY | true | 101,357 ± 1,382 | 71,714 |
| 2.3.0 candidate | INDEPENDENT | true | 32,130 ± 160 | 246,999 |

Default LEGACY throughput differed by less than 1%; allocation differences were below 0.2%.
No default-path regression exceeded the documented investigation thresholds.

The INDEPENDENT option changes the work performed for non-portable customized configurations.
For this fixture, customized throughput was about 68% lower and allocations 3.4 times higher
than customized LEGACY, in exchange for preserving unrelated field streams. This is an explicit
opt-in cost, not a default regression. Without customization the independent policy avoided
repeated portability checks and was faster in this workload. These results do not establish
performance for large graphs, arbitrary callbacks, or different machines.

Raw data: [2.2.0](baseline-2.2.0.json) and [candidate](candidate-2.3.0.json).
Source: [FieldStreamsBenchmark](../../../benchmarks/src/main/java/io/github/frikit/krandom/benchmarks/FieldStreamsBenchmark.java).

JMH arguments: `FieldStreamsBenchmark -p customized=false,true -f 2 -wi 3 -i 5 -w 1s -r 1s
-jvmArgs "-Xms256m -Xmx256m" -prof gc -rf json`. Use `-p policy=LEGACY` for 2.2.0 and
`-p policy=LEGACY,INDEPENDENT` for the candidate.
