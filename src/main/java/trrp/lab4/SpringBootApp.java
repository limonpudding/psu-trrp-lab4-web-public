package trrp.lab4;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import trrp.lab4.service.EgrulWorkerConnector;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication(scanBasePackages = {"trrp.lab4", "trrp.lab4.service"})
public class SpringBootApp {

    private final Integer pdfWorkerPort;

    private final String pdfWorkerIp;

    private final String pdfWorkerToken;

    private final Integer pdfWorkerConnectorPort;

    @Autowired
    private ApplicationContext applicationContext;

    public static void main(String[] args) {
        SpringApplication.run(SpringBootApp.class, args);
    }

    SpringBootApp(@Value("${pdf.worker.port}") Integer pdfWorkerPort,
                  @Value("${pdf.worker.ip}") String pdfWorkerIp,
                  @Value("${pdf.worker.token}") String pdfWorkerToken,
                  @Value("${pdf.worker.connector.port}") Integer pdfWorkerConnectorPort) {
        this.pdfWorkerPort = pdfWorkerPort;
        this.pdfWorkerIp = pdfWorkerIp;
        this.pdfWorkerToken = pdfWorkerToken;
        this.pdfWorkerConnectorPort = pdfWorkerConnectorPort;
    }

    @PostConstruct
    private void init() {
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) applicationContext.getBean("taskExecutor");

        EgrulWorkerConnector egrulWorkerConnector = applicationContext.getBean(EgrulWorkerConnector.class);
        executor.execute(egrulWorkerConnector);


        ProcessBuilder processBuilder = new ProcessBuilder();

        Thread game = new Thread(() -> {
            try {
                List<String> workerArgs = new ArrayList<>();
                workerArgs.add("python");
                workerArgs.add("parser.py");
                workerArgs.add(pdfWorkerIp);
                workerArgs.add("--bindPort");
                workerArgs.add(pdfWorkerPort.toString());
                workerArgs.add("--connPort");
                workerArgs.add(pdfWorkerConnectorPort.toString());
                workerArgs.add("--token");
                workerArgs.add(pdfWorkerToken);
                processBuilder.command(workerArgs);
                processBuilder.directory(new File("pdf-worker"));
                Process pdfWorker = processBuilder.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(pdfWorker.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[PYTHON-PDF-WORKER] " + line);
                }

                int exitCode = pdfWorker.waitFor();
                System.out.println("\nРабота pdf-worker завершилась ошибкой: " + exitCode);

            } catch (Exception e) {
                System.out.println(e);
            }
        });
        game.setDaemon(false);
        game.start();
    }
}