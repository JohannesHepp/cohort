dependencies {
   implementation(project(":cohort-core"))
   api("org.elasticsearch.client:elasticsearch-rest-high-level-client:_")
   implementation("com.fasterxml.jackson.core:jackson-databind:_")
}

apply("../publish.gradle.kts")
