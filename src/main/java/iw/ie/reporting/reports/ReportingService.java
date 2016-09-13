package iw.ie.reporting.reports;

import java.io.File;
import java.util.List;

public interface ReportingService {
	static final String REPORTING_SITE = "/Sites/reporting/documentLibrary/";
	
	void uploadReport(List<File> reportFiles, String args);
}
