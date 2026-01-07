module ir.sudoit.claudecode4j.api {
    requires static org.jspecify;

    exports ir.sudoit.claudecode4j.api.client;
    exports ir.sudoit.claudecode4j.api.model.request;
    exports ir.sudoit.claudecode4j.api.model.response;
    exports ir.sudoit.claudecode4j.api.config;
    exports ir.sudoit.claudecode4j.api.exception;
    exports ir.sudoit.claudecode4j.api.spi;
    exports ir.sudoit.claudecode4j.api.annotation;
    exports ir.sudoit.claudecode4j.api.mock;
    exports ir.sudoit.claudecode4j.api.tool;
    exports ir.sudoit.claudecode4j.api.cost;
    exports ir.sudoit.claudecode4j.api.template;
    exports ir.sudoit.claudecode4j.api.session;

    uses ir.sudoit.claudecode4j.api.client.ClaudeClientFactory;
    uses ir.sudoit.claudecode4j.api.tool.ToolRegistry;
    uses ir.sudoit.claudecode4j.api.cost.CostEstimator;
    uses ir.sudoit.claudecode4j.api.template.TemplateEngine;
    uses ir.sudoit.claudecode4j.api.session.SessionStore;
    uses ir.sudoit.claudecode4j.api.spi.BinaryResolver;
    uses ir.sudoit.claudecode4j.api.spi.InputSanitizer;
    uses ir.sudoit.claudecode4j.api.spi.OutputParser;
    uses ir.sudoit.claudecode4j.api.spi.ProcessExecutor;
}
