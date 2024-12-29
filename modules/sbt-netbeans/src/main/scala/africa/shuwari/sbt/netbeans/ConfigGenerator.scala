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

import sbt.*
import sbt.io.IO

object ConfigGenerator {

  def generate(name: String,
               javacTargetLevel: String,
               javacSourceLevel: String,
               baseDirectory: File,
               netbeansTargetDirectory: File,
               sourceDirectories: Seq[File],
               managedJars: Seq[File],
               unmanagedJars: Seq[File]): List[File] = {
    val configTarget = netbeansTargetDirectory / "generated-config"
    val netbeansPropertiesFileTarget = configTarget / "project.properties"
    val netbeansDescriptorFileTarget = configTarget / "project.xml"
    IO.createDirectory(configTarget)
    IO.write(
      netbeansPropertiesFileTarget,
      generateProperties(name,
                         baseDirectory,
                         javacTargetLevel,
                         javacSourceLevel,
                         netbeansTargetDirectory,
                         sourceDirectories,
                         managedJars,
                         unmanagedJars)
    )
    IO.write(netbeansDescriptorFileTarget, generateDescriptor(name, sourceDirectories))
    List(netbeansPropertiesFileTarget, netbeansDescriptorFileTarget)
  }

  private def generateProperties(name: String,
                                 baseDirectory: File,
                                 javacTargetLevel: String,
                                 javacSourceLevel: String,
                                 netbeansTargetDirectory: File,
                                 sourceDirectories: Seq[File],
                                 managedJars: Seq[File],
                                 unmanagedJars: Seq[File]): String = {
    val classpathEntries = (managedJars ++ unmanagedJars).map(jar => s"$${${fileReferenceName(jar)}}").mkString(":\\\n  ")

    val sourceReferences = sourceDirectories
      .collect {
        case f if f.exists =>
          val sourceRef = sourceReferenceName(f)
          val fileRef = fileReferenceName(sourceRef)
          s"$fileRef=${relativize(baseDirectory, f)}\n$sourceRef=$${$fileRef}"
      }
      .mkString("\n")

    val fileReferences = (managedJars ++ unmanagedJars)
      .map(file => s"${fileReferenceName(file)}=${relativize(baseDirectory, file)}")
      .mkString("\n")

    val netbeansTarget = relativize(baseDirectory, netbeansTargetDirectory)

    s"""
       |annotation.processing.enabled=true
       |annotation.processing.enabled.in.editor=false
       |annotation.processing.processors.list=
       |annotation.processing.run.all.processors=true
       |annotation.processing.source.output=$${build.generated.sources.dir}/ap-source-output
       |application.title=$name
       |application.vendor=$name
       |endorsed.classpath=
       |jar.compress=false
       |
       |build.dir=$netbeansTarget/build
       |build.classes.dir=$${build.dir}/classes
       |build.classes.excludes=**/*.java,**/*.form
       |build.generated.dir=$${build.dir}/generated
       |build.generated.sources.dir=$${build.dir}/generated-sources
       |build.sysclasspath=ignore
       |build.test.classes.dir=$${build.dir}/test/classes
       |build.test.results.dir=$${build.dir}/test/results
       |
       |dist.dir=$netbeansTarget/dist
       |dist.jar=$${dist.dir}/$name.jar
       |dist.javadoc.dir=$${dist.dir}/javadoc
       |dist.archive.excludes=
       |
       |mkdist.disabled=false
       |
       |debug.classpath=\\
       |  $${run.classpath}
       |debug.modulepath=\\
       |  $${run.modulepath}
       |debug.test.classpath=\\
       |  $${run.test.classpath}
       |debug.test.modulepath=\\
       |  $${run.test.modulepath}
       |
       |excludes=
       |includes=**
       |
       |$fileReferences
       |
       |$sourceReferences
       |
       |javac.source=$javacSourceLevel
       |javac.target=$javacTargetLevel
       |javac.deprecation=false
       |javac.external.vm=true
       |javac.modulepath=
       |javac.processormodulepath=
       |javac.processorpath=\\
       |  $${javac.classpath}
       |javac.test.classpath=
       |javac.test.modulepath=
       |
       |javadoc.author=false
       |javadoc.html5=false
       |javadoc.noindex=true
       |javadoc.nonavbar=true
       |javadoc.notree=true
       |javadoc.private=false
       |javadoc.splitindex=false
       |javadoc.use=false
       |javadoc.version=false
       |
       |jlink.launcher=false
       |jlink.launcher.name=$name
       |
       |platform.active=default_platform
       |
       |javac.classpath=\\
       |  $classpathEntries
       |
       |run.classpath=\\
       |  $${javac.classpath}:\\
       |  $${build.classes.dir}
       |
       |""".stripMargin
  }

  private def generateDescriptor(name: String, sourceDirectories: Seq[File]): String = {
    val sourceRootsEntries = sourceDirectories
      .collect {
        case dir if dir.exists =>
          s"""<root id="${sourceReferenceName(dir)}" />"""
      }
      .mkString("\n")
    s"""|<?xml version="1.0" encoding="UTF-8"?>
        |<project xmlns="http://www.netbeans.org/ns/project/1">
        |<type>org.netbeans.modules.java.j2seproject</type>
        |<configuration>
        |<data xmlns="http://www.netbeans.org/ns/j2se-project/3">
        |<name>$name</name>
        |<source-roots>
        |$sourceRootsEntries
        |</source-roots>
        |<test-roots/>
        |</data>
        |</configuration>
        |</project>
        |""".stripMargin
  }

  @inline private def fileReferenceName(file: File): String = fileReferenceName(file.name)
  @inline private def fileReferenceName(name: String): String = s"file.reference.$name"

  @inline private def sourceReferenceName(file: File): String = s"src-${file.getParentFile.getName}-${file.getName}"

  @inline private def relativize(base: File, file: File): String = IO.relativize(base, file).getOrElse(file.getAbsolutePath)
}
