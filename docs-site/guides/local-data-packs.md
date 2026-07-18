---
layout: page
title: Local Data Packs
permalink: /guides/local-data-packs/
---

# Local Data Packs

University fixtures use verified local data packs. A pack is a directory—not a URL—and it is only
visible through the `DataRegistryContext` that registers it. kRandom never downloads a pack at
runtime.

Create `krandom-data-pack.properties` beside `universities.csv`:

```properties
format.version=1
locale=en-US
source=Synthetic fixture data for this test suite
license=CC0-1.0
university.file=universities.csv
university.sha256=<64-character SHA-256 of universities.csv>
```

The CSV requires this header and five non-blank columns per row. Quoted CSV values are supported:

```csv
name,degree,prefix,suffix,place
Northbridge University,BSc,School of,University,Northbridge
"Riverdale, Institute",MSc,Institute of,Institute,Riverdale
```

Load and attach the pack to the exact generator configuration that needs it:

```java
LocalDataPack pack = LocalDataPack.load(Path.of("src/test/fixtures/university-pack"));

DataRegistryContext data = DataRegistryContext.builder()
        .isolated()
        .registerDataPack(pack)
        .build();

GeneratorConfig config = GeneratorConfig.builder()
        .locale(Locale.forLanguageTag("en-US"))
        .seed(42L)
        .registryContext(data)
        .build();

UniversityGenerator universities = Generators.ofUniversity(config);
UniversityData fixture = universities.generate();
String name = universities.name();
String degree = universities.degree();
String prefix = universities.prefix();
String suffix = universities.suffix();
String place = universities.place();
```

Each `UniversityData` value keeps its name, degree, prefix, suffix, and place together. The
individual accessors select a fixture per call; use `generate()` when a test needs a coherent
five-field record.

## Safety contract

- Manifest format version, locale, source, license, file name, and SHA-256 checksum are required.
- The loader accepts direct child files only, limits manifest and data-file size, checks the
  checksum before parsing, and rejects malformed or blank rows.
- A pack affects neither global registries nor other `GeneratorConfig` instances.
- A missing pack fails fast with the configured locale and visible local data-pack keys.

For a native executable, packs remain normal local files; load them from an application-controlled
path and use the [GraalVM Native Image]({{ '/guides/native-image/' | relative_url }}) guidance for
reflection-based object generation.
