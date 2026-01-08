open module ir.sudoit.claudecode4j.rest {
    requires transitive ir.sudoit.claudecode4j.api;
    requires ir.sudoit.claudecode4j.core;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.web;
    requires spring.context;
    requires spring.beans;
    requires spring.core;
    requires tools.jackson.core;
    requires tools.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires org.slf4j;
    requires jakarta.validation;
    requires org.jspecify;
    requires spring.webmvc;

    exports ir.sudoit.claudecode4j.rest.anthropic.controller;
    exports ir.sudoit.claudecode4j.rest.anthropic.dto.request;
    exports ir.sudoit.claudecode4j.rest.anthropic.dto.response;
    exports ir.sudoit.claudecode4j.rest.anthropic.mapper;
    exports ir.sudoit.claudecode4j.rest.anthropic.sse;
    exports ir.sudoit.claudecode4j.rest.autoconfigure;
    exports ir.sudoit.claudecode4j.rest.common.dto;
    exports ir.sudoit.claudecode4j.rest.common.streaming;
    exports ir.sudoit.claudecode4j.rest.controller;
    exports ir.sudoit.claudecode4j.rest.dto;
    exports ir.sudoit.claudecode4j.rest.openai.controller;
    exports ir.sudoit.claudecode4j.rest.openai.dto.request;
    exports ir.sudoit.claudecode4j.rest.openai.dto.response;
    exports ir.sudoit.claudecode4j.rest.openai.mapper;
    exports ir.sudoit.claudecode4j.rest.openai.sse;
    exports ir.sudoit.claudecode4j.rest.sse;
}
