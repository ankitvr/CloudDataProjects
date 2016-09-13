/*
 * Copyright (c) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package iw.ie.reporting.setup;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public final class LocalServerReceiver implements VerificationCodeReceiver {

	private static final String LOCALHOST = "127.0.0.1";

	@Autowired
	private Environment environment;

	private Object lock;

	/** Verification code or {@code null} before received. */
	volatile String code;

	@Override
	public String getRedirectUri() throws Exception {
		return "http://" + LOCALHOST + ":" + environment.getProperty("server.port")
				.concat(environment.getProperty("server.context-path")).concat(environment.getProperty("callback.url"));
	}

	@Override
	public String waitForCode() {
		try {
			if (lock == null) {
				lock = new Object();
			}
			synchronized (lock) {
				lock.wait();
			}
		} catch (InterruptedException e) {
		}
		return code;
	}

	@RequestMapping(path = { "${callback.url}" })
	public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String error = request.getParameter("error");
		if (error != null) {
			System.out.println("Authorization failed. Error=" + error);
			System.out.println("Quitting.");
			System.exit(1);
		}
		code = request.getParameter("code");
		synchronized (lock) {
			lock.notify();
		}
	}

	public Environment getEnvironment() {
		return environment;
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}
}
