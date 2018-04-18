# Better Error Pages

Rails like error pages for Spring Boot applications.

## Prerequisites

Your project must have **one of** following files:

* application.yml
* application-dev.yml
* application.properties
* application-dev.properties

and `better-error-pages.package-name` property must be set.

Add the dependency to your project

```xml
<dependency>
	    <groupId>com.github.kodgemisi</groupId>
	    <artifactId>better-error-pages</artifactId>
	    <version>${better-error-pages.version}</version>
	</dependency>
```

See https://jitpack.io/#kodgemisi/better-error-pages for Jitpack usage, dependency details and version info.

## Configuration

```yaml
better-error-pages:
  package-name: <String> # A package name whose classes' source code will be parsed and displayed in error pages. Mandatory.
  profiles: <Coma separated string> # Override default profiles to enable Better Error Pages. Default value: "dev, development"
```

## Limitations

Won't work for

* package names containing upper case characters

## Troubleshooting

### IllegalArgumentException: Could not resolve placeholder

`Caused by: java.lang.IllegalArgumentException: Could not resolve placeholder 'better-error-pages.package-name' in value "${better-error-pages.package-name}"`

 See Prerequisites, you need to set `better-error-pages.package-name` property.

 # LICENSE

 © Copyright 2018 Kod Gemisi Ltd.

 Mozilla Public License 2.0 (MPL-2.0)

 https://tldrlegal.com/license/mozilla-public-license-2.0-(mpl-2)

 MPL is a copyleft license that is easy to comply with. You must make the source code for any of your changes available under MPL, but you can combine the MPL software with proprietary code, as long as you keep the MPL code in separate files. Version 2.0 is, by default, compatible with LGPL and GPL version 2 or greater. You can distribute binaries under a proprietary license, as long as you make the source available under MPL.

 [See Full License Here](https://www.mozilla.org/en-US/MPL/2.0/)
