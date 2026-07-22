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
`Generators.ofURL()` for `java.net.URL` or `Generators.ofURI()` for `java.net.URI`. The distinction
is intentional: lowercase names describe a text format and uppercase names describe the JDK type.

## File-oriented values

```java
String ext = Generators.ofFileExtension().generate();
String fileName = Generators.ofFileName().generateWithExtension(ext);
String mime = Generators.ofMimeType().generate();
String filePath = Generators.ofFilePath().generate();
```
