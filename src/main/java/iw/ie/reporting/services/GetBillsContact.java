package iw.ie.reporting.services;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.chemistry.opencmis.client.api.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import iw.ie.reporting.services.multithreading.DocumentCallable;
import iw.ie.reporting.setup.Command;
import iw.ie.reporting.setup.LocalCmisUtility;

@Component
public class GetBillsContact implements Command {

	@Autowired
	LocalCmisUtility cmisUtil;

	private Session cmisSession;

	private String[] monthDetails = { "2014-01", "2015-02", "2014-03", "2014-01", "2015-02", "2014-03", "2014-04",
			"2015-05", "2014-06", "2014-07", "2015-08", "2014-09", "2014-10", "2015-11", "2014-12", "2015-01",
			"2015-02", "2015-03", "2015-04", "2015-05", "2015-06", "2015-07", "2015-08", "2015-09", "2015-10",
			"2015-11", "2015-12", "2016-01", "2016-02", "2016-03", "2016-04", "2016-05", "2016-06", "2016-07",
			"2016-08", "2016-09" };

	@Override
	public List<File> execute() throws Exception {

		if (cmisSession == null) {
			synchronized (this) {
				if (cmisSession == null) {
					cmisSession = cmisUtil.getCmisSession();
				}
			}
		}

		ExecutorService executorService = Executors.newFixedThreadPool(24);
		List<Callable<File>> tasks = new ArrayList<>();
		for (int i = 0; i < monthDetails.length - 1; i++) {
			Callable<File> task = new DocumentCallable<>(monthDetails[i], monthDetails[i + 1],
					"", cmisSession, "bills");
			tasks.add(task);
		}

		List<Future<File>> futures = executorService.invokeAll(tasks);
		List<File> reports = new ArrayList<>();

		for (Future<File> future : futures) {
			File report = future.get();
			reports.add(report);
		}
		executorService.shutdown();
		return reports;
	}
}
