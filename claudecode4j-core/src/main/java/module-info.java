module ir.sudoit.claudecode4j.core {
    uses ir.sudoit.claudecode4j.api.spi.BinaryResolver;
    requires ir.sudoit.claudecode4j.api;
    requires static org.jspecify;

    exports ir.sudoit.claudecode4j.core.client;
    exports ir.sudoit.claudecode4j.core.resolver;
    exports ir.sudoit.claudecode4j.core.process;
    exports ir.sudoit.claudecode4j.core.parser;
    exports ir.sudoit.claudecode4j.core.security;

    provides ir.sudoit.claudecode4j.api.client.ClaudeClientFactory with
            ir.sudoit.claudecode4j.core.client.DefaultClaudeClientFactory;
    provides ir.sudoit.claudecode4j.api.spi.BinaryResolver with
            ir.sudoit.claudecode4j.core.resolver.NpmBinaryResolver,
            ir.sudoit.claudecode4j.core.resolver.PathBinaryResolver;
    provides ir.sudoit.claudecode4j.api.spi.InputSanitizer with
            ir.sudoit.claudecode4j.core.security.DefaultInputSanitizer;
    provides ir.sudoit.claudecode4j.api.spi.ProcessExecutor with
            ir.sudoit.claudecode4j.core.process.VirtualThreadExecutor;
    provides ir.sudoit.claudecode4j.api.spi.OutputParser with
            ir.sudoit.claudecode4j.core.parser.StreamJsonParser;
}
