# Java API Examples

Usage is catered to the factory methods and builder.  Some sample are included below.

## Maven / Gradle

To include in a Maven project:

```xml
<dependency>
    <groupId>net.sf.applecommander</groupId>
    <artifactId>applesingle-api</artifactId>
    <version>1.2.0</version>
</dependency>
```

To include in a Gradle project:

```groovy
dependencies {
    // ...
    compile "net.sf.applecommander:applesingle-api:1.2.0"
    // ...
}
```

## Read AppleSingle

Use the factory method to...

Reading from standard input:

```java
AppleSingle as = AppleSingle.read(System.in);
```

Reading from a file:

```java
File file = new File("myfile.as");
AppleSingle as = AppleSingle.read(file);
```

The AppleSingle file can be read from an `InputStream`, `File`, `Path`, or just a byte array.

## Create AppleSingle

Use the builder to create a new AppleSingle file and then save it...

```java
AppleSingle as = AppleSingle.builder()
        .dataFork(dataFork)
        .realName(realName)
        .build();
        
Path file = Paths.get("mynewfile.as"); 
as.save(file);
```

The `save(...)` method can save to a `File`, `Path`, or an `OutputStream`.

## Entries

If the higher-level API is insufficient, the lower-level API does allow either tracking of the processing
(see code for the `analyze` subcommand) or alternate processing of `Entry` objects (see the `filter`
subcommand).

To tap into the `AppleSingleReader` events, add as many reporters as required.  For example, the `analyze`
command uses these to display the details of the AppleSingle file as it is read:

```java
AppleSingleReader reader = AppleSingleReader.builder(fileData)
        .readAtReporter((start,chunk,desc) -> used.add(IntRange.of(start, start + chunk.length)))
        .readAtReporter((start,chunk,desc) -> dumper.dump(start, chunk, desc))
        .versionReporter(this::reportVersion)
        .numberOfEntriesReporter(this::reportNumberOfEntries)
        .entryReporter(this::reportEntry)
        .build();
```

To work with the raw `Entry` objects, use the various `AppleSingle#asEntries` methods. For instance, the
`filter` subcommand bypasses the `AppleSingle` object altogether to implement the filter:

```java
List<Entry> entries = stdinFlag ? AppleSingle.asEntries(System.in) : AppleSingle.asEntries(inputFile);
// ...
AppleSingle.write(outputStream, newEntries);
```
