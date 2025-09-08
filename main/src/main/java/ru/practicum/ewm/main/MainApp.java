package ru.practicum.ewm.main;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "ru.practicum.ewm.main",
        "ru.practicum.ewm.client.stats"
})
public class MainApp {
    public static void main(String[] args) {
        SpringApplication.run(MainApp.class);
    }
}
