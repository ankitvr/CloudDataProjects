package iw.ie.reporting.setup;

import java.io.File;
import java.util.List;

public interface Command {
	public List<File> execute() throws Exception;
}
