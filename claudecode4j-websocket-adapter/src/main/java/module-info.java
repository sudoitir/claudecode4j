module ir.sudoit.claudecode4j.websocket {
    requires ir.sudoit.claudecode4j.api;
    requires ir.sudoit.claudecode4j.spring;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.beans;
    requires spring.core;
    requires spring.web;
    requires spring.websocket;
    requires tools.jackson.databind;
    requires static org.jspecify;

    exports ir.sudoit.claudecode4j.websocket.handler;
    exports ir.sudoit.claudecode4j.websocket.session;
    exports ir.sudoit.claudecode4j.websocket.message;
    exports ir.sudoit.claudecode4j.websocket.config;
    exports ir.sudoit.claudecode4j.websocket.autoconfigure;

    opens ir.sudoit.claudecode4j.websocket.message to
            tools.jackson.databind;
    opens ir.sudoit.claudecode4j.websocket.autoconfigure to
            spring.core,
            spring.beans,
            spring.context;
    opens ir.sudoit.claudecode4j.websocket.config to
            spring.core,
            spring.beans,
            spring.context;
    opens ir.sudoit.claudecode4j.websocket.handler to
            spring.core,
            spring.beans,
            spring.context;
}
