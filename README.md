# ReferenceAccessPointsEAD
Add Reference attributes to XML files from mapping csv file

This project is very similar to XML-EAD project. The main concern was the Java DOM parser that would 
cause memory problems with huge files. We also moved to opencsv to read the mapping files. 
We made this project into a Maven project and added com.opencsv and javax.xml.stream as dependencies.
The parsing of xml files are done one line at a time and therefore we have no memory issues. 
Apparently, the export of the mappings file has not been done correctly and therefore many of the mappings 
in the big xml files are incomplete. 
