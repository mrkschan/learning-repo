# Build Dependency #

There are already files included in web/ directory and lib/ directory.
Still, there are some other dependency needed for building the deployment .war. They are: 

- antisamy-1.3-SNAPSHOT.jar (can be found in ESAPI-1.4.3 distribution)
- batik-css-1.6-1.jar (can be found in ESAPI-1.4.3 distribution)
- mongo-1.2.jar (http://cloud.github.com/downloads/mongodb/mongo-java-driver/mongo-1.2.jar)
- nekohtml-1.9.11.jar (can be found in ESAPI-1.4.3 distribution)
- xercesImpl-2.8.1.jar (can be found in ESAPI-1.4.3 distribution)

# How to Build #

Ant 1.7+ is required to build the deployment .war. The build process need a proper configuration of j2ee server. Modify the file build.properties and change the j2ee.server.path according to your environment.

    build.properties:
    j2ee.server.path=<path_to_j2ee_server>

Run the following Ant task to build the .war:

    $> ant dist

