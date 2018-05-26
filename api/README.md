# Java API Examples

Usage is catered to the factory methods and builder.  Some sample are included below.

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
