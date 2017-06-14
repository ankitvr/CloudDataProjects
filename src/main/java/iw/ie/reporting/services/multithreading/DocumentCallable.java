package iw.ie.reporting.services.multithreading;

import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.concurrent.Callable;

import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.log4j.Logger;

public class DocumentCallable<T> implements Callable<File> {

	String startMonthDetail;

	String nextMonthDetail;

	Session cmisSession;

	String type;

	String documentType;

	private static Logger log = Logger.getLogger(DocumentCallable.class);

	@Override
	public File call() throws Exception {
		OperationContext context = cmisSession.getDefaultContext();
		context.setMaxItemsPerPage(1000);
		String statement = "select b.* from cmis:document d join outcc:bill b on d.cmis:objectId = b.cmis:objectId"
				+ " where  d.cmis:creationDate  > TIMESTAMP '" + startMonthDetail
				+ "-01T00:00:00.000-00:00' and d.cmis:creationDate < TIMESTAMP '" + nextMonthDetail
				+ "-01T00:00:00.000-00:00'";
		log.debug(statement);
		ItemIterable<QueryResult> results = cmisSession.query(statement, false);

		/*
		 * ItemIterable<QueryResult> results = cmisSession .query(
		 * "select * from cmis:document d " +
		 * " where  IN_TREE(d, 'workspace://SpacesStore/a3bd370b-1aa5-4313-9e9b-32d64694da15') AND  d.cmis:creationDate  > TIMESTAMP '"
		 * + startMonthDetail +
		 * "-01T00:00:00.000-00:00' and d.cmis:creationDate < TIMESTAMP '" +
		 * nextMonthDetail + "-01T00:00:00.000-00:00'", false);
		 */

		File result = null;
		long count = results.getTotalNumItems();
		if (results.getHasMoreItems()) {

			result = new File(type + startMonthDetail + ".txt");
			try (FileWriter writer = new FileWriter(result)) {
				for (long i = 0; i < count; i++) {
					Iterator<QueryResult> iter = results.skipTo(i).iterator();
					while (iter.hasNext()) {
						QueryResult doc = null;
						try {
							doc = iter.next();
							PropertyData<Object> property = doc.getPropertyById("outcc:billId");
							if (property != null) {
								String billId = property.getFirstValue().toString();
								if (!(null == billId || billId.equals("null") || billId.isEmpty()))
									writer.write(billId);
								writer.write("\n");
							}
						} catch (Exception e) {
							try {
								System.out.println(doc.getPropertyById("cmis:name"));
								PropertyData<Object> property = doc.getPropertyById("cmis:name");
								if (property != null) {
									String billId = property.getFirstValue().toString();
									if (!(null == billId || billId.equals("null") || billId.isEmpty()))
										if (billId.length() > 12) {
											billId = billId.substring(0, 11);
										}
									writer.write(billId);
									writer.write("\n");
								}
							} catch (Exception e1) {
								System.out.println("***** Unexpected Errorr ********");
								e1.printStackTrace();
							}

							e.printStackTrace();
						}
					}
					writer.flush();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		return result;
	}

	public DocumentCallable(String startMonthDetail, String nextMonthDetail, String documentType, Session cmisSession,
			String type) {
		super();
		this.startMonthDetail = startMonthDetail;
		this.nextMonthDetail = nextMonthDetail;
		this.documentType = documentType;
		this.cmisSession = cmisSession;
		this.type = type;
	}
}
