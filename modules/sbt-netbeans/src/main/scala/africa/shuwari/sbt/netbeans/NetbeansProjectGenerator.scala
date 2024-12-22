package africa.shuwari.sbt.netbeans

import sbt.*

object NetbeansProjectGenerator {

  /** Generates Netbeans project files (`project.xml` and `project.properties`) in the `nbproject` directory.
    *
    * @param baseDir
    *   Base directory of the project.
    * @param moduleName
    *   Name of the module.
    * @param classpath
    *   Classpath entries for compilation.
    * @param unmanagedClasspath
    *   Unmanaged classpath entries.
    * @param resources
    *   Managed resource directories (including those from dependent modules).
    * @param unmanagedResources
    *   Unmanaged resource directories (including those from dependent modules).
    * @param sources
    *   Source directories.
    * @param targetDir
    *   Target directory for compilation output.
    * @param log
    *   Logger for outputting messages.
    */
  def generate(
    baseDir: File,
    moduleName: String,
    classpath: Seq[File],
    unmanagedClasspath: Seq[File],
    resources: Seq[File],
    unmanagedResources: Seq[File],
    sources: Seq[String],
    targetDir: File,
    log: Logger
  ): Unit = {
    val nbprojectDir = baseDir / "nbproject"
    IO.createDirectory(nbprojectDir)

    val projectXml = generateProjectXmlContent(moduleName, sources)
    IO.write(nbprojectDir / "project.xml", projectXml)

    val projectProperties = generateProjectPropertiesContent(
      classpath,
      unmanagedClasspath,
      resources,
      unmanagedResources,
      targetDir
    )
    IO.write(nbprojectDir / "project.properties", projectProperties)

    log.info(s"Netbeans project configuration generated for $moduleName at $nbprojectDir")
  }

  /** Generates the content of the `project.xml` file.
    *
    * @param moduleName
    *   Name of the module.
    * @param sources
    *   Source directories.
    * @return
    *   The content of the `project.xml` file.
    */
  private def generateProjectXmlContent(moduleName: String, sources: Seq[String]): String =
    s"""<?xml version="1.0" encoding="UTF-8"?>
       |<project xmlns="http://www.netbeans.org/ns/project/1">
       |    <type>org.netbeans.modules.java.j2seproject</type>
       |    <configuration>
       |        <data xmlns="http://www.netbeans.org/ns/j2se-project/3">
       |            <name>$moduleName</name>
       |            <source-roots>
       |                ${sources.map(source => "<root id=\"" + source + "\"/>").mkString("\n")}
       |            </source-roots>
       |            <test-roots>
       |                <root id="test.src.dir"/>
       |            </test-roots>
       |        </data>
       |    </configuration>
       |</project>
       |""".stripMargin

  /** Generates the content of the `project.properties` file.
    *
    * @param classpath
    *   Classpath entries for compilation.
    * @param unmanagedClasspath
    *   Unmanaged classpath entries.
    * @param resources
    *   Managed resource directories (including those from dependent modules).
    * @param unmanagedResources
    *   Unmanaged resource directories (including those from dependent modules).
    * @param targetDir
    *   Target directory for compilation output.
    * @return
    *   The content of the `project.properties` file.
    */
  private def generateProjectPropertiesContent(
    classpath: Seq[File],
    unmanagedClasspath: Seq[File],
    resources: Seq[File],
    unmanagedResources: Seq[File],
    targetDir: File
  ): String = {
    val dependencies = classpath.map(_.getAbsolutePath).mkString(":")
    val unmanagedDependencies = unmanagedClasspath.map(_.getAbsolutePath).mkString(":")
    val resourcePaths = (resources ++ unmanagedResources).map(_.getAbsolutePath).mkString(":")
    s"""# Space-separated list of modules this module depends on
       |modules=\n
       |# Space-separated list of classpaths for compilation
       |javac.classpath=$dependencies\n
       |javac.unmanaged.classpath=$unmanagedDependencies\n
       |# Space-separated list of source level and target level
       |javac.source=1.8
       |javac.target=1.8\n
       |# Location of the build directory
       |build.dir=${targetDir.getAbsolutePath}\n
       |# Resource directories
       |source.root.dir=$resourcePaths
       |""".stripMargin
  }
}
