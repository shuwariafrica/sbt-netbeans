package africa.shuwari.sbt.netbeans

import sbt.*
import sbt.Def
import sbt.Keys.*

/** An sbt plugin for generating Netbeans project configuration.
  */
object NetbeansPlugin extends AutoPlugin {

  override def requires = plugins.JvmPlugin
  override def trigger = allRequirements

  object autoImport {

    /** Generates Netbeans project configuration files.
      */
    val netbeansGenerate = taskKey[Unit]("Generate Netbeans project configuration")

    /** Checks if the existing Netbeans project configuration is consistent.
      */
    val netbeansCheck = taskKey[Unit]("Check Netbeans project configuration")

    /** Additional resources to include in the Netbeans project configuration.
      */
    val netbeansAdditionalResources =
      settingKey[List[File]]("Additional resources to include in the Netbeans project configuration")
  }

  import autoImport._

  // Task to generate Netbeans project files
  private val generateNetbeansFiles = Def.task {
    val log = streams.value.log
    val baseDir = baseDirectory.value
    val moduleName = name.value
    val classpath = fullClasspath.value.map(_.data)
    val unmanagedClasspath = (Compile / Keys.unmanagedClasspath).value.map(_.data)
    val sources = (Compile / sourceDirectories).value.map(_.getAbsolutePath)
    val targetDir = target.value
    val managedResources = (Compile / resources).value
    val unmanagedResources = (Compile / Keys.unmanagedResources).value
    val additionalResourcesList = netbeansAdditionalResources.value

    NetbeansProjectGenerator.generate(
      baseDir,
      moduleName,
      classpath,
      unmanagedClasspath,
      managedResources ++ additionalResourcesList, // Include additional resources
      unmanagedResources, // Do not include additional resources here
      sources,
      targetDir,
      log
    )
  }

  override lazy val projectSettings: List[Def.Setting[?]] = List(
    netbeansAdditionalResources := List.empty[File], // Default to an empty list
    netbeansGenerate := generateNetbeansFiles.value,
    netbeansGenerate := netbeansGenerate.dependsOn(compile).value,
    netbeansCheck := {
      val log = streams.value.log
      val baseDir = baseDirectory.value
      val cacheDir = streams.value.cacheDirectory

      // Generate files in cache directory
      NetbeansProjectGenerator.generate(
        cacheDir,
        name.value,
        fullClasspath.value.map(_.data),
        (Compile / Keys.unmanagedClasspath).value.map(_.data),
        (Compile / resources).value,
        (Compile / Keys.unmanagedResources).value,
        (Compile / sourceDirectories).value.map(_.getAbsolutePath),
        target.value,
        log
      )

      val nbprojectDir = baseDir / "nbproject"

      // Compare project.xml
      val expectedProjectXml = IO.read(cacheDir / "nbproject" / "project.xml")
      val actualProjectXml = IO.read(nbprojectDir / "project.xml")
      if (expectedProjectXml != actualProjectXml) {
        log.error(
          s"Inconsistent project.xml detected for ${name.value}. Run 'netbeansGenerate' to update."
        )
        throw new MessageOnlyException(
          s"Inconsistent project.xml detected for ${name.value}."
        )
      }

      // Compare project.properties
      val expectedProjectProperties =
        IO.read(cacheDir / "nbproject" / "project.properties")
      val actualProjectProperties = IO.read(nbprojectDir / "project.properties")
      if (expectedProjectProperties != actualProjectProperties) {
        log.error(
          s"Inconsistent project.properties detected for ${name.value}. Run 'netbeansGenerate' to update."
        )
        throw new MessageOnlyException(
          s"Inconsistent project.properties detected for ${name.value}."
        )
      }

      log.info(s"Netbeans project configuration for ${name.value} is consistent.")
    }
  )
}
