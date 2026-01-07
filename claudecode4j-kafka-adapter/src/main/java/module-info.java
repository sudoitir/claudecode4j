module ir.sudoit.claudecode4j.kafka {
    requires ir.sudoit.claudecode4j.api;
    requires ir.sudoit.claudecode4j.spring;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.beans;
    requires spring.core;
    requires spring.kafka;
    requires spring.messaging;
    requires kafka.clients;
    requires static org.jspecify;

    exports ir.sudoit.claudecode4j.kafka.listener;
    exports ir.sudoit.claudecode4j.kafka.producer;
    exports ir.sudoit.claudecode4j.kafka.config;
    exports ir.sudoit.claudecode4j.kafka.autoconfigure;

    opens ir.sudoit.claudecode4j.kafka.listener to
            spring.core,
            spring.beans,
            spring.context;
    opens ir.sudoit.claudecode4j.kafka.producer to
            spring.core,
            spring.beans,
            spring.context;
    opens ir.sudoit.claudecode4j.kafka.config to
            spring.core,
            spring.beans,
            spring.context;
    opens ir.sudoit.claudecode4j.kafka.autoconfigure to
            spring.core,
            spring.beans,
            spring.context;
}
