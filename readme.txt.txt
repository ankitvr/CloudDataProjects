Application tested in java 8

Instructions to build and use this project. 

Step 1:
Please provide the Client and secret token in the class file:
	
Step 2:
Build the project:To build the project please run the following command in the root folder of the project
	mvn clean install

Step 3:
Run the project using the standalone jar by specifying the reporting you wish to trigger:Once the project is built a jar is created in the target folder.
The jar can be executed using the following command:
	java -jar <jar file>  <reportingMethod>
example:
	java -jar target\Reporting-Tools-0.0.1-SNAPSHOT.jar getAllCloudUsers
