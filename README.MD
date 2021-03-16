# java版grpc教程

## 普通java工程引入grpc教程

### 1. pom文件引入grpc依赖
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.example</groupId>
    <artifactId>grpc_sample</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <java.version>1.8</java.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <grpc.version>1.34.1</grpc.version><!-- CURRENT_GRPC_VERSION -->
        <protobuf.version>3.12.0</protobuf.version>
        <protoc.version>3.12.0</protoc.version>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
    </properties>

    <dependencyManagement>
        <!--声明grpc的版本-->
        <dependencies>
            <dependency>
                <groupId>io.grpc</groupId>
                <artifactId>grpc-bom</artifactId>
                <version>${grpc.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-netty-shaded</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-protobuf</artifactId>
        </dependency>
        <dependency>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-stub</artifactId>
        </dependency>
        <dependency>
            <groupId>com.google.protobuf</groupId>
            <artifactId>protobuf-java-util</artifactId>
            <version>${protobuf.version}</version>
        </dependency>
    </dependencies>

    <build>
        <!--插件的作用主要用于在编译的时候根据proto生成java代码-->
        <extensions>
            <!--用于获取os相关信息-->
            <extension>
                <groupId>kr.motd.maven</groupId>
                <artifactId>os-maven-plugin</artifactId>
                <version>1.6.2</version>
            </extension>
        </extensions>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.xolstice.maven.plugins</groupId>
                <artifactId>protobuf-maven-plugin</artifactId>
                <version>0.6.1</version>
                <configuration>
                    <protocArtifact>com.google.protobuf:protoc:${protoc.version}:exe:${os.detected.classifier}
                    </protocArtifact>
                    <pluginId>grpc-java</pluginId>
                    <pluginArtifact>io.grpc:protoc-gen-grpc-java:${grpc.version}:exe:${os.detected.classifier}
                    </pluginArtifact>
                    <!--proto文件的位置-->
                    <protoSourceRoot>src/main/resources/proto/</protoSourceRoot>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>compile</goal>
                            <goal>compile-custom</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>1.8</source>
                    <target>1.8</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### 2.在resource下创建proto目录,编写交互的proto文件
```protobuf
syntax = "proto3";

package mygrpc;
option java_package = "a.b.c";
option java_outer_classname = "AddServiceProto";
option java_multiple_files = true;

service TableNumberService{
  rpc getTableNumber(GetTableNumberRequest) returns (GetTableNumberResponse){}
}

message GetTableNumberRequest{
  string commodityId = 1;
  int32 tableCount = 2;
}

message GetTableNumberResponse{
  int32 tableNumber = 1;
}
```

### 3. 此时便可以通过mvn compile编译生成grpc的代码,代码在target/generated-sources/protobuf目录下
```shell 
mvn clean;
mvn compile;
```

### 4. 编写grpc服务端代码，并运行
```java
package com.watermelon.demo;

import a.b.c.GetTableNumberRequest;
import a.b.c.GetTableNumberResponse;
import a.b.c.TableNumberServiceGrpc;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;

public class GetTableNumberServer extends TableNumberServiceGrpc.TableNumberServiceImplBase {
    @Override
    public void getTableNumber(GetTableNumberRequest request, StreamObserver<GetTableNumberResponse> responseObserver) {
        int count = getTableNumber(request.getCommodityId(),request.getTableCount());
        responseObserver.onNext(GetTableNumberResponse.newBuilder().setTableNumber(count).build());
        responseObserver.onCompleted();
    }

    public int getTableNumber(String commodityId, int count) {
        System.out.println(String.format("GetTableNumberServer getTableNumber(%s,%s)", commodityId, count));
        return (commodityId.hashCode() & Integer.MAX_VALUE) % count;
    }


    public static void main(String[] args) throws IOException {
        ServerBuilder.forPort(9999)
                .addService(new GetTableNumberServer())
                .build()
                .start();

        while (true){

        }

    }
}
```

### 5. 编写客户端代码，运行
```java
package com.watermelon.demo;

import a.b.c.GetTableNumberRequest;
import a.b.c.GetTableNumberResponse;
import a.b.c.TableNumberServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GetTableNumberClient {
    /**
     * 桩，rpc通过桩连接客户端与服务端，
     * 客户端存根(Client Stub)，存放服务端的地址消息，再将客户端的请求参数打包成网络消息，然后通过网络远程发送给服务方。
     * 服务端存根(Server Stub)，接收客户端发送过来的消息，将消息解包，并调用本地的方法。
     */
    private TableNumberServiceGrpc.TableNumberServiceBlockingStub stub;

    /**
     * 通道
     */
    private ManagedChannel channel;

    public GetTableNumberClient() {
        channel = ManagedChannelBuilder.forAddress("localhost", 9999)
                .usePlaintext()
                .build();

        stub = TableNumberServiceGrpc.newBlockingStub(channel);
    }

    public static void main(String[] args) {
        String commodity="123213123cefafewafewafwe";
        int count=64;
        GetTableNumberClient client = new GetTableNumberClient();
        GetTableNumberResponse tableNumber = client.stub.getTableNumber(GetTableNumberRequest.newBuilder().setCommodityId(commodity)
                .setTableCount(count).build());
        System.out.println(tableNumber.getTableNumber());
    }
}
```



## springboot引入grpc教程

此教程使用的是:

```xml
        <dependency>
            <groupId>net.devh</groupId>
            <artifactId>grpc-spring-boot-starter</artifactId>
            <version>2.11.0.RELEASE</version>
        </dependency>
```



### 服务端

#### 1. pom文件引入grpc依赖
```java
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.4.3</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.watermelon</groupId>
    <artifactId>springboot_grpc_client</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>springboot_grpc_client</name>
    <description>Demo project for Spring Boot</description>
    <properties>
        <java.version>1.8</java.version>
        <grpc.version>1.35.0</grpc.version>
        <protobuf.version>3.14.0</protobuf.version>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>

        <!-- https://mvnrepository.com/artifact/net.devh/grpc-spring-boot-starter -->
        <dependency>
            <groupId>net.devh</groupId>
            <artifactId>grpc-spring-boot-starter</artifactId>
            <version>2.11.0.RELEASE</version>
        </dependency>


    </dependencies>

    <build>
        <extensions>
            <extension>
                <groupId>kr.motd.maven</groupId>
                <artifactId>os-maven-plugin</artifactId>
                <version>1.6.1</version>
            </extension>
        </extensions>

        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>

            <plugin>
                <groupId>org.xolstice.maven.plugins</groupId>
                <artifactId>protobuf-maven-plugin</artifactId>
                <version>0.6.1</version>
                <configuration>
                    <protocArtifact>com.google.protobuf:protoc:${protobuf.version}:exe:${os.detected.classifier}</protocArtifact>
                    <pluginId>grpc-java</pluginId>
                    <pluginArtifact>io.grpc:protoc-gen-grpc-java:${grpc.version}:exe:${os.detected.classifier}</pluginArtifact>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>compile</goal>
                            <goal>compile-custom</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

#### 2. 在src/main目录下创建proto目录,编写交互的proto文件

```proto
syntax = "proto3";

package mygrpc;
option java_package = "com.watermelon.grpc";
option java_outer_classname = "GetTableNumberServiceProto";
option java_multiple_files = true;

service TableNumberService{
  rpc getTableNumber(GetTableNumberRequest) returns (GetTableNumberResponse){}
}

message GetTableNumberRequest{
  string commodityId = 1;
  int32 tableCount = 2;
}

message GetTableNumberResponse{
  int32 tableNumber = 1;
}
```

#### 3. 此时便可以通过mvn compile编译生成grpc的代码,代码在target/generated-sources/protobuf目录下
```shell 
mvn clean;
mvn compile;
```

#### 4. 编写grpc服务端代码

```java
package com.watermelon.demo.grpc;

import com.watermelon.grpc.GetTableNumberRequest;
import com.watermelon.grpc.GetTableNumberResponse;
import com.watermelon.grpc.TableNumberServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class GetTableNumberGRpcService extends TableNumberServiceGrpc.TableNumberServiceImplBase {
    @Override
    public void getTableNumber(GetTableNumberRequest request, StreamObserver<GetTableNumberResponse> responseObserver) {
        int count = getTableNumber(request.getCommodityId(),request.getTableCount());
        responseObserver.onNext(GetTableNumberResponse.newBuilder().setTableNumber(count).build());
        responseObserver.onCompleted();
    }

    public int getTableNumber(String commodityId, int count) {
        System.out.println(String.format("GetTableNumberServer getTableNumber(%s,%s)", commodityId, count));
        return (commodityId.hashCode() & Integer.MAX_VALUE) % count;
    }
}
```

#### 5. 自定义grpc服务端口

```properties
grpc.server.port= 9898
```



运行springboot项目



### 客户端

#### 1. Pom文件里引入grpc依赖

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.4.3</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.watermelon</groupId>
    <artifactId>springboot_grpc_client</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>springboot_grpc_client</name>
    <description>Demo project for Spring Boot</description>
    <properties>
        <java.version>1.8</java.version>
        <grpc.version>1.35.0</grpc.version>
        <protobuf.version>3.14.0</protobuf.version>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>

        <!-- https://mvnrepository.com/artifact/net.devh/grpc-spring-boot-starter -->
        <dependency>
            <groupId>net.devh</groupId>
            <artifactId>grpc-spring-boot-starter</artifactId>
            <version>2.11.0.RELEASE</version>
        </dependency>


    </dependencies>

    <build>
        <extensions>
            <extension>
                <groupId>kr.motd.maven</groupId>
                <artifactId>os-maven-plugin</artifactId>
                <version>1.6.1</version>
            </extension>
        </extensions>

        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>

            <plugin>
                <groupId>org.xolstice.maven.plugins</groupId>
                <artifactId>protobuf-maven-plugin</artifactId>
                <version>0.6.1</version>
                <configuration>
                    <protocArtifact>com.google.protobuf:protoc:${protobuf.version}:exe:${os.detected.classifier}</protocArtifact>
                    <pluginId>grpc-java</pluginId>
                    <pluginArtifact>io.grpc:protoc-gen-grpc-java:${grpc.version}:exe:${os.detected.classifier}</pluginArtifact>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>compile</goal>
                            <goal>compile-custom</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

#### 2.在src/main目录下创建proto目录，编写交互的proto文件

```proto
syntax = "proto3";

package mygrpc;
option java_package = "com.watermelon.grpc";
option java_outer_classname = "GetTableNumberServiceProto";
option java_multiple_files = true;

service TableNumberService{
  rpc getTableNumber(GetTableNumberRequest) returns (GetTableNumberResponse){}
}

message GetTableNumberRequest{
  string commodityId = 1;
  int32 tableCount = 2;
}

message GetTableNumberResponse{
  int32 tableNumber = 1;
}
```

#### 3.添加要访问的服务端grpc配置

```properties
grpc.client.grpc_server.address=static://localhost:9898
grpc.client.grpc_server.negotiation-type=plaintext

server.port=8000
```

#### 4. 作为客户端调用服务端的grpc接口

```java
package com.watermelon.demo.client;

import com.watermelon.grpc.GetTableNumberRequest;
import com.watermelon.grpc.GetTableNumberResponse;
import com.watermelon.grpc.TableNumberServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class GetTableNumberRpcClient {

    @GrpcClient("grpc_server")
    private TableNumberServiceGrpc.TableNumberServiceBlockingStub stub;

    public Integer getTableNumber(){
        GetTableNumberRequest request = GetTableNumberRequest.newBuilder()
                .setCommodityId("112321321312waeffsd")
                .setTableCount(4).build();
        GetTableNumberResponse tableNumber = stub.getTableNumber(request);
        return tableNumber.getTableNumber();
    }

}
```