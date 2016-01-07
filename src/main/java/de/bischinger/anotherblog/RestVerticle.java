/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package de.bischinger.anotherblog;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.search.SearchHits;

import java.net.UnknownHostException;

import static java.net.InetAddress.getByName;
import static org.elasticsearch.client.transport.TransportClient.builder;
import static org.elasticsearch.common.settings.Settings.settingsBuilder;

public class RestVerticle extends AbstractVerticle {

    private String indexName = "myindex";
    private String typeName = "blogs";

    private TransportClient client;

    @Override
    public void start() throws UnknownHostException {

        setupElasticsearchClient();

        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        router.put("/blogs/:blogKey").handler(this::handleAddBlog);
        router.get("/blogs").handler(this::handleListBlogs);

        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }

    private void setupElasticsearchClient() throws UnknownHostException {
        Settings settings = settingsBuilder()
                .put("cluster.name", "elasticsearch_bischofa").build();
        client = builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(getByName("localhost"), 9300));
    }

    //curl -XPUT http://localhost:8080/blogs/1 -d '{"title": "Nest eggs2","body":  "Making your money work...","comments": [{"name":    "John Smith"},{"name":    "John Smith2"}]}'

    //Partial update
    //curl http://localhost:9200/myindex/blogs/1alex?pretty
    //curl -XPOST http://localhost:9200/myindex/blogs/_search -d '{"query": {"filtered": {"filter":{"bool": {"must": { "term":  { "_id": "2alex" }}}}}}}'
    //curl -XPUT http://localhost:9200/myindex/blogs/2alex -d '{"title": "Nest eggs2","body":  "Making your money work...","comments": [{"name":    "John Smith"}]}'
    //curl -XPOST http://localhost:9200/myindex/blogs/2alex/_update -d '{"script" : "ctx._source.comments += new_comment", "params" : {"new_comment" : {"name": "Karin"}}}'
    private void handleAddBlog(RoutingContext routingContext) {
        String blogKey = routingContext.request().getParam("blogKey");
        HttpServerResponse response = routingContext.response();
        if (blogKey == null) {
            sendError(400, response);
        } else {
            JsonObject blog = routingContext.getBodyAsJson();
            if (blog == null) {
                sendError(400, response);
            } else {
                String id = blog.getString("title");
                IndexRequest indexRequest = new IndexRequest(indexName, typeName, id).source(blog.toString());

                vertx.executeBlocking(future -> {
                    client.index(indexRequest).actionGet();
                    future.complete();
                }, res -> response.end());
            }
        }
    }

    //curl http://localhost:8080/blogs
    private void handleListBlogs(RoutingContext routingContext) {
        JsonArray arr = new JsonArray();

        vertx.executeBlocking(future -> {
            SearchResponse searchResponse = client.prepareSearch(indexName).execute().actionGet();
            future.complete(searchResponse);
        }, result -> {
            SearchResponse searchResponse = (SearchResponse) result.result();
            searchResponse.getHits().forEach(hit -> arr.add(hit.getSourceAsString()));
            routingContext.response().putHeader("content-type", "application/json").end(arr.encodePrettily());
        });
    }

    private void sendError(int statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode).end();
    }
}