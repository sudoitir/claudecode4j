module ir.sudoit.claudecode4j.spring {
    requires ir.sudoit.claudecode4j.api;
    requires ir.sudoit.claudecode4j.core;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.beans;
    requires spring.core;
    requires static spring.boot.actuator;
    requires static spring.boot.actuator.autoconfigure;
    requires static spring.aop;
    requires static micrometer.core;
    requires static org.jspecify;
    requires static org.aspectj.weaver;
    requires spring.boot.health;

    exports ir.sudoit.claudecode4j.spring.autoconfigure;
    exports ir.sudoit.claudecode4j.spring.properties;
    exports ir.sudoit.claudecode4j.spring.health;
    exports ir.sudoit.claudecode4j.spring.metrics;
    exports ir.sudoit.claudecode4j.spring.aop;

    opens ir.sudoit.claudecode4j.spring.autoconfigure to
            spring.core,
            spring.beans,
            spring.context;
    opens ir.sudoit.claudecode4j.spring.properties to
            spring.core,
            spring.beans,
            spring.context;
    opens ir.sudoit.claudecode4j.spring.health to
            spring.core,
            spring.beans,
            spring.context;
    opens ir.sudoit.claudecode4j.spring.metrics to
            spring.core,
            spring.beans,
            spring.context;
    opens ir.sudoit.claudecode4j.spring.aop to
            spring.core,
            spring.beans,
            spring.context,
            spring.aop;
}
