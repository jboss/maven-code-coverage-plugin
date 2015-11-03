# maven-code-coverage-plugin
Maven code coverage plugin -- bytecode code inspection for class usage

Maven configuration usage:

        <profile>
            <id>default</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <skipTests>true</skipTests>
            </properties>

            <build>
                <plugins>
                    <plugin>
                        <groupId>org.jboss.maven.plugins</groupId>
                        <artifactId>code-coverage-maven-plugin</artifactId>
                        <version>${version.org.jboss.maven.coverage.plugin}</version>
                        <executions>
                            <execution>
                                <id>api-coverage</id>
                                <goals>
                                    <goal>coverage</goal>
                                </goals>
                                <phase>process-test-classes</phase>
                                <configuration>
                                    <module>tests</module>
                                    <repositoryUser>GoogleCloudPlatform</repositoryUser>
                                    <repositoryProject>appengine-tck</repositoryProject>
                                    <coverageTitle>GAE API</coverageTitle>
                                    <javadocRoot>http://developers.google.com/appengine/docs/java/javadoc/</javadocRoot>
                                </configuration>
                            </execution>
                        </executions>
                    </plugin>
                </plugins>
            </build>

            <dependencies>
                <dependency>
                    <groupId>org.jboss.maven.plugins</groupId>
                    <artifactId>code-coverage-maven-plugin</artifactId>
                </dependency>
            </dependencies>
        </profile>

Put coverage.txt files (listing classes you want to cover / check) into resources/ dir:

    com.google.appengine.api.taskqueue.Queue
    com.google.appengine.api.taskqueue.DeferredTaskContext
    com.google.appengine.api.taskqueue.LeaseOptions
    com.google.appengine.api.taskqueue.LeaseOptions$Builder
    com.google.appengine.api.taskqueue.QueueFactory
    com.google.appengine.api.taskqueue.QueueStatistics
    com.google.appengine.api.taskqueue.RetryOptions
    com.google.appengine.api.taskqueue.RetryOptions$Builder
    com.google.appengine.api.taskqueue.TaskHandle
    com.google.appengine.api.taskqueue.TaskOptions
    com.google.appengine.api.taskqueue.TaskOptions$Builder

And potential exclusions.txt (next to coverage.txt):

    # TaskHandle
    com.google.appengine.api.taskqueue.TaskHandle@<init>@(Lcom/google/appengine/api/taskqueue/TaskOptions;Ljava/lang/String;)V
