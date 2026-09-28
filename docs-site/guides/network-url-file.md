---
layout: page
title: Network, URL, and File Data
permalink: /guides/network-url-file/
---

# Network, URL, and File Data

## Network values

```java
String ipv4 = Generators.ofIPv4().generate();
String ipv6 = Generators.ofIPv6().generate();
String mac = Generators.ofMacAddress().generate();
String ua = Generators.ofUserAgent().generate();
```

## URL generation

```java
URLGenerator urls = Generators.ofUrl();
String simple = urls.generate();
String withPath = urls.generateWithPath();
String full = urls.generateWithPathAndQuery();
```

`ofUrl()` and `ofUri()` produce text values. When an API needs parsed JDK objects instead, use
`Generators.ofUrlObject()` for `java.net.URL` or `Generators.ofUriObject()` for `java.net.URI`
(each also accepts a `GeneratorConfig`). The former case twins `ofURL()`/`ofURI()` are deprecated
because they differed from the text factories only by letter case.

## File-oriented values

```java
String ext = Generators.ofFileExtension().generate();
String fileName = Generators.ofFileName().generateWithExtension(ext);
String mime = Generators.ofMimeType().generate();
String filePath = Generators.ofFilePath().generate();
```
