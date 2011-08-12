This is lessc2java; this open source project allows Java builds (either Ant-
or Maven-based) to convert LessCSS files to regular CSS files.

For more information about LessCSS, see:

   http://lesscss.org/

This code has been tested with the following combination of software:

   - Java SE 6
   - Maven 3.0.3
   - Ant 1.8.2
   - lessc 1.1.13 and 1.2.21
   - plessc 0.1.6
   - less.js 1.0.40

Several LessCSS interpreters are supported:

   1. less.js - The official node.js-based implementation, from
                http://lesscss.org/, for the code see:
                https://github.com/cloudhead/less.js

   2. plessc  - LessPHP, an alternative implementation from
                http://leafo.net/lessphp/
                PHP-based, performance is good.

Others may work equally well.


LICENSE

This software is available under the terms of a BSD-style license, see
the accompanied LICENSE.txt file.


BUILDING LESSCSS-JI

This project is built using Maven, e.g.:

   mvn install


ANT EXAMPLE

Example usage of the task in an Ant build file:

   <taskdef name="lesscss"
       classname="org.znerd.lesscss.ant.LessCSSTask"
       classpath="lib/lessc4java-ant-task.jar,lib/lessc4java-core.jar" />

   <lesscss dir="src/htdocs" todir="build/htdocs" />

Although all parameters are optional, the task supports various:

   dir      - the source directory, to read LessCSS files from, defaults to
              the project base directory;

   todir    - the destination directory, to write the generated CSS files to,
              defaults to the source directory;

   command  - the command to execute, by default the task uses 'lessc', but
              in the future it may become more intelligent and possibly find
              'plessc' as well, possibly even preferring that over 'lessc';

   timeOut  - the time-out in milliseconds for executing a single command,
              defaults to 60000 (meaning 60 seconds);

   includes - the files in the source directory to include, defaults to
              '*.less';

   excludes - the files to exclude, even if they are matched by the includes;

   overwrite - if set to 'true', then existing files are always overwritten,
               even if they are newer.

and other parameters inherited from the MatchingTask, see:

   http://ant.apache.org/manual/dirtasks.html


ISSUES

If you want to file a bug report or a feature request, please do so here:

   http://github.com/znerd/lesscss-ant-task/issues


AUTHOR

This software has been developed by:

   Ernst de Haan
   ernst@ernstdehaan.com
   twitter: @ernstdehaan
