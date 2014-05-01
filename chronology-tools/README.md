chronology-tools
================

### Tools

  * com.higherfrequencytrading.chronology.tools.ChroniTail
  ```
    ChroniTail [-t|-i] path
        -t = text chronicle, default binary
        -i = IndexedCronicle, default VanillaChronicle

    mvn exec:java -Dexec.mainClass="com.higherfrequencytrading.chronology.tools.ChroniTail" -Dexec.args="..."
  ```

  * com.higherfrequencytrading.chronology.tools.ChroniCat
  ```
      ChroniCat [-t|-i] path
        -t = text chronicle, default binary
        -i = IndexedCronicle, default VanillaChronicle

      mvn exec:java -Dexec.mainClass="com.higherfrequencytrading.chronology.tools.ChroniCat" -Dexec.args="..."
  ```

  * com.higherfrequencytrading.chronology.tools.ChroniGrep
  ```
      ChroniCat [-t|-i] regexp1 ... regexpN path
        -t = text chronicle, default binary
        -i = IndexedCronicle, default VanillaChronicle

      mvn exec:java -Dexec.mainClass="com.higherfrequencytrading.chronology.tools.ChroniCat" -Dexec.args="..."
  ```

### Writing a simple LogSearch with Groovy and Grape

  * Binary log search
  ```groovy
  import net.openhft.chronicle.VanillaChronicle
  import net.openhft.chronicle.IndexedChronicle
  import com.higherfrequencytrading.chronology.ChronologyLogProcessor
  import com.higherfrequencytrading.chronology.tools.ChroniTool

  @Grapes([
     @Grab(group='net.openhft'               , module='chronicle'        , version='3.0b-SNAPSHOT'),
     @Grab(group='com.higherfrequencytrading', module='chronology-tools' , version='1.0-SNAPSHOT' ),
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
  import com.higherfrequencytrading.chronology.ChronologyLogProcessor
  import com.higherfrequencytrading.chronology.tools.ChroniTool

  @Grapes([
     @Grab(group='net.openhft'               , module='chronicle'       , version='3.0b-SNAPSHOT'),
     @Grab(group='com.higherfrequencytrading', module='chronology-tools', version='1.0-SNAPSHOT' ),
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
