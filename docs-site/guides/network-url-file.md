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

## File-oriented values

```java
String ext = Generators.ofFileExtension().generate();
String fileName = Generators.ofFileName().generateWithExtension(ext);
String mime = Generators.ofMimeType().generate();
String filePath = Generators.ofFilePath().generate();
```
