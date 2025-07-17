# Graal Native Image configuration

This is a mish-mash of manual and automatic code generation.

To _update_ the configurations, use:

```declarative
-agentlib:native-image-agent=config-merge-dir=cli/src/main/resources/META-INF/native-image
```

Please delete empty files and reformat the JSON!

When running the JAR from the command line. This particular pathing, suggests that it be run from the root of the project.

Note: With `asu` every subcommand should be executed to capture all the pieces!

For example:

```shell
$ java -agentlib:native-image-agent=config-merge-dir=tools/asu/src/main/resources/META-INF/native-image \
       -jar tools/asu/build/libs/applesingle-tools-asu-1.4.0-DEV.jar \
       info api/src/test/resources/hello.applesingle.bin 
Real Name: -Unknown-
ProDOS info:
  Access: 0xC3
  File Type: 0x06
  Auxtype: 0x0803
File dates info:
  Creation: 2025-07-17T01:19:06Z
  Modification: 2025-07-17T01:19:06Z
  Access: 2025-07-17T01:19:06Z
  Backup: 2025-07-17T01:19:06Z
Data Fork: Present, 2,912 bytes
Resource Fork: Not present
```