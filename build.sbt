inThisBuild(
  List(
    description := "A simple SBT plugin to allow generation of Netbeans configuration files for a specific module",
    homepage := Some(url("https://github.com/shuwarifrica/sbt-netbeans")),
    version := versionSetting.value,
    dynver := versionSetting.toTaskable.toTask.value,
    sonatypeCredentialHost := "s01.oss.sonatype.org",
    publishCredentials,
    scmInfo := ScmInfo(
      url("https://github.com/shuwariafrica/sbt-netbeans"),
      "scm:git:https://github.com/shuwariafrica/sbt-netbeans.git",
      Some("scm:git:git@github.com:shuwariafrica/sbt-netbeans.git")
    ).some,
    startYear := Some(2024),
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )
)

lazy val `sbt-netbeans` =
  project
    .in(file("modules/sbt-netbeans"))
    .enablePlugins(SbtPlugin)
    .settings(publishSettings *)
    .settings(scriptedSettings *)

lazy val `sbt-netbeans-root` = project
  .in(file("."))
  .aggregate(`sbt-netbeans`)
  .notPublished
  .shuwariProject
  .apacheLicensed
  .settings(sonatypeProfile)
  .settings(organization := "africa.shuwari.sbt")

def publishCredentials = credentials := List(
  Credentials(
    "Sonatype Nexus Repository Manager",
    sonatypeCredentialHost.value,
    System.getenv("PUBLISH_USER"),
    System.getenv("PUBLISH_USER_PASSPHRASE")
  )
)

def publishSettings = publishCredentials +: pgpSettings ++: List(
  packageOptions += Package.ManifestAttributes(
    "Created-By" -> "Simple Build Tool",
    "Built-By" -> System.getProperty("user.name"),
    "Build-Jdk" -> System.getProperty("java.version"),
    "Specification-Title" -> name.value,
    "Specification-Version" -> version.value,
    "Specification-Vendor" -> organizationName.value,
    "Implementation-Title" -> name.value,
    "Implementation-Version" -> implementationVersionSetting.value,
    "Implementation-Vendor-Id" -> organization.value,
    "Implementation-Vendor" -> organizationName.value
  ),
  publishTo := sonatypePublishToBundle.value,
  pomIncludeRepository := (_ => false),
  publishMavenStyle := true,
  sonatypeProfile
)

def sonatypeProfile = sonatypeProfileName := "africa.shuwari"

def pgpSettings = List(
  PgpKeys.pgpSelectPassphrase :=
    sys.props
      .get("SIGNING_KEY_PASSPHRASE")
      .map(_.toCharArray),
  usePgpKeyHex(System.getenv("SIGNING_KEY_ID"))
)

def baseVersionSetting(appendMetadata: Boolean): Def.Initialize[String] = {
  def baseVersionFormatter(in: sbtdynver.GitDescribeOutput) = {
    def meta =
      if (appendMetadata) s"+${in.commitSuffix.distance}.${in.commitSuffix.sha}"
      else ""

    if (!in.isSnapshot()) in.ref.dropPrefix
    else {
      val parts = {
        def current = in.ref.dropPrefix.split("\\.").map(_.toInt)

        current.updated(current.length - 1, current.last + 1)
      }
      s"${parts.mkString(".")}-SNAPSHOT$meta"
    }
  }

  Def.setting(
    dynverGitDescribeOutput.value.mkVersion(
      baseVersionFormatter,
      "SNAPSHOT"
    )
  )
}

def versionSetting = baseVersionSetting(appendMetadata = false)

def implementationVersionSetting = baseVersionSetting(appendMetadata = true)

def scriptedSettings: List[Setting[?]] = List(
  scriptedLaunchOpts := {
    scriptedLaunchOpts.value ++
      Seq("-Dplugin.version=" + version.value)
  },
  scriptedBufferLog := false
)

enablePlugins(SbtPlugin)
