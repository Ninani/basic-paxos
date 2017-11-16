package com.agh.edu.iosr.paxos;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ServerApplicationTests {

	static {
		System.setProperty("timestamp",String.valueOf(System.currentTimeMillis()));
	}


	@Test
	public void contextLoads() {
	}

}
