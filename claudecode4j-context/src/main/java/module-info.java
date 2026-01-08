module ir.sudoit.claudecode4j.context {
    requires ir.sudoit.claudecode4j.api;
    requires jtokkit;
    requires static org.jspecify;
    requires static spring.boot;
    requires static spring.boot.autoconfigure;
    requires static spring.context;
    requires static spring.beans;
    requires static spring.core;

    exports ir.sudoit.claudecode4j.context.spi;
    exports ir.sudoit.claudecode4j.context.model;
    exports ir.sudoit.claudecode4j.context.optimizer;
    exports ir.sudoit.claudecode4j.context.tokenizer;
    exports ir.sudoit.claudecode4j.context.autoconfigure;

    uses ir.sudoit.claudecode4j.context.spi.ContextOptimizer;
    uses ir.sudoit.claudecode4j.context.spi.TokenCounter;

    provides ir.sudoit.claudecode4j.context.spi.TokenCounter with
            ir.sudoit.claudecode4j.context.tokenizer.JTokkitTokenCounter;
    provides ir.sudoit.claudecode4j.context.spi.ContextOptimizer with
            ir.sudoit.claudecode4j.context.optimizer.DefaultContextOptimizer;

    opens ir.sudoit.claudecode4j.context.autoconfigure to
            spring.core,
            spring.beans,
            spring.context;
}
