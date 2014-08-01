chronology-tools
================

### Tools

  * net.openhft.chronicle.logger.tools.ChroniTail
  ```
    ChroniTail [-t|-i] path
        -t = text chronicle, default binary
        -i = IndexedCronicle, default VanillaChronicle

    mvn exec:java -Dexec.mainClass="net.openhft.chronicle.logger.tools.ChroniTail" -Dexec.args="..."
  ```

  * net.openhft.chronicle.logger.tools.ChroniCat
  ```
      ChroniCat [-t|-i] path
        -t = text chronicle, default binary
        -i = IndexedCronicle, default VanillaChronicle

      mvn exec:java -Dexec.mainClass="net.openhft.chronicle.logger.tools.ChroniCat" -Dexec.args="..."
  ```

  * net.openhft.chronicle.logger.tools.ChroniGrep
  ```
      ChroniCat [-t|-i] regexp1 ... regexpN path
        -t = text chronicle, default binary
        -i = IndexedCronicle, default VanillaChronicle

      mvn exec:java -Dexec.mainClass="net.openhft.chronicle.logger.tools.ChroniCat" -Dexec.args="..."
  ```

### Writing a simple LogSearch with Groovy and Grape

  * Binary log search
  ```groovy
  import net.openhft.chronicle.VanillaChronicle
  import net.openhft.chronicle.IndexedChronicle
  import net.openhft.chronicle.logger.ChronologyLogProcessor
  import net.openhft.chronicle.logger.tools.ChroniTool

  @Grapes([
     @Grab(group='net.openhft', module='chronicle'              , version='3.2.1-SNAPSHOT'),
     @Grab(group='net.openhft', module='chronicle-logger-tools' , version='1.0.0-SNAPSHOT' ),
  ])
  class LogSearch {
      static def main(String[] args) {
          def processor = { event ->
              if(event.message =~ '.*n.*') {
                  printf("%s => %s\n",ts,msg)
              }
          }

          try {
              if(args.length == 1) {
                  ChroniTool.process(
                      new VanillaChronicle(args[0]),
                      ChroniTool.binaryReader(processor as ChronologyLogProcessor),
                      false,
                      false)
              }
          } catch(Exception e) {
              e.printStackTrace(System.err);
          }
      }
  }
  ```

  * Text log search
  ```groovy
  import net.openhft.chronicle.VanillaChronicle
  import net.openhft.chronicle.IndexedChronicle
  import net.openhft.chronicle.logger.ChronologyLogProcessor
  import net.openhft.chronicle.logger.tools.ChroniTool

  @Grapes([
     @Grab(group='net.openhft', module='chronicle'             , version='3.2.1-SNAPSHOT'),
     @Grab(group='net.opemhft', module='chronicle-logger-tools', version='1.0.0-SNAPSHOT' ),
  ])
  class LogSearch {
      static def main(String[] args) {
          def processor = { msg ->
              if(msg =~ '.*n.*') {
                  printf("%s => %s\n",ts,msg)
              }
          }

          try {
              if(args.length == 1) {
                  ChroniTool.process(
                      new VanillaChronicle(args[0]),
                      ChroniTool.binaryReader(processor as ChronologyLogProcessor),
                      false,
                      false)
              }
          } catch(Exception e) {
              e.printStackTrace(System.err);
          }
      }
  }
  ```
