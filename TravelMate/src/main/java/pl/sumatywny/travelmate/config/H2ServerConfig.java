package pl.sumatywny.travelmate.config;

import org.h2.tools.Server;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;

@Configuration
public class H2ServerConfig {

    @PostConstruct
    public void startH2TCPServer() throws SQLException {
        Server.createTcpServer(
                "-tcpAllowOthers",
                "-tcpPort", "9092",
                "-ifNotExists"
        ).start();
        System.out.println(">>> H2 TCP server started on port 9092");
    }
}