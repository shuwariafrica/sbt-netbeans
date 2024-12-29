/****************************************************************
 * Copyright © Shuwari Africa Ltd.                              *
 *                                                              *
 * This file is licensed to you under the terms of the Apache   *
 * License Version 2.0 (the "License"); you may not use this    *
 * file except in compliance with the License. You may obtain   *
 * a copy of the License at:                                    *
 *                                                              *
 *     https://www.apache.org/licenses/LICENSE-2.0              *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, *
 * either express or implied. See the License for the specific  *
 * language governing permissions and limitations under the     *
 * License.                                                     *
 ****************************************************************/
package africa.shuwari.sbt.netbeans

import scala.util.Properties

import sbt.*
import sbt.io.IO
import sbt.plugins.JvmPlugin

object NetbeansPlugin extends AutoPlugin {

  override def requires: Plugins = JvmPlugin
  override def trigger: PluginTrigger = noTrigger

  val netbeans = taskKey[Seq[File]]("Generate NetBeans configuration")
  val netbeansCopyDefault = settingKey[Boolean]("Copy generated files to NetBeans default config directory")
  val netbeansClean = taskKey[Unit]("Clean generated NetBeans configuration files")

  object autoImport {
    val netbeans: TaskKey[Seq[File]] = NetbeansPlugin.netbeans
    val netbeansCopyDefault: SettingKey[Boolean] = NetbeansPlugin.netbeansCopyDefault
    val netbeansClean: TaskKey[Unit] = NetbeansPlugin.netbeansClean
  }

  override lazy val projectSettings: Seq[Setting[?]] = Seq(
    netbeans / Keys.target := Keys.target.value / "netbeans",
    netbeansCopyDefault := true,
    netbeans := {
      val log = Keys.streams.value.log
      val name = Keys.normalizedName.value
      val baseDir = Keys.baseDirectory.value
      val netbeansTargetDir = (netbeans / Keys.target).value
      val sourceDirs = (Compile / Keys.sourceDirectories).value
      val managedJars = (Compile / Keys.managedClasspath).value.files
      val unmanagedJars = (Compile / Keys.unmanagedJars).value

      def getJavacOption(option: String): String =
        (Compile / Keys.javacOptions).value
          .find(_.startsWith(option))
          .map(_.split(" ")(1))
          .getOrElse(Properties.javaVersion.split('.').head)

      val javacSourceLevel = getJavacOption("-source")
      val javacTargetLevel = getJavacOption("-target")

      log.info(s"Generating NetBeans configuration for project $name")
      val generatedFiles = ConfigGenerator.generate(name,
                                                    javacTargetLevel,
                                                    javacSourceLevel,
                                                    baseDir,
                                                    netbeansTargetDir,
                                                    sourceDirs,
                                                    managedJars,
                                                    unmanagedJars.map(_.data))

      if (netbeansCopyDefault.value) {
        val nbProjectDir = baseDir / "nbproject"
        log.info(s"Copying generated files to NetBeans default config directory: $nbProjectDir")
        IO.createDirectory(nbProjectDir)
        generatedFiles.foreach { file =>
          IO.copyFile(file, nbProjectDir / file.getName)
        }
      }

      generatedFiles
    },
    netbeansClean := {
      val log = Keys.streams.value.log
      val netbeansTargetDir = (netbeans / Keys.target).value
      val nbProjectDir = Keys.baseDirectory.value / "nbproject"

      log.info(s"Cleaning generated NetBeans configuration files in $netbeansTargetDir")
      IO.delete(netbeansTargetDir)

      if (netbeansCopyDefault.value) {
        log.info(s"Cleaning copied NetBeans configuration files in $nbProjectDir")
        IO.delete(nbProjectDir)
      }
    }
  )
}
