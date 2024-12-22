# sbt-netbeans

`sbt-netbeans` is an SBT plugin designed to simplify the generation of Netbeans project configuration files for SBT
projects. This plugin helps in managing Netbeans project settings and configurations, making it easier to develop SBT
projects using Netbeans.

## Features

- **Project Configuration**: Generates Netbeans project configuration files (`project.xml` and `project.properties`).
- **Consistency Check**: Checks if the existing Netbeans project configuration is consistent with the current SBT
  project settings.
- **Additional Resources**: Allows specifying additional resources to include in the Netbeans project configuration.

## Usage

To use the `sbt-netbeans` plugin, add the following to your `project/plugins.sbt` file:

```scala
addSbtPlugin("africa.shuwari.sbt" % "sbt-netbeans" % "[latest tag]")
```

In your `build.sbt` file, you can configure the plugin settings as follows:

```scala
name := "my-netbeans-project"

version := "0.1.0"

enablePlugins(NetbeansPlugin)

netbeansAdditionalResources := List(file("extra-resources"))
```

This example sets up a Netbeans project and includes additional resources from the `extra-resources` directory.

## Settings

The following `settingKey`s are available:

- `netbeansGenerate` \[TaskKey\[Unit\]\]: Generates Netbeans project configuration files.
- `netbeansCheck` \[TaskKey\[Unit\]\]: Checks if the existing Netbeans project configuration is consistent.
- `netbeansAdditionalResources` \[SettingKey\[List\[File\]\]\]: Additional resources to include in the Netbeans project
  configuration.

## Tasks

- **netbeansGenerate**: Generates the Netbeans project configuration files (`project.xml` and `project.properties`).
- **netbeansCheck**: Checks if the existing Netbeans project configuration is consistent with the current SBT project
  settings.

## Opening the Project in Netbeans

To open a module in Netbeans once a configuration has been generated using the `sbt-netbeans` plugin, follow these
steps:

1. **Generate the Netbeans Configuration**:
   Run the following SBT task to generate the Netbeans project configuration files:
   ```sh
   sbt netbeansGenerate
   ```

2. **Open Netbeans**:
   Launch the Netbeans IDE.

3. **Open the Project**:

- Go to `File` > `Open Project`.
- Navigate to the directory where your SBT project is located.
- Select the project directory (it should contain the `nbproject` folder generated by the plugin).
- Click `Open Project`.

4. **Build and Run**:

- Once the project is opened, you can build and run it using the standard Netbeans options.

## Contributing

Contributions are welcome! Please feel free to submit issues and pull requests to help improve the plugin.

## License

Copyright © Shuwari Africa Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this work except in compliance with the License.
You may obtain a copy of the License at:

[`http://www.apache.org/licenses/LICENSE-2.0`](https://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
