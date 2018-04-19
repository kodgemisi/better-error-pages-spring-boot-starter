# Better Error Pages

This is a `Spring Boot Starter` project which provides Rails like error pages for Spring Boot applications for development environments.

## Usage

Just add the dependency to your maven/gradle of your Spring Boot application then it will autoconfigure itself and
_Better Error Pages_ will be available if your active profiles includes `dev` or `development`.

You can change profiles in which _Better Error Pages_ will be enabled via `better-error-pages.profiles` property.

## Installation

Your project must have **one of** following files:

* application.yml
* application-dev.yml
* application.properties
* application-dev.properties

and `better-error-pages.package-name` property must be set.

Add the dependency to your project

```xml
	<dependencies>
	  <dependency>
		    <groupId>com.github.kodgemisi</groupId>
		    <artifactId>better-error-pages-spring-boot-starter</artifactId>
		    <version>${better-error-pages-spring-boot-starter.version}</version>
		</dependency>
	</dependencies>

	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
```

See https://jitpack.io/#kodgemisi/better-error-pages-spring-boot-starter for Jitpack usage, dependency details, version info and Gradle usage.

Then any error page will look like following:

![Screenshot](docs/screenshot.png)

## Configuration

```yaml
better-error-pages:
  package-name: <String> # A package name whose classes' source code will be parsed and displayed in error pages. Mandatory.
  profiles: <Coma separated string> # Override default profiles to enable Better Error Pages. Default value: "dev, development"
```

Note that `package-name` should be typically your own base package which is used througout your own classes like `com.yourcompany`.

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
