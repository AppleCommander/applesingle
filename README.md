# AppleSingle

This project is an off-shoot of AppleCommander's support for the AppleSingle format in that there are multiple 
Java-based tools that can benefit from a pre-built library to support the AppleSingle format.

# Java Usage Examples

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

# Command-Line Examples

For the included command-line utility, we are using `asu` for the name.
`as` is the GNU Assembler while `applesingle` is already on Macintoshes.
Hopefully that will prevent some confusion!

Note that all runs are with the `asu` alias defined as `alias asu='java -jar build/libs/applesingle-1.0.0.jar'`
(adjust as necessary).

## Basic usage

```shell
$ asu
Usage: asu [-hV] [--debug] [COMMAND]

AppleSingle utility

Options:
      --debug     Dump full stack trackes if an error occurs
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.

Commands:
  help     Displays help information about the specified command
  info     Display information about an AppleSingle file
  create   Create an AppleSingle file
  extract  Extract contents of an AppleSingle file
```

## Subcommand help

```shell
$ asu info --help
Usage: asu info [-h] [--stdin] [<file>]

Display information about an AppleSingle file
Please include a file name or indicate stdin should be read, but not both.

Parameters:
      [<file>]   File to process

Options:
      --stdin    Read AppleSingle from stdin.
  -h, --help     Show help for subcommand
```

## Info subcommand 

```shell
$ asu info src/test/resources/hello.applesingle.bin 
Real Name: -Unknown-
ProDOS info:
  Access: 0xC3
  File Type: 0x06
  Auxtype: 0x0803
Data Fork: Present, 2,912 bytes
Resource Fork: Not present
```

## Sample runs

Using pipes to create a text file and display information.  Note that the invalid `my-text-file` was changed to `MY.TEXT.FILE`.

```shell
$ echo "Hello World!" | asu create --name my-text-file --stdout --filetype 0x06 --stdin-fork=data --fix-text | asu info --stdin
Real Name: MY.TEXT.FILE
ProDOS info:
  Access: 0xC3
  File Type: 0x06
  Auxtype: 0x0000
Data Fork: Present, 13 bytes
Resource Fork: Not present
```

The `--fix-text` file flips the high-bit and translates the newline character.

```shell
$ echo "Hello World!" | asu create --name my-text-file --stdout --filetype 0x06 --stdin-fork=data --fix-text | hexdump -C
00000000  00 05 16 00 00 02 00 00  00 00 00 00 00 00 00 00  |................|
00000010  00 00 00 00 00 00 00 00  00 03 00 00 00 03 00 00  |................|
00000020  00 3e 00 00 00 0c 00 00  00 0b 00 00 00 4a 00 00  |.>...........J..|
00000030  00 08 00 00 00 01 00 00  00 52 00 00 00 0d 4d 59  |.........R....MY|
00000040  2e 54 45 58 54 2e 46 49  4c 45 00 c3 00 06 00 00  |.TEXT.FILE......|
00000050  00 00 c8 e5 ec ec ef a0  d7 ef f2 ec e4 a1 8d     |...............|
0000005f
```
(The message is at 0x52 through 0x5e.)

# Resources 

* [AppleSingle spec](http://kaiser-edv.de/documents/AppleSingle_AppleDouble.pdf)
* [Original ticket for AppleCommander](https://github.com/AppleCommander/AppleCommander/issues/20)
