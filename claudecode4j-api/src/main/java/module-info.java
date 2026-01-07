module ir.sudoit.claudecode4j.api {
    requires static org.jspecify;

    exports ir.sudoit.claudecode4j.api.client;
    exports ir.sudoit.claudecode4j.api.model.request;
    exports ir.sudoit.claudecode4j.api.model.response;
    exports ir.sudoit.claudecode4j.api.config;
    exports ir.sudoit.claudecode4j.api.exception;
    exports ir.sudoit.claudecode4j.api.spi;
    exports ir.sudoit.claudecode4j.api.annotation;

    uses ir.sudoit.claudecode4j.api.client.ClaudeClientFactory;
    uses ir.sudoit.claudecode4j.api.spi.BinaryResolver;
    uses ir.sudoit.claudecode4j.api.spi.InputSanitizer;
    uses ir.sudoit.claudecode4j.api.spi.OutputParser;
    uses ir.sudoit.claudecode4j.api.spi.ProcessExecutor;
}
