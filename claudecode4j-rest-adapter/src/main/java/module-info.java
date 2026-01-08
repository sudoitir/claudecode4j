open module ir.sudoit.claudecode4j.rest {
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
    requires tools.jackson.databind;
    requires org.hibernate.validator;
    requires com.fasterxml.jackson.annotation;
    requires static org.jspecify;

    exports ir.sudoit.claudecode4j.rest.controller;
    exports ir.sudoit.claudecode4j.rest.dto;
    exports ir.sudoit.claudecode4j.rest.autoconfigure;
    exports ir.sudoit.claudecode4j.rest.sse;
    exports ir.sudoit.claudecode4j.rest.openai.controller;
    exports ir.sudoit.claudecode4j.rest.openai.dto.request;
    exports ir.sudoit.claudecode4j.rest.openai.dto.response;
    exports ir.sudoit.claudecode4j.rest.openai.mapper;
    exports ir.sudoit.claudecode4j.rest.openai.sse;
    exports ir.sudoit.claudecode4j.rest.anthropic.controller;
    exports ir.sudoit.claudecode4j.rest.anthropic.dto.request;
    exports ir.sudoit.claudecode4j.rest.anthropic.dto.response;
    exports ir.sudoit.claudecode4j.rest.anthropic.mapper;
    exports ir.sudoit.claudecode4j.rest.anthropic.sse;
    exports ir.sudoit.claudecode4j.rest.common.dto;
    exports ir.sudoit.claudecode4j.rest.common.streaming;
    exports ir.sudoit.claudecode4j.rest.config;
}
