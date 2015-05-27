<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>com.digitalbarista.cat</groupId>
  <artifactId>flex-common-lib</artifactId>
  <version>1.5</version>
  <packaging>swc</packaging>

  <name>CAT Shared Flex/Air Components</name>
  <organization>
      <name>Digital Barista, Inc.</name>
  </organization>
  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>

  <parent>
    <groupId>com.digitalbarista.cat</groupId>
    <artifactId>cat</artifactId>
    <version>1.5</version>
  </parent>

  <build>
  	<plugins>
      <plugin>
      <groupId>org.sonatype.flexmojos</groupId>
      <artifactId>flexmojos-maven-plugin</artifactId>
      <extensions>true</extensions>
        <executions>
          <execution>
            <id>compile-swc</id>
            <phase>compile</phase>
            <inherited>false</inherited>
            <goals>
              <goal>compile-swc</goal>
            </goals>
            <configuration>
              <keepAs3Metadatas>
                <keepAs3Metadata>Property</keepAs3Metadata>
              </keepAs3Metadatas>
              <incremental>true</incremental>
            </configuration>
          </execution>
        </executions>
        <dependencies>
          <dependency>
            <groupId>com.adobe.flex</groupId>
            <artifactId>compiler</artifactId>
            <version>3.5.0.12683</version>
            <type>pom</type>
          </dependency>
        </dependencies>
      </plugin>
  	</plugins>
    <resources>
      <resource>
        <directory>src</directory>
        <includes>
          <include>**/*</include>
        </includes>
        <excludes>
          <exclude>**/*.as</exclude>
          <exclude>**/*.mxml</exclude>
        </excludes>
      </resource>
    </resources>
  	<sourceDirectory>src</sourceDirectory>
  </build>

  <dependencies>
    <dependency>
      <groupId>com.adobe</groupId>
      <artifactId>Cairngorm</artifactId>
      <type>swc</type>
    </dependency>
    <dependency>
      <groupId>com.adobe.flex.framework</groupId>
      <artifactId>air-framework</artifactId>
      <type>pom</type>
    </dependency>
  </dependencies>
</project>
