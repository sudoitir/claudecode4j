module ir.sudoit.claudecode4j.rest {
    requires ir.sudoit.claudecode4j.api;
    requires ir.sudoit.claudecode4j.spring;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.beans;
    requires spring.core;
    requires spring.web;
    requires spring.webmvc;
    requires jakarta.validation;
    requires static org.jspecify;

    exports ir.sudoit.claudecode4j.rest.controller;
    exports ir.sudoit.claudecode4j.rest.dto;
    exports ir.sudoit.claudecode4j.rest.autoconfigure;

    opens ir.sudoit.claudecode4j.rest.controller to
            spring.core,
            spring.beans,
            spring.context,
            spring.web;
    opens ir.sudoit.claudecode4j.rest.dto to
            spring.core,
            spring.beans,
            com.fasterxml.jackson.databind;
    opens ir.sudoit.claudecode4j.rest.autoconfigure to
            spring.core,
            spring.beans,
            spring.context;
}
