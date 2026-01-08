module ir.sudoit.claudecode4j.mcp {
    requires ir.sudoit.claudecode4j.api;
    requires ir.sudoit.claudecode4j.spring;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.beans;
    requires spring.core;
    requires tools.jackson.databind;
    requires static org.jspecify;

    exports ir.sudoit.claudecode4j.mcp.annotation;
    exports ir.sudoit.claudecode4j.mcp.server;
    exports ir.sudoit.claudecode4j.mcp.registry;
    exports ir.sudoit.claudecode4j.mcp.autoconfigure;

    opens ir.sudoit.claudecode4j.mcp.autoconfigure to
            spring.core,
            spring.beans,
            spring.context;
    opens ir.sudoit.claudecode4j.mcp.server to
            spring.core,
            spring.beans,
            spring.context;
    opens ir.sudoit.claudecode4j.mcp.registry to
            spring.core,
            spring.beans,
            spring.context;
}
