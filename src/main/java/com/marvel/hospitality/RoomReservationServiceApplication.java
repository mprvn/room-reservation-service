package com.marvel.hospitality;

import com.marvel.hospitality.common.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class RoomReservationServiceApplication {

	static void main(String[] args) {
		SpringApplication.run(RoomReservationServiceApplication.class, args);
	}

}
