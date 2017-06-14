package iw.ie.reporting;

import java.io.File;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import iw.ie.reporting.reports.ReportingService;
import iw.ie.reporting.setup.Command;

@SpringBootApplication

public class Application implements CommandLineRunner {

	@Autowired
	ApplicationContextProvider contextProvider;

	@Autowired
	ReportingService reportingService;

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		SpringApplication.run(Application.class, args);
	}

	public void run(String... args) throws Exception {
		if (args.length > 0) {
			Command command = contextProvider.getContext().getBean(args[0], Command.class);
			List<File> reportFiles = command.execute();
			if (reportFiles != null) {
				//reportingService.uploadReport(reportFiles, command.getClass().getSimpleName());
			}
		}
	}
}
