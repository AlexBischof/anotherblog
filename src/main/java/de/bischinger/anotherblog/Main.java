package de.bischinger.anotherblog;

import java.io.IOException;

import static io.vertx.core.Vertx.vertx;

public class Main {

    public static void main(String[] args) throws IOException {
        vertx().deployVerticle(RestVerticle.class.getName());
        System.out.println("Vertx started on port 8080");
    }

}