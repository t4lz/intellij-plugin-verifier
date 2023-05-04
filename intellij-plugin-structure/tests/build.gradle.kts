dependencies {
  testImplementation(project(":"))
  testImplementation(project(":structure-ide"))
  testImplementation(project(":structure-ide-classes"))
  testImplementation(project(":structure-intellij"))
  testImplementation(project(":structure-intellij-classes"))
  testImplementation(project(":structure-teamcity"))
  testImplementation(project(":structure-hub"))
  testImplementation(project(":structure-fleet"))
  testImplementation(project(":structure-dotnet"))
  testImplementation(project(":structure-edu"))
  testImplementation(project(":structure-toolbox"))
  testImplementation(libs.junit)
  testImplementation(libs.jackson.module.kotlin)
  testImplementation(libs.jimfs)
  testRuntimeOnly(libs.logback.classic)
}