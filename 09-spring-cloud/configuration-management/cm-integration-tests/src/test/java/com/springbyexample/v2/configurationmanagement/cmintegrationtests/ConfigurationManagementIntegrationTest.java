package com.springbyexample.v2.configurationmanagement.cmintegrationtests;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.client.RestClient;

import com.springbyexample.v2.configurationmanagement.cmconfigserver.ConfigServerApplication;
import com.springbyexample.v2.configurationmanagement.cmorderservice.OrderConfigurationResponse;
import com.springbyexample.v2.configurationmanagement.cmorderservice.OrderServiceApplication;

/**
 * Integration tests for Spring Cloud Configuration Management.
 *
 * @author Mujuzi Moses
 */
public class ConfigurationManagementIntegrationTest {

    private static ConfigurableApplicationContext configServer;

    private static ConfigurableApplicationContext orderService;

    private static Path temporaryConfigRepository;

    private static int orderServicePort;

    @BeforeAll
    static void startApplications() throws IOException, InterruptedException {
        temporaryConfigRepository = createGitRepository();

        configServer = new SpringApplicationBuilder(ConfigServerApplication.class)
                .run("--server.port=0", "--spring.cloud.config.server.git.uri=" + temporaryConfigRepository.toUri());

        int configServerPort = portOf(configServer);

        String configServerUrl = "http://localhost:" + configServerPort;

        orderService = new SpringApplicationBuilder(OrderServiceApplication.class)
                .run(
                        "--server.port=0",
                        "--spring.application.name=cm-order-service",
                        "--spring.profiles.active=dev",
                        "--spring.cloud.config.enabled=true",
                        "--spring.config.import=configserver:" + configServerUrl
                );

        orderServicePort = portOf(orderService);
    }

    @AfterAll
    static void stopApplications() throws IOException {
        closeContext(orderService);
        closeContext(configServer);

        if (temporaryConfigRepository != null) {
            deleteDirectory(temporaryConfigRepository);
        }
    }

    @Test
    void orderService_shouldLoadCentralizedConfiguration() {
        OrderConfigurationResponse response = RestClient.create()
                .get()
                .uri("http://localhost:" + orderServicePort + "/api/orders/configuration")
                .retrieve()
                .body(OrderConfigurationResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.message()).isEqualTo("Orders are processed using the development configuration.");
        assertThat(response.maxItems()).isEqualTo(25);
        assertThat(response.currency()).isEqualTo("USD");
        assertThat(response.expeditedEnabled()).isTrue();
    }

    private static Path createGitRepository() throws IOException, InterruptedException {
        Path sourceDirectory = Path.of("../config-repo").toAbsolutePath().normalize();
        Path repository =
                Files.createTempDirectory(Path.of("target").toAbsolutePath(), "configuration-repository");

        try (Stream<Path> paths = Files.walk(sourceDirectory)) {
            for (Path source : paths.filter(Files::isRegularFile).toList()) {
                Path destination = repository.resolve(sourceDirectory.relativize(source));

                Files.createDirectories(destination.getParent());
                Files.copy(source, destination);
            }
        }

        executeGit(repository, "init");
        executeGit(repository, "config", "user.name", "Spring by Example");
        executeGit(repository, "config", "user.email", "spring-by-example@example.com");
        executeGit(repository, "add", ".");
        executeGit(repository, "commit", "-m", "Add configuration files");
        executeGit(repository, "branch", "-M", "main");

        return repository;
    }

    private static void executeGit(Path directory, String... arguments) throws IOException, InterruptedException {

        String[] command = new String[arguments.length + 1];
        command[0] = "git";
        System.arraycopy(arguments, 0, command, 1, arguments.length);

        Process process = new ProcessBuilder(command).directory(directory.toFile()).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes());

        if (process.waitFor() != 0) {
            throw new IllegalStateException("Git command failed: %s%n%s".formatted(String.join(" ", command), output));
        }
    }

    private static int portOf(ConfigurableApplicationContext context) {
        return Objects.requireNonNull(((WebServerApplicationContext) context).getWebServer()).getPort();
    }

    private static void closeContext(ConfigurableApplicationContext context) {
        if (context != null) {
            context.close();
        }
    }

    private static void deleteDirectory(Path directory) throws IOException {
        try (Stream<Path> paths = Files.walk(directory)) {
            for (Path path : paths.sorted((first, second) -> second.compareTo(first)).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }
}
