package com.sksamuel.cohort.elastic

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.Refresh
import co.elastic.clients.elasticsearch.core.IndexRequest
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.rest_client.RestClientTransport
import com.sksamuel.cohort.HealthCheckResult
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.elastic.ElasticTestContainerExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeTypeOf
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.testcontainers.elasticsearch.ElasticsearchContainer
import org.testcontainers.utility.DockerImageName

class ElasticIndexHealthCheckTest : FunSpec({

   val container = ElasticsearchContainer(
      DockerImageName.parse("elasticsearch:7.17.6")
         .asCompatibleSubstituteFor("docker.elastic.co/elasticsearch/elasticsearch")
   )
   val client = install(ElasticTestContainerExtension(container))

   test("ElasticIndexCheck should connect to elastic and check for topic") {
      client.indices().create(CreateIndexRequest.Builder().index("foo").build())
      ElasticIndexHealthCheck(client, "foo").check() shouldBe
         HealthCheckResult.Healthy("Detected elastic index 'foo'")
   }

   test("missing index") {
      ElasticIndexHealthCheck(client, "qwe").check().shouldBeInstanceOf<HealthCheckResult.Unhealthy>()
         .message shouldBe "Elastic index 'qwe' was not found"
   }

   test("ElasticIndexCheck should support failIfEmpty=true with empty index") {
      client.indices().create(CreateIndexRequest.Builder().index("bar").build())
      ElasticIndexHealthCheck(client, "bar", true).check().shouldBeInstanceOf<HealthCheckResult.Unhealthy>()
         .message shouldBe "Elastic index 'bar' is empty"
   }

   test("ElasticIndexCheck should support failIfEmpty=true with populated index") {
      client.indices().create(CreateIndexRequest.Builder().index("baz").build())
      client.index(
         IndexRequest.Builder<Map<String, String>>().index("baz").document(mapOf("a" to "b"))
            .refresh(Refresh.True).build()
      )
      ElasticIndexHealthCheck(client, "baz", true).check() shouldBe
         HealthCheckResult.Healthy("Detected elastic index 'baz'")
   }

   test("ElasticIndexHealthCheck should fail if cannot connect") {
      val restClient = RestClient.builder(HttpHost("localhost", 11111)).build()
      val transport = RestClientTransport(restClient, JacksonJsonpMapper())
      val client = ElasticsearchClient(transport)
      ElasticIndexHealthCheck(client, "foo").check().shouldBeTypeOf<HealthCheckResult.Unhealthy>()
   }
})