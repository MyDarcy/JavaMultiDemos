package com.darcy.service.impl;

import static org.junit.Assert.fail;

import com.darcy.service.BookService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.darcy.BaseTest;
import com.darcy.dto.AppointExecution;

public class BookServiceImplTest extends BaseTest {

	@Autowired
	private BookService bookService;

	@Test
	public void testAppoint() throws Exception {
		long bookId = 1001;
		long studentId = 12345678910L;
		AppointExecution execution = bookService.appoint(bookId, studentId);
		System.out.println(execution);
	}

}
