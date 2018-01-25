# ReferenceAccessPointsEAD
Add Reference attributes to XML files from mapping csv file

This project is very similar to XML-EAD project. The main concern was the Java DOM parser that would 
cause memory problems with huge files. We also moved to opencsv to read the mapping files. 
We made this project into a Maven project and added com.opencsv and javax.xml.stream as dependencies.
The project uses stax api and does not read the xml file into a dom tree because the xml files might be
very big and this might cause memory problems. The parsing of xml files are done one line at a time.
Therefore we do not anticipate any no memory issues. 
The input arguments to the program are the name the input files directory and the mapping file name. 
We create an output directory in which we put the corrected files. For example the following line:
Java –jar ReferenceAccessPointsEAD.jar input mappings_yv.csv 
we create a folder called input_coref and in it we put all the corrected output files we find in input directory.
Apparently, the export of the mappings file has not been done correctly and therefore many of the mappings 
in the big xml files are incomplete. To make it easier for the user to identify which mappings are missing,
we generate for each file we read the listing of the missing mappings in the same <folder>_coref directory.
If a file is named: O41_ENG.xml we create a file called O41_ENG_missing.txt in the out put directory.
To build this project in Eclipse, you have to create the project as a Maven project. In the pom file add 
the following dependencies:
    <dependency>
  		<groupId>com.opencsv</groupId>
  		<artifactId>opencsv</artifactId>
  		<version>4.0</version>
  	</dependency>
  	<dependency>
  		<groupId>javax.xml.stream</groupId>
  		<artifactId>stax-api</artifactId>
  		<version>1.0-2</version>
  	</dependency>
    

